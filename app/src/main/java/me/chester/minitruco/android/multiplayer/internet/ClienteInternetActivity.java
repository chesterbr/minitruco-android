package me.chester.minitruco.android.multiplayer.internet;

import static android.provider.Settings.Global.DEVICE_NAME;
import static android.text.InputType.TYPE_CLASS_TEXT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import me.chester.minitruco.R;
import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.android.TrucoActivity;
import me.chester.minitruco.android.multiplayer.ClienteMultiplayer;
import me.chester.minitruco.android.multiplayer.JogoRemoto;
import me.chester.minitruco.core.Jogo;

public class ClienteInternetActivity extends Activity implements ClienteMultiplayer {

    private final static Logger LOGGER = Logger.getLogger("ClienteInternetActivity");

    public EditText editNomeJogador;
    private Socket socket;
    private Thread thread;
    private PrintWriter out;
    private BufferedReader in;
    private SharedPreferences preferences;

	private static ClienteInternetActivity currentInstance;
    private JogoRemoto jogo;
    private String modo;
    private int posJogador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentInstance = this;

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
            msgErroFatal("Erro de comunicação.", e);
            return;
        }
        if (line == null) {
            desconecta();
            msgErroFatal("Você foi desconectado.", null);
            return;
        }
        if (line.length() == 0) { // O servidor manda linhas vazias periodicamente para fins de keepalive
            return;
        }
        LOGGER.log(Level.INFO, "recebeu: " + line);
        switch (line.charAt(0)) {
            case 'W': // O servidor manda um W quando conecta
                pedeNome();
                break;
            case 'N': // Nome foi aceito
                runOnUiThread(() -> {
                    setContentView(R.layout.internet_menu);
                    findViewById(R.id.btnEntrarSalaPublica).setOnClickListener(v -> {
                        // TODO pegar as regras das preferências
                        enviaLinha("E PUB FFF");
                    });
                    ((TextView) findViewById(R.id.textViewInternetTitulo)).setText(
                        "Conectado como " + line.substring(2) + ". Regras:"
                    );
                    ((TextView) findViewById(R.id.textViewInternetRegras)).setText(
                        "TODO mostrar as regras aqui"
                    );
                });
                break;
            case 'I': // Entrou numa sala (ou ela foi atualizada)
                runOnUiThread(() -> {
                    setContentView(R.layout.internet_sala);
                    findViewById(R.id.btnQueroJogar).setOnClickListener(v -> {
                        enviaLinha("Q");
                    });
                    if (jogo != null) {
                        jogo.abortaJogo(0);
                        jogo = null;
                    }
            		String[] tokens = line.split(" ");
                    String[] nomes = tokens[1].split(Pattern.quote("|"));
                    ((TextView) findViewById(R.id.textViewJogador1)).setText(nomes[0]);
                    ((TextView) findViewById(R.id.textViewJogador2)).setText(nomes[1]);
                    ((TextView) findViewById(R.id.textViewJogador3)).setText(nomes[2]);
                    ((TextView) findViewById(R.id.textViewJogador4)).setText(nomes[3]);
		            posJogador = Integer.parseInt(tokens[2]);
		            modo = tokens[3];
                });
                break;
            case 'X': // Erro tratável
                switch(line) {
                    case "X NE": // Nome já existe
                        pedeNome();
                        break;
                    case "X NI":
                        // TODO sinalizar como invalido, tambem fazer
                        // algo parecido no NE acima
                        pedeNome();
                        break;
                }
                break;
            case 'P': // Jogo iniciado
                // Se for o primeiro jogo nessa sala, temos que abrir a activity
                while (!TrucoActivity.isViva()) {
                    startActivity(
                        new Intent(this, TrucoActivity.class)
                            .putExtra("clienteInternet", true));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        // TODO consolidar esses sleeps
                    }
                }
                // TODO checar se não tem chance de ficar preso no while acima
                //      (acho que não , mas ainda teve uma vez que o jogo iniciou de bobeira)

                // Não tem break mesmo, porque se *não* for o primeiro jogo, temos que
                // deixar a activity encerrar (visualmente) o jogo anterior.
            default:
                // Se chegou aqui, não é nossa, encaminha pro JogoRemoto
                if (jogo != null) {
                    jogo.processaNotificacao(line.charAt(0),
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

	public static Jogo criaNovoJogo(JogadorHumano jogadorHumano) {
		return currentInstance._criaNovoJogo(jogadorHumano);
	}

	public Jogo _criaNovoJogo(JogadorHumano jogadorHumano) {
		jogo = new JogoRemoto(this, jogadorHumano, posJogador, modo);
		return jogo;
	}

    private void msgErroFatal(String msg, Throwable e) {
        runOnUiThread(() -> {
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
        enviaLinha("N " + editNomeJogador.getText().toString());
    }


    // TODO botão de back tem que
    //  - Sair da sala se estiver jogando
    //  - Fechar a activity se não estiver
    // TODO no destroy da activity, desconectar / encerrar a thread
}
