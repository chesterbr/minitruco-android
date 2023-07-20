package me.chester.minitruco.android;

import static android.provider.Settings.Global.DEVICE_NAME;
import static android.text.InputType.TYPE_CLASS_TEXT;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;

import me.chester.minitruco.BuildConfig;
import me.chester.minitruco.R;
import me.chester.minitruco.android.multiplayer.bluetooth.ClienteBluetoothActivity;
import me.chester.minitruco.android.multiplayer.bluetooth.ServidorBluetoothActivity;
import me.chester.minitruco.android.multiplayer.internet.ClienteInternetActivity;
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
        // TODO ver se tem um modo mais central de garantir este default (e outros)
        //      (provavelmente quando migrar esse PreferenceManager deprecado
        //      e começar a centralizar as preferencias nesta view)
        selecionaModo(preferences.getString("modo", "P"));
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
                pedeNome((nome) -> {
                    startActivity(new Intent(getBaseContext(),
                        ClienteInternetActivity.class));
                });
            });
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
            // Não-documentado e só funciona se tiver Bluetooth, cf https://stackoverflow.com/a/67949517/64635
            nome = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
        }
        // Se nada disso deu certo, o sanitizador coloca o nome default
        nome = Jogador.sanitizaNome(nome);

        // Faz a pergunta sugerindo o nome encontrado
        EditText editNomeJogador = new EditText(this);
        editNomeJogador.setInputType(TYPE_CLASS_TEXT);
        editNomeJogador.setMaxLines(1);
        editNomeJogador.setText(nome);

        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("Nome")
                    .setMessage("Qual nome você gostaria de usar?")
                    .setView(editNomeJogador)
                    .setPositiveButton("Ok", (d, w) -> {
                        final String nomeFinal = Jogador.sanitizaNome(
                            editNomeJogador.getText().toString());
                        preferences.edit().putString("nome_multiplayer",
                            nomeFinal).apply();
                        callback.accept(nomeFinal);
                    })
                    .setNegativeButton("Cancela", null)
                    .show();
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
                })
                .setPositiveButton("Manilha Velha", (dialog, which) -> {
                    selecionaModo("V");
                })
                .show();
        } else {
            selecionaModo((String) view.getTag());
        }
    }

    private void selecionaModo(String modo) {
        ((TextView) findViewById(R.id.textViewModo)).setText(Partida.textoModo(modo));
        preferences.edit().putString("modo", modo).apply();
    }

    @Override
    public Partida criaNovaPartida(JogadorHumano jogadorHumano) {
        String modo = preferences.getString("modo", "P");
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
