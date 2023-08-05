package me.chester.minitruco.android.multiplayer.internet;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.BuildConfig;
import me.chester.minitruco.R;
import me.chester.minitruco.android.CriadorDePartida;
import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.android.SalaActivity;
import me.chester.minitruco.android.TrucoActivity;
import me.chester.minitruco.android.multiplayer.PartidaRemota;
import me.chester.minitruco.core.Partida;

public class ClienteInternetActivity extends SalaActivity {

    private final static Logger LOGGER = Logger.getLogger("ClienteInternetActivity");

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private SharedPreferences preferences;

    private boolean contagemRegressivaParaIniciar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CriadorDePartida.setActivitySala(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        inicializaLayoutSala();
        findViewById(R.id.btnIniciar).setOnClickListener(v -> {
            solicitaInicioDeJogoConfirmandoSeTiverBots();
        });
        findViewById(R.id.btnInverter).setOnClickListener(v -> {
            enviaLinha("R I");
        });
        findViewById(R.id.btnTrocar).setOnClickListener(v -> {
            enviaLinha("R T");
        });

        conectaEIniciaProcessamentoDeNotificacoes();
    }

    private void solicitaInicioDeJogoConfirmandoSeTiverBots() {
        if (numJogadores == 4) {
            enviaLinha("Q");
        } else {
            new AlertDialog.Builder(this)
                .setTitle("Mesa não está cheia")
                .setMessage("Você prefere esperar mais gente, ou jogar com bots?")
                .setPositiveButton("Jogar", (dialog, which) -> {
                    enviaLinha("Q");
                })
                .setNegativeButton("Esperar", null)
                .show();
        }
    }

    private void conectaEIniciaProcessamentoDeNotificacoes() {
        setMensagem("Conectando...");
        new Thread(() -> {
            try {
                if (conecta()) {
                    while (!socket.isClosed()) {
                        processaNotificacoes();
                    }
                }
            } finally {
                desconecta();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CriadorDePartida.setActivitySala(this);
    }

    @Override
    public void onBackPressed() {
        desconecta();
        finish();
    }

    private boolean conecta() {
        String servidor = preferences.getBoolean("servidorLocal", false) ?
            this.getString(R.string.opcoes_default_servidor_local) :
            this.getString(R.string.opcoes_default_servidor);
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(servidor, 6912), 10_000);
            socket.setSoTimeout(20_000);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            // Envia a versão do app para o servidor ver se é compatível
            enviaLinha("B " + BuildConfig.VERSION_CODE);
            // Envia o nome que já foi sanitizado e salvo na TituloActivity
            enviaLinha("N " + preferences.getString(
                "nome_multiplayer", null));
        } catch (IOException e) {
            msgErroFatal("Falha na conexão", "Não foi possível conectar nos servidores do miniTruco. Tente novamente mais tarde.\n\nDetalhes: " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private void processaNotificacoes() {
        String line;
        try {
            line = in.readLine();
        } catch (IOException e) {
            return;
        }
        if (line == null) {
            desconecta();
            msgErroFatal("Conexão finalizada.");
            return;
        }
        if (!line.startsWith("K ")) {
            // Não loga o keepalive (evita poluir o log)
            LOGGER.log(Level.INFO, "recebeu: " + line);
            // Qualquer outra notificação cancela a contagem regressiva
            contagemRegressivaParaIniciar = false;
            setMensagem(null);
        }
        switch (line.charAt(0)) {
            case 'N': // Nome foi aceito
                // Já vamos entrar de cara numa sala pública (se a pessoa quiser
                // fazer outra coisa, ela usa o botão apropriado)
                enviaLinha("E PUB " + preferences.getString("modo", "P"));
                break;
            case 'I': // Entrou/voltou para uma sala (ou ela foi atualizada)
                exibeMesaForaDoJogo(line);
                atualizaStatusEContagemRegressiva();
                break;
            case 'X': // Erro tratável
                switch(line) {
                }
                break;
            case 'K': // Keepalive, apenas temos que devolver a notificação como comando
                enviaLinha(line);
                break;
            case '!': // Mensagem
                if (line.length() > 4 && line.startsWith("! T ")) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(),
                            line.substring(4),
                            Toast.LENGTH_LONG).show();
                    });
                }
                break;
            case 'P': // Partida iniciada
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
                // Se chegou aqui, não é nossa, encaminha pra PartidaRemota
                if (partida != null) {
                    partida.processaNotificacao(line.charAt(0),
                            line.length() > 2 ? line.substring(2) : "");
                }
        }
    }

    /**
     * Se a mesa estiver cheia, inicia contagem regressiva para auto-início
     * do jogo.
     * <p>
     * Observe que qualquer notificação (exceto o de keepalive) cancela a
     * contagem, e isso é feito no loop de processamento de notificações.
     * <p>
     * Em qualquer caso, garante que a mensagem de status reflita a situação.
     */
    private void atualizaStatusEContagemRegressiva() {
        runOnUiThread(() -> {
            switch (numJogadores) {
                case 1:
                    setMensagem("Aguardando outra pessoa entrar");
                    break;
                case 2:
                case 3:
                    setMensagem("Aguardando mais pessoas");
                    break;
                case 4:
                    // Auto-inicia se a sala estiver cheia (dando um tempo para
                    // o gerente organizar ou dar kick de jogadores)
                    contagemRegressivaParaIniciar = true;
                    new Thread(() -> {
                        for (int i = 10; i > 0; i--) {
                            setMensagem("Mesa completa. Auto-iniciando em " + i);
                            sleep(1000);
                            if (!contagemRegressivaParaIniciar) {
                                return;
                            }
                        }
                        if (isGerente) {
                            enviaLinha("Q");
                        }
                    }).start();
            }
        });
    }

    private void desconecta() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO log
            }
        }
    }

    @Override
    public Partida criaNovaPartida(JogadorHumano jogadorHumano) {
        enviaLinha("Q");
        partida = new PartidaRemota(this, jogadorHumano, posJogador, modo);
        return partida;
    }

    public void enviaLinha(String comando) {
        // Roda numa nova thread sempre, porque pode ser chamado por handlers da main thread
        // Não é a coisa mais otimizada do planeta, mas o custo é mínimo
        new Thread(() -> {
            out.write(comando);
            out.write('\n');
            out.flush();
            // Não loga o keepalive (evita poluir o log)
            if (!comando.startsWith("K ")) {
                LOGGER.log(Level.INFO, "enviou: " + comando);
            }
        }).start();
    }

    @Override
    public void enviaLinha(int slot, String linha) {
        throw new RuntimeException("ClienteInternet só tem uma conexão");
    }

    // TODO botão de back tem que
    //  - Sair da sala se estiver jogando
    //  - Fechar a activity se não estiver
    // TODO no destroy da activity, desconectar / encerrar a thread
}
