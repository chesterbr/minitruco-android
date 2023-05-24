package me.chester.minitruco.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import me.chester.minitruco.R;
import me.chester.minitruco.android.multiplayer.bluetooth.ClienteBluetoothActivity;
import me.chester.minitruco.android.multiplayer.bluetooth.ServidorBluetoothActivity;
import me.chester.minitruco.android.multiplayer.internet.ClienteInternetActivity;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Activity onde os jogos (partidas) efetivamente acontecem..
 * <p>
 * Ela inicializa o jogo e exibe sa cartas, "balões" de texto e diálogos através
 * de uma <code>MesaView</code>.
 * <p>
 */
public class TrucoActivity extends Activity {

    private MesaView mesa;
    private View layoutFimDeJogo;
    private static boolean mIsViva = false;
    boolean jogoAbortado = false;

    JogadorHumano jogadorHumano;

    Jogo jogo;

    final int[] placar = new int[2];

    static final int MSG_ATUALIZA_PLACAR = 0;
    static final int MSG_TIRA_DESTAQUE_PLACAR = 1;
    static final int MSG_OFERECE_NOVA_PARTIDA = 2;
    static final int MSG_REMOVE_NOVA_PARTIDA = 3;
    static final int MSG_MOSTRA_BOTAO_AUMENTO = 4;
    static final int MSG_ESCONDE_BOTAO_AUMENTO = 5;
    static final int MSG_MOSTRA_BOTAO_ABERTA_FECHADA = 6;
    static final int MSG_ESCONDE_BOTAO_ABERTA_FECHADA = 7;

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            TextView tvNos = findViewById(R.id.textview_nos);
            TextView tvEles = findViewById(R.id.textview_eles);
            Button btnAumento = findViewById(R.id.btnAumento);
            Button btnAbertaFechada = findViewById(R.id.btnAbertaFechada);
            Button btnNovaPartida = findViewById(R.id.btnNovaPartida);
            switch (msg.what) {
            case MSG_ATUALIZA_PLACAR:
                if (placar[0] != msg.arg1) {
                    tvNos.setBackgroundColor(Color.YELLOW);
                }
                if (placar[1] != msg.arg2) {
                    tvEles.setBackgroundColor(Color.YELLOW);
                }
                tvNos.setText(msg.arg1 + " 👇");
                tvEles.setText("👆 " + msg.arg2);
                placar[0] = msg.arg1;
                placar[1] = msg.arg2;
                break;
            case MSG_TIRA_DESTAQUE_PLACAR:
                tvNos.setBackgroundColor(Color.TRANSPARENT);
                tvEles.setBackgroundColor(Color.TRANSPARENT);
                break;
            case MSG_OFERECE_NOVA_PARTIDA:
                if (jogo instanceof JogoLocal) {
                    layoutFimDeJogo.setVisibility(View.VISIBLE);
                    if (jogo.isJogoAutomatico()) {
                        btnNovaPartida.performClick();
                    }
                }
                break;
            case MSG_REMOVE_NOVA_PARTIDA:
                layoutFimDeJogo.setVisibility(View.INVISIBLE);
                break;
            case MSG_MOSTRA_BOTAO_AUMENTO:
                int chave = getResources().getIdentifier("botao_aumento_" +
                    jogo.nomeNoTruco(jogadorHumano.valorProximaAposta),
                "string", "me.chester.minitruco");
                btnAumento.setText(getResources().getString(chave));
                btnAumento.setVisibility(Button.VISIBLE);
                break;
            case MSG_ESCONDE_BOTAO_AUMENTO:
                btnAumento.setVisibility(Button.GONE);
                break;
            case MSG_MOSTRA_BOTAO_ABERTA_FECHADA:
                btnAbertaFechada.setText(mesa.vaiJogarFechada ? "Aberta"
                        : "Fechada");
                btnAbertaFechada.setVisibility(Button.VISIBLE);
                break;
            case MSG_ESCONDE_BOTAO_ABERTA_FECHADA:
                btnAbertaFechada.setVisibility(Button.GONE);
                break;
            default:
                break;
            }
        }
    };
    private SharedPreferences preferences;
    private ImageView imageValorMao;

    /**
     * Cria um novo jogo e dispara uma thread para ele. Para jogos multiplayer,
     * a criação é terceirizada para a classe apropriada.
     * <p>
     * Este método é chamada pela primeira vez a partir da MesaView (para
     * garantir que o jogo só role quando ela estiver inicializada) e dali em
     * diante pelo botão de nova partida.
     */
    public void criaEIniciaNovoJogo() {
        jogadorHumano = new JogadorHumano(this, mesa);
        if (getIntent().hasExtra("servidorBluetooth")) {
            jogo = ServidorBluetoothActivity.criaNovoJogo(jogadorHumano);
        } else if (getIntent().hasExtra("clienteBluetooth")) {
            jogo = ClienteBluetoothActivity.criaNovoJogo(jogadorHumano);
        } else if (getIntent().hasExtra("clienteInternet")) {
            jogo = ClienteInternetActivity.criaNovoJogo(jogadorHumano);
        } else {
            jogo = criaNovoJogoSinglePlayer(jogadorHumano);
        }
        (new Thread(jogo)).start();
        mIsViva = true;
    }

    private Jogo criaNovoJogoSinglePlayer(JogadorHumano humano) {
        String modo = preferences.getString("modo", "P");
        boolean humanoDecide = preferences.getBoolean("humanoDecide", true);
        boolean jogoAutomatico =  preferences.getBoolean("jogoAutomatico", false);
        Jogo novoJogo = new JogoLocal(humanoDecide, jogoAutomatico, modo);
        novoJogo.adiciona(jogadorHumano);
        for (int i = 2; i <= 4; i++) {
            novoJogo.adiciona(new JogadorBot());
        }
        return novoJogo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.truco);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        imageValorMao = findViewById(R.id.imageValorMao);
        setValorMao(0);
        mesa = findViewById(R.id.MesaView01);
        mesa.setCorFundoCartaBalao(preferences.getInt("corFundoCarta", Color.WHITE));
        layoutFimDeJogo = findViewById(R.id.layoutFimDeJogo);

        mesa.velocidade = preferences.getBoolean("animacaoRapida", false) ? 4 : 1;
        mesa.setTrucoActivity(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.truco, menu);
        return true;
    }

    public void novaPartidaClickHandler(View v) {
        Message.obtain(handler, MSG_REMOVE_NOVA_PARTIDA).sendToTarget();
        criaEIniciaNovoJogo();
    }

    public void aumentoClickHandler(View v) {
        // Não usamos o handler aqui para reduzir a chance da pessoa
        // fazer uma acionamento duplo (e duplicar o aumento)
        findViewById(R.id.btnAumento).setVisibility(Button.GONE);
        mesa.setStatusVez(MesaView.STATUS_VEZ_HUMANO_AGUARDANDO);
        jogo.aumentaAposta(jogadorHumano);
    }

    public void abertaFechadaClickHandler(View v) {
        mesa.vaiJogarFechada = !mesa.vaiJogarFechada;
        handler.sendMessage(Message.obtain(handler,
                MSG_MOSTRA_BOTAO_ABERTA_FECHADA));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mesa.setVisivel(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mesa.setVisivel(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsViva = false;
        if (!jogoAbortado) {
            jogo.abortaJogo(1);
        }
    }

    @Override
    public void onBackPressed() {
        if (!preferences.getBoolean("sempreConfirmaFecharJogo", true)) {
            finish();
            return;
        }

        View dialogPerguntaAntesDeFechar = getLayoutInflater()
                .inflate(R.layout.dialog_sempre_confirma_fechar_jogo, null);
        final CheckBox checkBoxPerguntarSempre = dialogPerguntaAntesDeFechar
                .findViewById(R.id.checkBoxSempreConfirmaFecharJogo);
        checkBoxPerguntarSempre.setOnCheckedChangeListener((button, isChecked) -> {
            preferences.edit().putBoolean("sempreConfirmaFecharJogo", isChecked).apply();
        });

        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Encerrar")
            .setView(dialogPerguntaAntesDeFechar)
            .setMessage("Você quer mesmo encerrar este jogo?")
            .setPositiveButton("Sim", (dialog, which) -> finish())
            .setNegativeButton("Não", null)
            .show();
    }

    public static boolean isViva() {
        return mIsViva;
    }

    public void setValorMao(int valor) {
        runOnUiThread(() -> {
            int image;
            switch (valor) {
                case 1:
                    image = R.drawable.valorrodada1;
                    break;
                case 2:
                    image = R.drawable.valorrodada2;
                    break;
                case 3:
                    image = R.drawable.valorrodada3;
                    break;
                case 4:
                    image = R.drawable.valorrodada4;
                    break;
                case 6:
                    image = R.drawable.valorrodada6;
                    break;
                case 10:
                    image = R.drawable.valorrodada10;
                    break;
                case 12:
                    image = R.drawable.valorrodada12;
                    break;
                default:
                    image = R.drawable.placarrodada0;
            }
            imageValorMao.setImageResource(image);
        });
    }
}
