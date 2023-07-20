package me.chester.minitruco.android.multiplayer.internet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.regex.Pattern;

import me.chester.minitruco.R;
import me.chester.minitruco.android.BaseActivity;
import me.chester.minitruco.android.CriadorDePartida;
import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.android.TrucoActivity;
import me.chester.minitruco.android.multiplayer.Sala;
import me.chester.minitruco.android.multiplayer.PartidaRemota;
import me.chester.minitruco.core.Partida;

public class ClienteInternetActivity extends BaseActivity implements Sala<Activity> {

    private final static Logger LOGGER = Logger.getLogger("ClienteInternetActivity");

    public EditText editNomeJogador;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private SharedPreferences preferences;

    private PartidaRemota partida;
    private String modo;
    private int posJogador;
    private boolean humanoGerente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CriadorDePartida.setActivitySala(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.sala);
        findViewById(R.id.btnIniciarBluetooth).setOnClickListener(v -> {
            enviaLinha("Q");
        });


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
        String servidor = preferences.getString("servidor", this.getString(R.string.opcoes_default_servidor));
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(servidor, 6912), 10_000);
            socket.setSoTimeout(20_000);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
        } catch (IOException e) {
            msgErroFatal("Não foi possivel conectar.", e);
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
            msgErroFatal("Você foi desconectado.", null);
            return;
        }
        LOGGER.log(Level.INFO, "recebeu: " + line);
        switch (line.charAt(0)) {
            case 'W': // O servidor manda um W quando conecta
                // Envia o nome que já foi sanitizado e salvo na TituloActivity
                enviaLinha("N " + preferences.getString(
                    "nome_multiplayer", null));
                break;
            case 'N': // Nome foi aceito
                // Já vamos entrar de cara numa sala pública (se a pessoa quiser
                // fazer outra coisa, ela usa o botão apropriado)
                enviaLinha("E PUB " + preferences.getString("modo", "P"));
                break;
            case 'I': // Entrou numa sala (ou ela foi atualizada)
                runOnUiThread(() -> {
                    if (partida != null) {
                        partida.abandona(0);
                        partida = null;
                    }
                    String[] tokens = line.split(" ");
                    String[] nomes = tokens[1].split(Pattern.quote("|"));
                    posJogador = Integer.parseInt(tokens[2]);
                    // Ajusta os nomes para que o jogador local fique sempre na
                    // parte inferior da tela (textViewJogador1)
                    int p = (posJogador - 1) % 4;
                    ((TextView) findViewById(R.id.textViewJogador1)).setText(nomes[p]);
                    p = (p + 1) % 4;
                    ((TextView) findViewById(R.id.textViewJogador2)).setText(nomes[p]);
                    p = (p + 1) % 4;
                    ((TextView) findViewById(R.id.textViewJogador3)).setText(nomes[p]);
                    p = (p + 1) % 4;
                    ((TextView) findViewById(R.id.textViewJogador4)).setText(nomes[p]);
                    modo = tokens[3];
                    int posGerente = Integer.parseInt(tokens[5]);
                    humanoGerente = (posGerente == posJogador);
                    findViewById(R.id.layoutIniciar).setVisibility(
                        humanoGerente ? View.VISIBLE : View.GONE);
                });
                break;
            case 'X': // Erro tratável
                switch(line) {
                }
                break;
            case 'K': // Keepalive, apenas temos que devolver a notificação como comando
                enviaLinha(line);
                break;
            case 'P': // Partida iniciada
                // Se for a primeira partida nessa sala, temos que abrir a activity
                while (!TrucoActivity.isViva()) {
                    startActivity(
                        new Intent(this, TrucoActivity.class)
                            .putExtra("multiplayer", true));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        // TODO consolidar esses sleeps
                    }
                }
                // TODO checar se não tem chance de ficar preso no while acima
                //      (acho que não , mas ainda teve uma vez que a partida iniciou de bobeira)

                // Não tem break mesmo, porque se *não* for a primeira partida, temos que
                // deixar a activity encerrar (visualmente) a partida anterior.
            default:
                // Se chegou aqui, não é nossa, encaminha pra PartidaRemota
                if (partida != null) {
                    partida.processaNotificacao(line.charAt(0),
                            line.length() > 2 ? line.substring(2) : "");
                }
        }
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
        partida = new PartidaRemota(this, jogadorHumano, posJogador, modo);
        partida.setHumanoGerente(humanoGerente);
        return partida;
    }

    private void msgErroFatal(String msg, Throwable e) {
        runOnUiThread(() -> {
            encerraTrucoActivity();
            if (isFinishing()) {
                return;
            }
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle("Erro")
                    .setMessage(e == null ? msg : msg + "\n\nDetalhes: " + e.getLocalizedMessage())
                    .setNeutralButton("Fechar", (dialog, which) -> finish())
                    .setOnCancelListener(v -> finish())
                    .show();
        });
    }

    public void enviaLinha(String comando) {
        // Roda numa nova thread sempre, porque pode ser chamado por handlers da main thread
        // Não é a coisa mais otimizada do planeta, mas o custo é mínimo
        new Thread(() -> {
            out.write(comando);
            out.write('\n');
            out.flush();
            LOGGER.log(Level.INFO, "enviou: " + comando);
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
