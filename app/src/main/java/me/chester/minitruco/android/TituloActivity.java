package me.chester.minitruco.android;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.provider.Settings.Global.DEVICE_NAME;
import static me.chester.minitruco.android.PreferenceUtils.getLetraDoModo;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;

import me.chester.minitruco.BuildConfig;
import me.chester.minitruco.R;
import me.chester.minitruco.android.multiplayer.bluetooth.ClienteBluetoothActivity;
import me.chester.minitruco.android.multiplayer.bluetooth.ServidorBluetoothActivity;
import me.chester.minitruco.android.multiplayer.internet.ClienteInternetActivity;
import me.chester.minitruco.android.multiplayer.internet.InternetUtils;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.Partida;
import me.chester.minitruco.core.PartidaLocal;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Tela inicial do jogo. Permite mudar opções, inciar uma partida single-player
 * (atuando com a sua "sala") ou iniciar uma partida multiplayer.
 */
public class TituloActivity extends SalaActivity {

    SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.titulo);

        ((TextView) findViewById(R.id.versao_app)).setText("versão " + BuildConfig.VERSION_NAME);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        configuraBotoesMultiplayer();
        mostraNotificacaoInicial();
        migraOpcoesLegadas();

        TextView textViewLinkContato = findViewById(R.id.textViewLinkContato);
        SpannableString textoLinkContato = new SpannableString(textViewLinkContato.getText());
        textoLinkContato.setSpan(new UnderlineSpan(), 0, textoLinkContato.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textViewLinkContato.setText(textoLinkContato);
        textViewLinkContato.setOnClickListener(v -> {
            String facebookUrl = "https://www.facebook.com/profile.php?id=61550014616104";
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=" + facebookUrl));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl));
                startActivity(intent);
            }
        });

        findViewById(R.id.btnAjuda).setOnClickListener(v -> {
            mostraAlertBox(this.getString(R.string.titulo_instrucoes),
                this.getString(R.string.texto_instrucoes));
        });

        findViewById(R.id.btnSobre).setOnClickListener(v -> {
            int partidas = preferences.getInt("statPartidas", 0);
            String versao;
            try {
                versao = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            String stats_versao = "Esta é a versão " + versao
                + " do jogo. Você já iniciou " + partidas
                + " partidas (locais ou multiplayer).<br/><br/>";
            mostraAlertBox(this.getString(R.string.titulo_sobre), stats_versao
                + this.getString(R.string.texto_sobre));
        });
        selecionaModo(getLetraDoModo(this));
    }

    /**
     * Atualiza opções que mudaram com o tempo
     */
    private void migraOpcoesLegadas() {
        if (preferences.contains("velocidadeAnimacao")) {
            String velocidadeAnimacao = preferences.getString("velocidadeAnimacao", "1");
            preferences.edit()
                .putBoolean("animacaoRapida", velocidadeAnimacao.equals("4"))
                .remove("velocidadeAnimacao")
                .apply();

        }
        if (preferences.contains("baralhoLimpo")) {
            boolean tentoMineiro = preferences.getBoolean("tentoMineiro", false);
            boolean manilhaVelha = preferences.getBoolean("manilhaVelha", false);
            boolean baralhoLimpo = preferences.getBoolean("baralhoLimpo", false);
            String modo;
            if (tentoMineiro && manilhaVelha) {
                modo = "M";
            } else if (baralhoLimpo) {
                modo = "L";
            } else {
                modo = "P";
            }
            preferences.edit()
                .putString("modo", modo)
                .remove("tentoMineiro")
                .remove("baralhoLimpo")
                .remove("manilhaVelha")
                .apply();
        }
    }

    /**
     * Na primeira vez que a app roda, mostra as instruções.
     * Na primeira vez em que roda uma nova versão (sem ser a 1a. vez geral),
     * mostra as novidades desta versão.
     */
    private void mostraNotificacaoInicial() {
        boolean mostraInstrucoes = preferences.getBoolean("mostraInstrucoes",
            true);
        String versaoQueMostrouNovidades = preferences.getString("versaoQueMostrouNovidades", "");
        String versaoAtual = BuildConfig.VERSION_NAME;

        if (mostraInstrucoes) {
            mostraAlertBox(this.getString(R.string.titulo_instrucoes), this.getString(R.string.texto_instrucoes));
        } else if (!versaoQueMostrouNovidades.equals(versaoAtual)) {
            mostraAlertBox("Novidades", this.getString(R.string.novidades));
        } else {
            promoveJogoInternet(false);
        }

        Editor e = preferences.edit();
        e.putBoolean("mostraInstrucoes", false);
        e.putString("versaoQueMostrouNovidades", versaoAtual);
        e.apply();

    }

    private void configuraBotoesMultiplayer() {
        boolean temBluetooth = BluetoothAdapter.getDefaultAdapter() != null;
        boolean temInternet = true;

        Button btnBluetooth = findViewById(R.id.btnBluetooth);
        Button btnInternet = findViewById(R.id.btnInternet);
        btnBluetooth.setVisibility(temBluetooth ? View.VISIBLE : View.GONE);
        btnInternet.setVisibility(temInternet ? View.VISIBLE : View.GONE);
        if (temBluetooth) {
            btnBluetooth.setOnClickListener(v -> {
                perguntaCriarOuProcurarBluetooth();
            });
        }
        if (temInternet) {
            btnInternet.setOnClickListener(v -> {
                pedeNomeEConecta();
            });
        }
    }

    private void pedeNomeEConecta() {
        if (conectadoNaInternet()) {
            pedeNome((nome) -> {
                startActivity(new Intent(getBaseContext(),
                    ClienteInternetActivity.class));
            });
        } else {
            mostraAlertBox("Sem conexão",
                "Não foi possível conectar à Internet. Verifique sua conexão e tente novamente.");
        }
    }

    private boolean conectadoNaInternet() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                return cm.getNetworkCapabilities(cm.getActiveNetwork()).hasCapability(NET_CAPABILITY_INTERNET);
            } else {
                // Android < 6, vamos assumir que tem internet
                // (se não conectar só vai vir uma mensagem feia mesmo)
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void pedeNome(Consumer<String> callback) {
        // Se já temos um nome guardado, é ele
        String nome = preferences.getString("nome_multiplayer", null);
        // Senão, tentamos pegar o nome do dispositivo
        if (nome == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            nome = Settings.System.getString(getContentResolver(), DEVICE_NAME);
        }
        // Se não deu certo, tentamos pegar o nome Bluetooth
        if (nome == null) {
            try {
                // Não-documentado e só funciona se tiver Bluetooth, cf https://stackoverflow.com/a/67949517/64635
                nome = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
            } catch (Exception e) {
                // não se preocupa com exceções aqui (a Play Store tava "ain
                // você não pode ler esse setting na versão xyz do Android")
            }
        }
        // Se nada disso deu certo, o sanitizador coloca o nome default
        nome = Jogador.sanitizaNome(nome);

        // Se o usuário já disse que não quer perguntar, usa o nome que tem
        if (!preferences.getBoolean("semprePerguntarNome", true)) {
            callback.accept(nome);
            return;
        }

        // Faz a pergunta sugerindo o nome encontrado
        // (mostrando os _ como espaços; a sanitização vai trocar de volta)
        View viewConteudo = getLayoutInflater()
            .inflate(R.layout.dialog_nome_jogador, null);
        final CheckBox checkBoxPerguntarSempre = viewConteudo
            .findViewById(R.id.checkBoxSemprePerguntaNome);
        final EditText editTextNomeJogador = viewConteudo
            .findViewById(R.id.editTextNomeJogador);
        editTextNomeJogador.setText(nome.replaceAll("_", " "));

        runOnUiThread(() -> {
            AlertDialog dialogNome = new AlertDialog.Builder(this)
                    .setTitle("Nome")
                    .setMessage("Qual nome você gostaria de usar?")
                    .setView(viewConteudo)
                    .setPositiveButton("Ok", (d, w) -> {
                        if (!checkBoxPerguntarSempre.isChecked()) {
                            preferences.edit().putBoolean("semprePerguntarNome", false).apply();
                        }
                        final String nomeFinal = Jogador.sanitizaNome(
                            editTextNomeJogador.getText().toString());
                        preferences.edit().putString("nome_multiplayer",
                            nomeFinal).apply();
                        callback.accept(nomeFinal);
                    })
                    .setNegativeButton("Cancela", null)
                    .create();

            // Evita mostrar o teclado de cara em alguns Androids mais antigos
            // (não funciona em todos, mas evita a "dança" do diálogo em alguns)
            dialogNome.setOnShowListener(d -> {
                Button btnOk = dialogNome.getButton(AlertDialog.BUTTON_POSITIVE);
                btnOk.setFocusable(true);
                btnOk.setFocusableInTouchMode(true);
                btnOk.requestFocus();

            });
            dialogNome.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            dialogNome.show();
        });
    }

    private void botoesHabilitados(boolean status) {
        findViewById(R.id.btnJogar).setActivated(status);
        findViewById(R.id.btnBluetooth).setActivated(status);
        findViewById(R.id.btnOpcoes).setActivated(status);
    }

    private void perguntaCriarOuProcurarBluetooth() {
        botoesHabilitados(false);
        OnClickListener listener = (dialog, which) -> {
            botoesHabilitados(true);
            switch (which) {
                case AlertDialog.BUTTON_NEGATIVE:
                    startActivity(new Intent(TituloActivity.this,
                        ServidorBluetoothActivity.class));
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    startActivity(new Intent(TituloActivity.this,
                        ClienteBluetoothActivity.class));
                    break;
            }
        };
        new AlertDialog.Builder(this).setTitle("Bluetooth")
            .setMessage("Para jogar via Bluetooth, um celular deve criar o jogo e os outros devem procurá-lo.\n\nCertifique-se de que todos os celulares estejam pareados com o celular que criar o jogo.")
            .setNegativeButton("Criar Jogo", listener)
            .setPositiveButton("Procurar Jogo", listener)
            .show();
    }

    public void jogarClickHandler(View v) {
        CriadorDePartida.setActivitySala(this);
        Intent intent = new Intent(TituloActivity.this, TrucoActivity.class);
        startActivity(intent);
    }

    public void opcoesButtonClickHandler(View v) {
        Intent settingsActivity = new Intent(getBaseContext(),
            OpcoesActivity.class);
        startActivity(settingsActivity);
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.finishAndRemoveTask();
        }
    }

    public void modoButtonClickHandler(View view) {
        if (view.getTag().equals("outros")) {
            new AlertDialog.Builder(this).setTitle("Modos Especiais")
                .setMessage("Estes modos são jogados com a partida valendo 1 e o truco indo a 3, depois 6, 9 e 12.")
                .setNegativeButton("Baralho Limpo", (dialog, which) -> {
                    selecionaModo("L");
                    promoveJogoInternet(true);
                })
                .setPositiveButton("Manilha Velha", (dialog, which) -> {
                    selecionaModo("V");
                    promoveJogoInternet(true);
                })
                .show();
        } else {
            selecionaModo((String) view.getTag());
            promoveJogoInternet(true);
        }
    }

    private void selecionaModo(String modo) {
        ((TextView) findViewById(R.id.textViewModo)).setText(Partida.textoModo(modo));
        preferences.edit().putString("modo", modo).apply();
    }

    private void promoveJogoInternet(boolean repete) {
        new Thread(() -> {
            if (InternetUtils.isPromoveJogoInternet(this, repete)) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Jogue pela internet agora! Pessoas estão aguardando por você.", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public Partida criaNovaPartida(JogadorHumano jogadorHumano) {
        String modo = getLetraDoModo(this);
        boolean humanoDecide = preferences.getBoolean("humanoDecide", true);
        boolean jogoAutomatico = preferences.getBoolean("jogoAutomatico", false);
        Partida novaPartida = new PartidaLocal(humanoDecide, jogoAutomatico, modo);
        novaPartida.adiciona(jogadorHumano);
        for (int i = 2; i <= 4; i++) {
            novaPartida.adiciona(new JogadorBot());
        }
        return novaPartida;
    }

    @Override
    public void enviaLinha(String linha) {
        throw new RuntimeException("Jogo single-player não possui conexão");
    }

    @Override
    public void enviaLinha(int slot, String linha) {
        throw new RuntimeException("Jogo single-player não possui conexão");
    }

    @Override
    protected void onResume() {
        super.onResume();
        CriadorDePartida.setActivitySala(this);
    }
}
