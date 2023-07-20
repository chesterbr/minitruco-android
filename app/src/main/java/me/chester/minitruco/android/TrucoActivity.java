package me.chester.minitruco.android;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import me.chester.minitruco.R;
import me.chester.minitruco.core.Partida;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Activity onde os jogos (partidas) efetivamente acontecem..
 * <p>
 * Ela inicializa a partida e exibe a <code>MesaView</code> (cartas, balões, etc)
 * e os placares (mão, partida e partidas).
 * <p>
 */
public class TrucoActivity extends Activity {

    public static final String SEPARADOR_PLACAR_PARTIDAS = " x ";
    public static final String BROADCAST_IDENTIFIER = "me.chester.minitruco.EVENTO_TRUCO_ACTIVITY";
    private static boolean mIsViva = false;
    final int[] placar = new int[2];
    boolean partidaAbortada = false;
    JogadorHumano jogadorHumano;
    Partida partida;
    private MesaView mesa;
    private TextView textViewPartidas;
    TextView textViewNos;
    TextView textViewRivais;
    Button btnNovaPartida;

    /**
     * Atualiza o placar com a nova pontuação. Se houve mudança, destaca o
     * placar do time que pontuou.
     *
     * @param pontosNos pontos da dupla do jogador humano
     * @param pontosRivais pontos da outra dupla
     */
    void atualizaPlacar(int pontosNos, int pontosRivais) {
        runOnUiThread(() -> {
            if (placar[0] != pontosNos) {
                textViewNos.setTextColor(Color.YELLOW);
            }
            if (placar[1] != pontosRivais) {
                textViewRivais.setTextColor(Color.YELLOW);
            }
            textViewNos.setText((pontosNos < 10 ? " " : "") + pontosNos);
            textViewRivais.setText(pontosRivais + (pontosRivais < 10 ? " " : ""));
            placar[0] = pontosNos;
            placar[1] = pontosRivais;
        });
    }

    /**
     * Remove o destaque do placar do time que pontuou (se houver um).
     */
    void tiraDestaqueDoPlacar() {
        runOnUiThread(() -> {
            textViewNos.setTextColor(Color.BLACK);
            textViewRivais.setTextColor(Color.BLACK);
        });
    }


    private SharedPreferences preferences;
    private ImageView imageValorMao;
    private ImageView[] imagesResultadoRodada;

    public static boolean isViva() {
        return mIsViva;
    }

    /**
     * Cria um nova partida e dispara uma thread para ela.
     * <p>
     * A criação é, na prática, terceirizada para o CriadorDePartida.
     * <p>
     * Este método é chamada pela primeira vez a partir da MesaView (para
     * garantir que a partida só role quando a mesa estiver inicializada) e dali em
     * diante pelo botão de nova partida.
     */
    private void criaEIniciaNovaPartida() {
        preferences.edit().putInt("statPartidas",
            preferences.getInt("statPartidas", 0) + 1
        ).apply();
        jogadorHumano = new JogadorHumano(this, mesa);
        partida = CriadorDePartida.criaNovaPartida(jogadorHumano);
        mIsViva = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.truco);
        reorientaLayoutPlacar();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        textViewPartidas = findViewById(R.id.textViewPartidas);
        textViewNos = findViewById(R.id.textViewNos);
        textViewRivais = findViewById(R.id.textViewRivais);
        btnNovaPartida = findViewById(R.id.btnNovaPartida);
        imageValorMao = findViewById(R.id.imageValorMao);
        imagesResultadoRodada = new ImageView[3];
        imagesResultadoRodada[0] = findViewById(R.id.imageResultadoRodada1);
        imagesResultadoRodada[1] = findViewById(R.id.imageResultadoRodada2);
        imagesResultadoRodada[2] = findViewById(R.id.imageResultadoRodada3);

        inicializaPlacarDePartidas();
        setValorMao(0);
        mesa = findViewById(R.id.MesaView01);
        mesa.setCorFundoCartaBalao(preferences.getInt("corFundoCarta", Color.WHITE));
        mesa.velocidade = preferences.getBoolean("animacaoRapida", false) ? 4 : 1;
        mesa.setEscalaFonte(Integer.parseInt(preferences.getString("escalaFonte", "1")));
        mesa.setTrucoActivity(this);
        mesa.setIndiceDesenhoCartaFechada(preferences.getInt("indiceDesenhoCartaFechada", 0));
        mesa.setTextoAumento(3, getString(R.string.botao_aumento_truco));
        mesa.setTextoAumento(6, getString(R.string.botao_aumento_seis));
        mesa.setTextoAumento(9, getString(R.string.botao_aumento_nove));
        mesa.setTextoAumento(10, getString(R.string.botao_aumento_dez));
        mesa.setTextoAumento(12, getString(R.string.botao_aumento_doze));

        // O jogo só deve efetivamente iniciar quando a mesa estiver pronta
        new Thread(() -> {
            while (!mesa.isInicializada()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            criaEIniciaNovaPartida();
        }).start();
    }

    /**
     * Permite que um cliente bluetooth/internet encerre a atividade ao
     * detectar uma desconexão.
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String evento = intent.getStringExtra("evento");
            if (evento.equals("desconectado")) {
                finish();
            }
        }
    };

    public void novaPartidaClickHandler(View v) {
        btnNovaPartida.setVisibility(View.INVISIBLE);
        criaEIniciaNovaPartida();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reorientaLayoutPlacar();
    }

    /**
     * Ajusta a barra de placar de forma a aproveitar ao máximo o espaço
     * da mesa (abaixo em portrait e na lateral em landscape).
     * <p>
     * (sim, eu poderia ter layouts separados, mas a duplicação de código
     * ia ser monstra, e só tenho que ajustar as orientações dos layouts
     * e atribuir valores manuais para os elementos dinâmicos; além disso
     * eventualmente vai ser possível mudar a barra para o topo/esquerda,
     * e aí vai ter que ser no código mesmo)
     */
    private void reorientaLayoutPlacar() {
        LinearLayout layoutTruco = findViewById(R.id.layoutTruco);
        LinearLayout layoutPlacar = findViewById(R.id.layoutPlacar);
        LinearLayout layoutPlacarPartida = findViewById(R.id.layoutPlacarPartida);
        LinearLayout layoutPlacarPartidas = findViewById(R.id.layoutPlacarPartidas);

        int larguraPlacar;
        int alturaPlacaresDinamicos;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutTruco.setOrientation(LinearLayout.HORIZONTAL);
            layoutPlacar.setOrientation(LinearLayout.VERTICAL);
            larguraPlacar = Resources.getSystem().getDisplayMetrics().heightPixels / 3;
            alturaPlacaresDinamicos = (int) TypedValue.applyDimension(
                COMPLEX_UNIT_DIP,
                60,
                getResources().getDisplayMetrics());

        } else {
            layoutTruco.setOrientation(LinearLayout.VERTICAL);
            layoutPlacar.setOrientation(LinearLayout.HORIZONTAL);
            larguraPlacar = ViewGroup.LayoutParams.MATCH_PARENT;
            alturaPlacaresDinamicos = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        ViewGroup.LayoutParams params = layoutPlacar.getLayoutParams();
        params.width = larguraPlacar;
        layoutPlacar.setLayoutParams(params);

        ViewGroup.LayoutParams paramsPartida = layoutPlacarPartida.getLayoutParams();
        paramsPartida.height = alturaPlacaresDinamicos;
        layoutPlacarPartida.setLayoutParams(paramsPartida);

        ViewGroup.LayoutParams paramsPartidas = layoutPlacarPartidas.getLayoutParams();
        paramsPartidas.height = alturaPlacaresDinamicos;
        layoutPlacarPartidas.setLayoutParams(paramsPartidas);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        mesa.setVisivel(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mesa.setVisivel(true);
        registerReceiver(broadcastReceiver, new IntentFilter(BROADCAST_IDENTIFIER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsViva = false;
        if (partida != null && !partidaAbortada && !partida.finalizada) {
            // Usuário fechou partida em andamento, contabiliza derrota
            // (para o placar de partidas do single-player)...
            int[] pontos = getPlacarDePartidas();
            setPlacarDePartidas(pontos[0], pontos[1] + 1);
            // ...e notifica os outros jogadores (para que todos voltem
            // para a sala no multiplayer)
            partida.abandona(1);
        }
    }

    @Override
    public void onBackPressed() {
        boolean naoPrecisaConfirmar = !preferences.getBoolean("sempreConfirmaFecharJogo", true);
        if (partida == null || partida.finalizada || naoPrecisaConfirmar) {
            finish();
            return;
        }

        View dialogPerguntaAntesDeFechar = getLayoutInflater()
            .inflate(R.layout.dialog_sempre_confirma_fechar_jogo, null);
        final CheckBox checkBoxPerguntarSempre = dialogPerguntaAntesDeFechar
            .findViewById(R.id.checkBoxSempreConfirmaFecharJogo);

        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Encerrar")
            .setView(dialogPerguntaAntesDeFechar)
            .setMessage("Você quer mesmo desistir dessa partida?")
            .setPositiveButton("Sim", (dialog, which) -> {
                if (!checkBoxPerguntarSempre.isChecked()) {
                    preferences.edit().putBoolean("sempreConfirmaFecharJogo", false).apply();
                }
                finish();
            })
            .setNegativeButton("Não", null)
            .show();
    }

    public void setValorMao(int valor) {
        runOnUiThread(() -> {
            int bitmap;
            switch (valor) {
                case 1:
                    bitmap = R.drawable.vale1;
                    break;
                case 2:
                    bitmap = R.drawable.vale2;
                    break;
                case 3:
                    bitmap = R.drawable.vale3;
                    break;
                case 4:
                    bitmap = R.drawable.vale4;
                    break;
                case 6:
                    bitmap = R.drawable.vale6;
                    break;
                case 10:
                    bitmap = R.drawable.vale10;
                    break;
                case 12:
                    bitmap = R.drawable.vale12;
                    break;
                default:
                    bitmap = R.drawable.placarrodada0;
            }
            imageValorMao.setImageResource(bitmap);
        });
    }

    public void setResultadoRodada(int rodada, int resultado) {
        runOnUiThread(() -> {
            int bitmap;
            switch (resultado) {
                case 1:
                    bitmap = R.drawable.placarrodada1;
                    break;
                case 2:
                    bitmap = R.drawable.placarrodada2;
                    break;
                case 3:
                    bitmap = R.drawable.placarrodada3;
                    break;
                default:
                    bitmap = R.drawable.placarrodada0;
            }
            imagesResultadoRodada[rodada - 1].setImageResource(bitmap);
            if (bitmap != R.drawable.placarrodada0) {
                Animation animation = new AlphaAnimation(0, 1);
                animation.setDuration(400);
                animation.setInterpolator(new LinearInterpolator());
                animation.setRepeatCount(2);
                animation.setRepeatMode(Animation.RESTART);
                imagesResultadoRodada[rodada - 1].startAnimation(animation);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void jogoFechado(int numEquipeVencedora) {
        runOnUiThread(() -> {
            int[] pontos = getPlacarDePartidas();
            if (jogadorHumano.getEquipe() == numEquipeVencedora) {
                setPlacarDePartidas(pontos[0] + 1, pontos[1]);
            } else {
                setPlacarDePartidas(pontos[0], pontos[1] + 1);
            }
            if (partida.isHumanoGerente()) {
                btnNovaPartida.setVisibility(View.VISIBLE);
                if (partida.isJogoAutomatico()) {
                    btnNovaPartida.performClick();
                }
            }
        });
    }

    /**
     * Determina se o placar de partidas deve ser salvo/recuperado
     *
     * @return true se o jogo for single-player e o usuário tiver optado por
     *         não limpar o placar de partidas a cada novo jogo
     */
    private boolean placarDePartidasPersistente() {
        if (getIntent().hasExtra("multiplayer")) {
            return false;
        }
        return !preferences.getBoolean("limpaPlacarPartidas", false);
    }

    /**
     * Se o jogo for single-player e o usuário tiver optado por não limpar o
     * placar, recupera o placar de partidas anterior e dá a opção de limpar.
     */
    private void inicializaPlacarDePartidas() {
        if (placarDePartidasPersistente()) {
            setPlacarDePartidas(
                preferences.getInt("statVitorias", 0),
                preferences.getInt("statDerrotas", 0)
            );
            findViewById(R.id.image_limpar_placar).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutPlacarPartidas).setOnClickListener(v -> {
                confirmaLimpezaDoPlacarDePartidas();
            });
        } else {
            setPlacarDePartidas(0, 0);
            findViewById(R.id.image_limpar_placar).setVisibility(View.GONE);
            findViewById(R.id.layoutPlacarPartidas).setOnClickListener(null);
        }
    }

    public void confirmaLimpezaDoPlacarDePartidas() {
        View dialogLimparSempre = getLayoutInflater()
            .inflate(R.layout.dialog_sempre_limpa_placar_partidas, null);
        final CheckBox checkBoxLimparSempre = dialogLimparSempre
            .findViewById(R.id.checkBoxSempreLimpaPlacarPartidas);

        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Zerar placar de partidas")
            .setMessage("Você quer mesmo voltar o placar de partidas para 0 x 0?")
            .setView(dialogLimparSempre)
            .setPositiveButton("Sim", (dialog, which) -> {
                setPlacarDePartidas(0, 0);
                if (checkBoxLimparSempre.isChecked()) {
                    preferences.edit().putBoolean("limpaPlacarPartidas", true).apply();
                    inicializaPlacarDePartidas();
                }
            })
            .setNegativeButton("Não", null)
            .show();
    }


    /**
     * Atualiza o placar de partidas visualmente e, se for persistente,
     * salva o placar no SharedPreferences
     *
     * @param vitorias primeiro número do placar
     * @param derrotas segundo número do placar
     */
    private void setPlacarDePartidas(int vitorias, int derrotas) {
        if (placarDePartidasPersistente()) {
            preferences.edit().putInt("statVitorias", vitorias).apply();
            preferences.edit().putInt("statDerrotas", derrotas).apply();
        }

        runOnUiThread(() -> {
            textViewPartidas.setText(vitorias + SEPARADOR_PLACAR_PARTIDAS + derrotas);
        });
    }

    private int[] getPlacarDePartidas() {
        String[] pontos = textViewPartidas.getText().toString().split(SEPARADOR_PLACAR_PARTIDAS);
        return new int[]{Integer.parseInt(pontos[0]), Integer.parseInt(pontos[1])};
    }
}
