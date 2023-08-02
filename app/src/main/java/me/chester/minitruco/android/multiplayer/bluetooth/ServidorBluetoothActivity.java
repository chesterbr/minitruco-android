package me.chester.minitruco.android.multiplayer.bluetooth;

import static me.chester.minitruco.core.JogadorBot.APELIDO_BOT;
import static me.chester.minitruco.core.TrucoUtils.POSICAO_PLACEHOLDER;
import static me.chester.minitruco.core.TrucoUtils.montaNotificacaoI;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;

import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.Partida;
import me.chester.minitruco.core.PartidaLocal;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

@SuppressLint("MissingPermission")  // super.onCreate checa as permissões
public class ServidorBluetoothActivity extends BluetoothActivity {

    private final static Logger LOGGER = Logger.getLogger("ServidorBluetoothActivity");

    private static final char STATUS_AGUARDANDO = 'A';
    private static final char STATUS_LOTADO = 'L';
    private static final char STATUS_EM_JOGO = 'J';
    private static final char STATUS_BLUETOOTH_ENCERRADO = 'X';
    private static final int REQUEST_ENABLE_DISCOVERY = 1;

    private char status;
    private Thread threadMonitoraClientes;
    private Partida partida;
    private boolean aguardandoDiscoverable = false;
    private Thread threadAguardaConexoes;
    private BluetoothServerSocket serverSocket;

    private final JogadorBluetooth[] jogadores = new JogadorBluetooth[3];

    private final boolean[] respondeuComVersaoOk = new boolean[3];

    private final BroadcastReceiver receiverMantemDiscoverable = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            pedePraHabilitarDiscoverableSePreciso();
        }
    };

    @Override
    void iniciaAtividadeBluetooth() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        // TODO titulo poderia passar como extra do intent
        modo = preferences.getString("modo", "P");
        layoutBotoesGerente.setVisibility(View.VISIBLE);
        btnIniciar.setOnClickListener(v -> {
            status = STATUS_EM_JOGO;
            iniciaTrucoActivitySePreciso();
        });
        btnInverter.setOnClickListener(v -> inverteAdversarios());
        btnTrocar.setOnClickListener(v -> trocaParceiro());
        registerReceiver(receiverMantemDiscoverable, new IntentFilter(
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        pedePraHabilitarDiscoverableSePreciso();
        if (!aguardandoDiscoverable) {
            iniciaThreads();
        }
    }

    private void pedePraHabilitarDiscoverableSePreciso() {
        if (aguardandoDiscoverable
                || status == STATUS_EM_JOGO
                || btAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            return;
        }
        aguardandoDiscoverable = true;
        Intent discoverableIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(
                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_DISCOVERY) {
            aguardandoDiscoverable = false;
            if (resultCode == RESULT_CANCELED) {
                // Sem discoverable, sem servidor
                finish();
            } else {
                iniciaThreads();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiverMantemDiscoverable);
        } catch (IllegalArgumentException e) {
            // Não tem API pra verificar se está registrado, então é o que tem pra hoje
            // cf. https://stackoverflow.com/a/3568906
            LOGGER.log(Level.INFO, "Activity destruída antes do receiver ser registrado");
        }
        encerraConexoes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Dá um tempinho para os clientes se prepararem antes de permitir
        // o início de uma nova partida
        btnIniciar.setEnabled(false);
        new Handler().postDelayed(() -> atualizaDisplay(), 4000);
    }

    public void run() {
        LOGGER.log(Level.INFO, "iniciou atividade server");
        atualizaDisplay();
        try {
            serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(
                    NOME_BT, UUID_BT);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Exceção ao tentar iniciar o listen no servidor", e);
            return;
        }
        while (status != STATUS_BLUETOOTH_ENCERRADO) {
            while (status == STATUS_EM_JOGO) {
                sleep(500);
            }
            atualizaClientes();
            if (status == STATUS_LOTADO) {
                setMensagem(null);
                sleep(1000);
                continue;
            }
            pedePraHabilitarDiscoverableSePreciso();
            setMensagem("Aguardando conexões...");
            // Se chegamos aqui, estamos fora de jogo e com vagas
            try {
                BluetoothSocket socket = serverSocket.accept();
                setMensagem(null);
                if (socket != null) {
                    int slot = encaixaEmUmSlot(socket);
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "Exceção em serverSocket.accept()", e);
            }
            if (isFinishing()) {
                status = STATUS_BLUETOOTH_ENCERRADO;
            }
        }
        encerraConexoes();
        LOGGER.log(Level.INFO, "finalizou atividade server");
    }

    /**
     * Desconecta jogador com versão incompatível e mostra mensagem de erro.
     *
     * @param jogador JogadorBluetooth que será desconectado
     */
    public void desconectaPorVersaoIncompativel(JogadorBluetooth jogador) {
        mostraAlertBox("Versão antiga",
                "O aparelho " + jogador.socket.getRemoteDevice().getName() +
                        " está rodando uma versão diferente do miniTruco " +
                        " e foi desconectado.\n\n" +
                        " Atualize o jogo em todos os celulares e tente novamente.");
        desconecta(jogador);
    }

    private void encerraConexoes() {
        status = STATUS_BLUETOOTH_ENCERRADO;
        for (int slot = 0; slot <= 2; slot++) {
            desconecta(jogadores[slot]);
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "Exceção em serverSocket.close()", e);
            }
        }
    }

    private void iniciaThreads() {
        if (threadAguardaConexoes == null) {
            threadAguardaConexoes = new Thread(this);
            threadAguardaConexoes.start();
        }
        if (threadMonitoraClientes == null) {
            threadMonitoraClientes = new Thread() {
                public void run() {
                    // Executa enquanto o servidor não for encerrado
                    while (status != STATUS_BLUETOOTH_ENCERRADO) {
                        // Envia um comando vazio (apenas para testar a conexão
                        // e processar qualquer desconexão que tenha ocorrido)
                        enviaLinha("");
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            // não precisa tratar
                        }
                    }
                }
            };
            threadMonitoraClientes.start();
        }
    }

    public void atualizaDisplay() {
        // Esse array é usado pelo display para mostrar os nomes dos jogadores
        // TODO: rever isso; de repente a gente deveria atualizar direto ou passar
        //       ele, ainda mais agora que isso só é usado no servidorbluetooth
        apelidos[0] = Jogador.sanitizaNome(btAdapter.getName());
        for(int i = 1; i <= 3; i++) {
            if (jogadores[i - 1] != null) {
                apelidos[i] = jogadores[i - 1].getNome();
            } else {
                apelidos[i] = APELIDO_BOT;
            }
        }
        runOnUiThread(() -> {
            for (int i = 0; i < 4; i++) {
                textViewsJogadores[i].setText(apelidos[i]);
            }
            if (modo != null) {
                textViewStatus.setText("Modo: " + Partida.textoModo(modo));
            }
            btnIniciar.setEnabled(getNumClientes() > 0);
            btnInverter.setEnabled(getNumClientes() > 0);
            btnTrocar.setEnabled(getNumClientes() > 0);
        });
    }

    @Override
    public int getNumClientes() {
        int numClientes = 0;
        for (int i = 0; i <= 2; i++) {
            if (jogadores[i] != null) {
                numClientes++;
            }
        }
        return numClientes;
    }

    void atualizaClientes() {
        // Vamos pegar os nomes do array vinculado ao display, então
        // precisamos atualizar ele antes
        atualizaDisplay();

        String comando = montaNotificacaoI(apelidos, modo, "BLT");
        // Envia a notificação para cada jogador (com sua posição)
        for (int i = 0; i <= 2; i++) {
            enviaLinha(i, comando.replace(POSICAO_PLACEHOLDER,
                Integer.toString(i + 2)));
        }
    }

    /**
     * Desconecta o jogador e remove ele da lista de jogadores.
     *
     * @param jogador jogador a desconectar
     */
    void desconecta(JogadorBluetooth jogador) {
        LOGGER.log(Level.INFO, "desconecta()");
        try {
            jogador.socket.close();
        } catch (Exception e) {
            // No prob, já deve ter morrido
        }
        for (int i = 0; i <= 2; i++) {
            if (jogadores[i] == jogador) {
                jogadores[i] = null;
            }
        }
        status = STATUS_AGUARDANDO;
        atualizaClientes();
    }

    public Partida criaNovaPartida(JogadorHumano jogadorHumano) {
        Partida partida = new PartidaLocal(false, false, modo);
        partida.adiciona(jogadorHumano);
        for (int i = 0; i <= 2; i++) {
            if (jogadores[i] != null) {
                partida.adiciona(jogadores[i]);
            } else {
                JogadorBot bot = new JogadorBot();
                bot.setFingeQuePensa(false);
                partida.adiciona(bot);
            }
        }
        this.partida = partida;
        return partida;
    }

    // Os métodos abaixo são synchronized para evitar trocas na mesa enquanto um
    // jogador está entrando no jogo (ou uma desconexão é descoberta por envio
    // de mensagem)

    @Override
    public synchronized void enviaLinha(int slot, String comando) {
        JogadorBluetooth jogador = jogadores[slot];
        if (jogador != null) {
            if (comando.length() > 0) {
                LOGGER.log(Level.INFO, "enviando comando " + comando
                        + " para slot " + slot);
            }
            try {
                jogador.out.write(comando.getBytes());
                jogador.out.write(SEPARADOR_ENV);
                jogador.out.flush();
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "exceção ao enviar mensagem", e);
                desconecta(jogador);
            }
        }
    }

    @Override
    public void enviaLinha(String linha) {
        for (int i = 0; i <= 2; i++) {
            enviaLinha(i, "");
        }
    }

    private synchronized int encaixaEmUmSlot(BluetoothSocket socket)
            throws IOException {
        for (int i = 0; i <= 2; i++) {
            if (jogadores[i] == null) {
                jogadores[i] = new JogadorBluetooth(socket, this);
                jogadores[i].setNome(Jogador.sanitizaNome(socket.getRemoteDevice().getName()));
                status = i == 2 ? STATUS_LOTADO : STATUS_AGUARDANDO;
                return i;
            }
        }
        return -1;
    }

    private synchronized void inverteAdversarios() {
        JogadorBluetooth temp = jogadores[0];
        jogadores[0] = jogadores[2];
        jogadores[2] = temp;
        atualizaClientes();
    }

    private synchronized void trocaParceiro() {
        JogadorBluetooth temp = jogadores[2];
        jogadores[2] = jogadores[1];
        jogadores[1] = jogadores[0];
        jogadores[0] = temp;
        atualizaClientes();
    }

}
