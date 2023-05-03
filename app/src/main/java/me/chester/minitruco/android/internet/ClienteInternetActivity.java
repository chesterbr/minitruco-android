package me.chester.minitruco.android.internet;

import static android.provider.Settings.Global.DEVICE_NAME;
import static android.text.InputType.TYPE_CLASS_TEXT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import me.chester.minitruco.R;

public class ClienteInternetActivity extends Activity {

    public EditText editNomeJogador;
    private Socket socket;
    private Thread thread;
    private PrintWriter out;
    private BufferedReader in;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.internet_conectando);

        thread = new Thread(() -> {
            try {
                if (conecta()) {
                    while (!socket.isClosed()) {
                        processaNotificacoes();
                    }
                }
            } finally {
                desconecta();
            }
        });
        thread.start();
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
            msgErroFatal("Erro de comunicação", e);
            return;
        }
        if (line == null) { // Servidor desconectou
            msgErroFatal("Você foi desconectado", null);
            desconecta();
            return;
        }
        if (line.length() == 0) { // O servidor manda linhas vazias periodicamente para fins de keepalive
            return;
        }
        switch (line.charAt(0)) {
            case 'W': // O servidor manda um W quando conecta
                pedeNome();
                break;
            case 'N': // Nome foi aceito
                runOnUiThread(() -> {
                    setContentView(R.layout.internet_menu);
                    ((TextView) findViewById(R.id.textViewInternetTitulo)).setText(
                        "Conectado como " + editNomeJogador.getText() + ". Regras:"
                    );
                    ((TextView) findViewById(R.id.textViewInternetRegras)).setText(
                        "TODO mostrar as regras aqui"
                    );
                });
                break;
            case 'X': // Erro tratável
                switch(line) {
                    case "X NE": // Nome já existe
                        pedeNome();
                        break;
                }
                break;
            default:
                // TODO log? talvez só ignorar (aí nem precisa tratar o keepalive)
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



    private void msgErroFatal(String msg, Throwable e) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle("Erro")
                    .setMessage(e == null ? msg : msg + "\n\nErro: " + e.getLocalizedMessage())
                    .setNeutralButton("Fechar", (dialog, which) -> finish())
                    .setOnCancelListener(v -> finish())
                    .show();
        });
    }


    public void enviaComando(String comando) {
        // Roda numa nova thread sempre, porque pode ser chamado por handlers da main thread
        // Não é a coisa mais otimizada do planeta, mas o custo é mínimo
        new Thread(() -> {
            out.write(comando);
            out.write('\n');
            out.flush();
            // TODO log
//			Jogo.log(comando);
        }).start();
    }

    private void pedeNome() {
        String nome = null;
        String mensagem;
        if (editNomeJogador == null) {
            mensagem = "Qual nome você gostaria de usar?";
            // TODO armazenar último nome usado e recuperar aqui
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                nome = Settings.System.getString(getContentResolver(), DEVICE_NAME);
            }
            if (nome == null) {
                // Não-documentado e só funciona se tiver Bluetooth, cf https://stackoverflow.com/a/67949517/64635
                nome = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
            }
            if (nome == null) {
                nome = "um nome aleatório";
            }

        } else {
            mensagem = "Nome já usado ou inválido, tente outro:";
            nome = editNomeJogador.getText().toString() + (int)(1 + Math.random() * 99);
        }
        editNomeJogador = new EditText(this);
        editNomeJogador.setInputType(TYPE_CLASS_TEXT);
        editNomeJogador.setMaxLines(1);
        editNomeJogador.setText(nome);

        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.icon)
                    .setTitle("Nome")
                    .setMessage(mensagem)
                    .setView(editNomeJogador)
                    .setPositiveButton("Ok", (dialog, which) -> defineNome())
                    .setNegativeButton("Cancela", null) // TODO desconectar
                    .show();
        });
    }

    private void defineNome() {
        enviaComando("N " + editNomeJogador.getText().toString());
        // TODO trocar pra logger?
        Log.d("Internet", editNomeJogador.getText().toString());
    }


    // TODO botão de back tem que
    //  - Sair da sala se estiver jogando
    //  - Fechar a activity se não estiver
    // TODO no destroy da activity, desconectar / encerrar a thread
}
