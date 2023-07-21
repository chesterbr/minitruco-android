package me.chester.minitruco.android.multiplayer.bluetooth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.BuildConfig;
import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.android.TrucoActivity;
import me.chester.minitruco.android.multiplayer.PartidaRemota;
import me.chester.minitruco.core.Partida;


/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

@SuppressLint("MissingPermission") // super.onCreate checa as permissões
public class ClienteBluetoothActivity extends BluetoothActivity implements
        Runnable {

    private final static Logger LOGGER = Logger.getLogger("ClienteBluetoothActivity");

    private static final int REQUEST_ENABLE_BT = 1;

    private List<BluetoothDevice> dispositivosPareados;
    private BluetoothDevice servidor;

    private Thread threadConexao;
    private Thread threadMonitoraConexao;
    private PartidaRemota partida;
    private BluetoothSocket socket = null;
    private InputStream in;
    private OutputStream out;
    private int posJogador;

    @Override
    void iniciaAtividadeBluetooth() {
        listaDispositivosPareados();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dispositivosPareados != null) {
            dispositivosPareados.clear();
        }
        finalizaThreadFechandoConexoes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                // Sem bluetooth, sem cliente
                finish();
            } else {
                listaDispositivosPareados();
            }
        }
    }

    public void run() {
        if (!conectaNoServidor()) {
            return;
        }
        atualizaDisplay();

        if (socket == null) {
            msgErroFatal("Jogo não encontrado. Veja se o seu aparelho está pareado/autorizado com o que criou o jogo e tente novamente.");
            return;
        }
        sleep(500);
        setMensagem(null);
        iniciaMonitorConexao();
        // Loop principal: decodifica as notificações recebidas e as
        // processa (ou encaminha ao JogoBT, se estivermos em jogo)
        int c;
        StringBuilder sbLinha = new StringBuilder();
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            enviaLinha("B " + (BuildConfig.VERSION_CODE));
            while ((c = in.read()) != -1) {
                if (c == SEPARADOR_REC) {
                    if (sbLinha.length() > 0) {
                        LOGGER.log(Level.INFO, "Recebeu:" + sbLinha);
                        char tipoNotificacao = sbLinha.charAt(0);
                        String parametros = sbLinha.delete(0, 2).toString();
                        switch (tipoNotificacao) {
                        case 'I':
                            exibeMesaForaDoJogo(parametros);
                            break;
                        case 'P':
                            iniciaTrucoActivitySePreciso();
                            // Enquanto não tiver a activity iniciada, melhor não processar
                            // nenhuma mensagem
                            while (!TrucoActivity.isViva()) {
                                sleep(100);
                            }
                            // Não tem mesmo um break aqui, o início de partida
                            // também precisa ser processado pelo jogo anterior
                            // (para limpar o placar)
                        default:
                            if (partida != null) {
                                partida.processaNotificacao(tipoNotificacao,
                                        parametros);
                            }
                        }
                        sbLinha.setLength(0);
                    }
                } else {
                    sbLinha.append((char) c);
                }
            }
        } catch (IOException e) {
            if (!isFinishing()) {
                if (partida != null) {
                    partida.abandona(0);
                }
                LOGGER.log(Level.INFO, "desconectado");
                msgErroFatal("Você foi desconectado");
            }
        }
    }

    private void iniciaMonitorConexao() {
        if (threadMonitoraConexao == null) {
            threadMonitoraConexao = new Thread() {
                public void run() {
                    while (threadConexao.isAlive()) {
                        // Envia comando vazio, apenas para garantir desbloqueio
                        // de I/O na thread principal se o servidor sumir
                        for (int i = 0; i <= 2; i++) {
                            enviaLinha("");
                        }
                        ClienteBluetoothActivity.this.sleep(2000);
                    }
                }
            };
            threadMonitoraConexao.start();
        }
    }

    private void exibeMesaForaDoJogo(String parametros) {
        encerraTrucoActivity();
        if (partida != null) {
            partida.abandona(0);
            partida = null;
        }
        // Exibe as informações recebidas fora do jogo
        String[] tokens = parametros.split(" ");
        posJogador = Integer.parseInt(tokens[2]);
        modo = tokens[1];
        if (modo.length() != 1) {
            msgErroFatal("O celular que criou o jogo está com uma versão muito antiga do miniTruco. Peça para atualizar e tente novamente.");
            return;
        }
        encaixaApelidosNaMesa(tokens[0].split("\\|"));
        atualizaDisplay();
    }

    private void encaixaApelidosNaMesa(String[] apelidosOriginais) {
        for (int n = 1; n <= 4; n++) {
            apelidos[getPosicaoMesa(n) - 1] = apelidosOriginais[n - 1];
        }
    }

    @Override
    public int getNumClientes() {
        return 0;
    }

    /**
     * Manda um comando para o celular do servidor (se houver um conectado).
     */
    @Override
    public synchronized void enviaLinha(String linha) {
        try {
            if (out == null) {
                return;
            }
            if (linha.length() > 0) {
                LOGGER.log(Level.INFO, "Enviando:" + linha);
            }
            out.write(linha.getBytes());
            out.write(SEPARADOR_ENV);
            out.flush();
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Exceção em EnviaLinha (desconexão?)", e);
            try {
                socket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void enviaLinha(int slot, String linha) {
        throw new RuntimeException("ClienteBluetooth só tem uma conexão");
    }

    /**
     * Recupera a posição "visual" correspondente a uma posição de jogo (i.e.,
     * uma posição no servidor)
     * <p>
     * A idéia é que o jogador local fique sempre na parte inferior da tela,
     * então o método retorna 1 para o jogador local, 2 para quem está à direita
     * dele, etc.
     *
     * @param i
     *            posição (no servidor) do jogador que queremos consultar
     */
    public int getPosicaoMesa(int i) {
        int retorno = i - posJogador + 1;
        if (retorno < 1)
            retorno += 4;
        return retorno;
    }

    private void finalizaThreadFechandoConexoes() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public Partida criaNovaPartida(JogadorHumano jogadorHumano) {
        partida = new PartidaRemota(this, jogadorHumano, posJogador, modo);
        return partida;
    }

    /**
     * Verifica se o bluetooh está ativo e caso não esteja pede para habilitar
     *
     * @return true caso o blutooh esteja ativo
     */
    private boolean habilitaBluetooh() {
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    private boolean conectaNoServidor() {
        String nomeDoServidor = servidor.getName();
        try {
            LOGGER.log(Level.INFO, "Criando socket");
            LOGGER.log(Level.INFO, "device.getName()");
            setMensagem("Conectando em " + nomeDoServidor);
            socket = servidor.createRfcommSocketToServiceRecord(UUID_BT);
            sleep(1000);
            LOGGER.log(Level.INFO, "Conectando");
            socket.connect();
            LOGGER.log(Level.INFO, "Conectado");
            setMensagem("Conectado!");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.INFO,
                    "Falhou conexao com " + nomeDoServidor, e);
            msgErroFatal("Não foi possível conectar com " + nomeDoServidor + ". Veja se o o seu aparelho está pareado/autorizado com ele e tente novamente.");
            try {
                socket.close();
            } catch (Exception e1) {
                // Sem problemas, era só pra garantir
            }
            return false;
        }
    }

    /**
     * Cria um array somente com os nomes dos aparelhos que serão apresentados na lista
     *
     * @return array com o nomes dos aparelhos pareados
     */
    private CharSequence[] criaArrayComNomeDosAparelhosPareados() {
        // monta um array com o nome dos possíveis servidores
        CharSequence[] serverNameArray = new String[dispositivosPareados.size()];
        int i = 0;
        for (BluetoothDevice device : dispositivosPareados) {
            serverNameArray[i] = device.getName();
            i++;
        }

        return serverNameArray;
    }

    /**
     * Mostra um dialog com uma lista com os aparelhos pareados.
     * Caso o bluetooth não esteja habilidado, dispara o dialog que permite habilitar.
     * Caso não tenha nenhum aparelho pareado, mostra uma mensagem de erro.
     */
    private void listaDispositivosPareados() {

        setMensagem(null);

        if (!habilitaBluetooh()) {
            return;
        }

        dispositivosPareados = new ArrayList<>();
        dispositivosPareados.addAll(btAdapter.getBondedDevices());

        if (dispositivosPareados.size() == 0) {
            msgErroFatal("Não existem aparelhos pareados. Veja se o seu aparelho está pareado/autorizado com o que criou o jogo e tente novamente.");
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Escolha o celular que criou o jogo")
            .setItems(criaArrayComNomeDosAparelhosPareados(), (dialog, posicaoNaLista) -> {
                servidor = dispositivosPareados.get(posicaoNaLista);
                threadConexao = new Thread(ClienteBluetoothActivity.this);
                threadConexao.start();
            })
            .setOnCancelListener(dialog -> finish())
            .show();
    }

}
