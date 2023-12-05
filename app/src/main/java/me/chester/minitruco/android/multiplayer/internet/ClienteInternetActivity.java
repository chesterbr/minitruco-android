package me.chester.minitruco.android.multiplayer.internet;

import static android.text.InputType.TYPE_CLASS_NUMBER;

import static me.chester.minitruco.android.PreferenceUtils.getLetraDoModo;
import static me.chester.minitruco.android.PreferenceUtils.getServidor;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
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
import me.chester.minitruco.android.CriadorDePartida;
import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.android.SalaActivity;
import me.chester.minitruco.android.TrucoActivity;
import me.chester.minitruco.android.multiplayer.PartidaRemota;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Partida;

public class ClienteInternetActivity extends SalaActivity {

    private final static Logger LOGGER = Logger.getLogger("ClienteInternetActivity");

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private SharedPreferences preferences;

    private AlphaAnimation animationTrocaSala;
    private String comandoTrocaSala;

    private boolean contagemRegressivaParaIniciar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CriadorDePartida.setActivitySala(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        inicializaLayoutSala();
        configuraBotoes();
        conectaEIniciaProcessamentoDeNotificacoes();
    }

    private void configuraBotoes() {
        animationTrocaSala = new AlphaAnimation(1.0f, 0.0f);
        animationTrocaSala.setDuration(500);
        animationTrocaSala.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                enviaLinha(comandoTrocaSala);
            }
            public void onAnimationStart(Animation animation) { }
            public void onAnimationRepeat(Animation animation) { }
        });

        btnIniciar.setOnClickListener(v -> {
            solicitaInicioDeJogoConfirmandoSeTiverBots();
        });
        btnInverter.setOnClickListener(v -> {
            enviaLinha("R I");
        });
        btnTrocar.setOnClickListener(v -> {
            enviaLinha("R T");
        });
        btnNovaSalaPrivada.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setMessage("Salas privadas permitem convidar amigos(as), mas não " +
                    "permitem que outras pessoas entrem. Se está procurando companhia " +
                    "para jogar, é melhor ficar na sala pública.")
                .setPositiveButton("Criar sala privada", (d, w) -> {
                    comandoTrocaSala = "E PRI " + modo;
                    layoutJogadoresEBotoesGerente.startAnimation(animationTrocaSala);
                })
                .setNegativeButton("Ficar aqui", null)
                .show();
        });
        btnNovaSalaPublica.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setMessage("Trocar de sala só é recomendado se alguém estiver " +
                    "incomodando, pois vai demorar mais pra achar outras pessoas " +
                    "para jogar. Quer mesmo trocar?")
                .setPositiveButton("Trocar", (d, w) -> {
                    comandoTrocaSala = "E NPU " + modo;
                    layoutJogadoresEBotoesGerente.startAnimation(animationTrocaSala);
                })
                .setNegativeButton("Ficar aqui", null)
                .show();
        });
        btnEntrarComCodigo.setOnClickListener(v -> {
            // Faz a pergunta sugerindo o nome encontrado
            EditText editCodigo = new EditText(this);
            editCodigo.setInputType(TYPE_CLASS_NUMBER);
            editCodigo.setMaxLines(1);
            editCodigo.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { }
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) { }
                public void afterTextChanged(Editable editable) {
                    if (editable.length() > 5) {
                        editCodigo.setText(editable.subSequence(0, 5));
                        editCodigo.setSelection(5);
                    }
                }
            });
            new AlertDialog.Builder(this)
                .setTitle("Código da sala")
                .setMessage("Digite o código de 5 dígitos passado pela pessoa que convidou você:")
                .setView(editCodigo)
                .setPositiveButton("Ok", (d, w) -> {
                    if (editCodigo.getText().toString().length() != 5) {
                        Toast.makeText(this, "Código tem que ter 5 dígitos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    comandoTrocaSala = "E PRI-" + editCodigo.getText().toString();
                    layoutJogadoresEBotoesGerente.startAnimation(animationTrocaSala);
                })
                .setNegativeButton("Cancela", null)
                .show();
        });
    }

    /**
     * Sanitiza o código da sala (remove espaços, deixa tudo maiúsculo e limita
     * o tamanho; é mais pra evitar erros de digitação e abusos)
     */
    private String sanitiza(String codigo) {
        return Jogador.sanitizaNome(codigo).toUpperCase();
    }

    private void solicitaInicioDeJogoConfirmandoSeTiverBots() {
        if (numJogadores == 4) {
            enviaLinha("Q");
        } else {
            new AlertDialog.Builder(this)
                .setTitle("Mesa não está cheia")
                .setMessage("Você prefere chamar/esperar mais gente, ou jogar com bots na mesa?")
                .setPositiveButton("Jogar", (dialog, which) -> {
                    enviaLinha("Q");
                })
                .setNegativeButton("Chamar/esperar", null)
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
        String servidor = getServidor(this);
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
            msgErroFatal("Conexão perdida.");
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
                enviaLinha("E PUB " + getLetraDoModo(this));
                break;
            case 'I': // Entrou/voltou para uma sala (ou ela foi atualizada)
                exibeMesaForaDoJogo(line);
                iniciaContagemRegressivaSeNecessario();
                break;
            case 'X': // Erro
                switch(line) {
                    case "X SI":
                        erroFatalSalaInvalida();
                        break;
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

    private void erroFatalSalaInvalida() {
        String codigo = comandoTrocaSala.length() > 6 ?
            comandoTrocaSala.substring(6) : "";
        msgErroFatal("Erro", "A sala " +
            "privada com o código " + codigo + " não está aberta. " +
            "Ela pode estar lotada ou com jogo em andamento, ou " +
            "ainda, o código pode estar errado. Confira com a pessoa que " +
            "te convidou e tente novamente.");
    }

    /**
     * Se estivermos numa sala pública e a mesa estiver cheia, inicia contagem
     * regressiva para auto-início do jogo.
     * <p>
     * Observe que qualquer notificação (exceto o de keepalive) cancela a
     * contagem, e isso é feito no loop de processamento de notificações.
     */
    private void iniciaContagemRegressivaSeNecessario() {
        runOnUiThread(() -> {
            if (numJogadores == 4 && tipoSala.equals("PUB")) {
                contagemRegressivaParaIniciar = true;
                new Thread(() -> {
                    for (int i = 5; i > 0; i--) {
                        setMensagem("Mesa completa. Auto-iniciando em " + i);
                        sleep(1000);
                        if (!contagemRegressivaParaIniciar) {
                            return;
                        }
                    }
                    // Do ponto de vista da UI, salas públicas não têm gerente,
                    // mas o servidor ainda espera que ele(a) inicie o jogo,
                    // então...
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
