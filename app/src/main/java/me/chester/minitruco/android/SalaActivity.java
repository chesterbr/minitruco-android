package me.chester.minitruco.android;

import static me.chester.minitruco.core.TrucoUtils.nomeHtmlParaDisplay;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

import me.chester.minitruco.R;
import me.chester.minitruco.android.multiplayer.PartidaRemota;
import me.chester.minitruco.core.Partida;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Qualquer activity que permite ao jogador iniciar novas partidas.
 * <p>
 * Descendentes desta classe exibem as opções e/ou nomes dos jogadores (conforme
 * o tipo de jogo e o papel do usuário), criam os objetos Partida (onde a lógica
 * do jogo roda) e chamam a TrucoActivity (que permite ao usuário interagir com
 * essa lógica).
 */
public abstract class SalaActivity extends AppCompatActivity {

    protected Button btnIniciar;
    protected Button btnInverter;
    protected Button btnTrocar;
    protected Button btnNovaSalaPublica;
    protected Button btnNovaSalaPrivada;
    protected Button btnEntrarComCodigo;
    protected View layoutJogadoresEBotoesGerente;
    protected View layoutBotoesGerente;
    protected View layoutBotoesInternet;
    protected View layoutRegras;
    protected TextView textViewStatus;
    protected TextView textViewJogador1;
    protected TextView textViewJogador2;
    protected TextView textViewJogador3;
    protected TextView textViewJogador4;
    protected TextView[] textViewsJogadores;
    protected TextView textViewTituloSala;
    protected TextView textViewInfoSala;
    protected int posJogador;
    protected String modo;
    protected PartidaRemota partida;
    protected int numJogadores;
    protected boolean isGerente;

    /**
     * Em salas multiplayer (que de fato mostram uma "sala" com os nomes dos
     * jogadores, etc.), ativa e inicializa o layout com esses elementos.
     */
    protected void inicializaLayoutSala() {
        setContentView(R.layout.sala);
        btnIniciar = findViewById(R.id.btnIniciar);
        btnInverter = findViewById(R.id.btnInverter);
        btnTrocar = findViewById(R.id.btnTrocar);
        layoutJogadoresEBotoesGerente = findViewById(R.id.layoutJogadoresEBotoesGerente);
        layoutBotoesGerente = findViewById(R.id.layoutBotoesGerente);
        layoutBotoesInternet = findViewById(R.id.layoutBotoesInternet);
        layoutRegras = findViewById(R.id.layoutRegras);
        btnNovaSalaPublica = findViewById(R.id.btnNovaSalaPublica);
        btnNovaSalaPrivada = findViewById(R.id.btnNovaSalaPrivada);
        btnEntrarComCodigo = findViewById(R.id.btnEntrarComCodigo);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewTituloSala = findViewById(R.id.textViewTituloSala);
        textViewInfoSala = findViewById(R.id.textViewInfoSala);
        textViewJogador1 = findViewById(R.id.textViewJogador1);
        textViewJogador2 = findViewById(R.id.textViewJogador2);
        textViewJogador3 = findViewById(R.id.textViewJogador3);
        textViewJogador4 = findViewById(R.id.textViewJogador4);
        textViewsJogadores = new TextView[] {
            textViewJogador1, textViewJogador2, textViewJogador3, textViewJogador4
        };
        layoutJogadoresEBotoesGerente.setVisibility(View.INVISIBLE);
        layoutBotoesGerente.setVisibility(View.INVISIBLE);
        layoutBotoesInternet.setVisibility(View.GONE);
        textViewInfoSala.setVisibility(View.GONE);
        textViewStatus.setText("");
        setMensagem(null);
    }

    /**
     * Atualiza a sala com as informações recebidas do servidor.
     * <p>
     * Se houver partida em andamento, ela é encerrda (junto com a activity
     * de jogo).
     *
     * @param notificacaoI notificação recebida do servidor com as infos da sala.
     */
    protected void exibeMesaForaDoJogo(String notificacaoI) {
        runOnUiThread(() -> {
            // Decodifica a notificação, guardando os dados relevantes
            String[] tokens = notificacaoI.split(" ");
            String[] nomes = tokens[1].split(Pattern.quote("|"));
            modo = tokens[2];
            posJogador = Integer.parseInt(tokens[3]);
            isGerente = posJogador == 1;
            String tipoSala = tokens[4];
            String codigo = null;
            if (tipoSala.startsWith("PRI-")) {
                codigo = tipoSala.substring(4);
                tipoSala = "PRI";
            }

            // Volta pra mesa (se já não estiver nela)
            encerraTrucoActivity();
            if (partida != null) {
                partida.abandona(0);
                partida = null;
            }

            // "bot" é um nome especial, que indica que o jogador é um bot
            numJogadores = 4;
            for (String nome : nomes) {
                if (nome.equals("bot")) {
                    numJogadores--;
                }
            }

            // Coloca os nomes dos jogadores nas posições corretas, indicando
            // jogador da vez, gerente, expulsáveis
            for (int i = 1; i <= 4; i++) {
                String nomeHtml = nomeHtmlParaDisplay(notificacaoI, i);
                TextView tv = textViewsJogadores[i - 1];
                tv.setText(Html.fromHtml(nomeHtml));
            }

            // Atualiza outros itens do display
            layoutJogadoresEBotoesGerente.setVisibility(View.VISIBLE);
            layoutRegras.setVisibility(View.VISIBLE);
            findViewById(R.id.layoutBotoesGerente).setVisibility(
                isGerente && !tipoSala.equals("PUB") ? View.VISIBLE : View.INVISIBLE);
            if (isGerente) {
                btnIniciar.setEnabled(numJogadores > 1);
                btnInverter.setEnabled(numJogadores > 1);
                btnTrocar.setEnabled(numJogadores > 1);
            }
            switch (tipoSala) {
                case "PUB":
                    textViewTituloSala.setText("Sala Pública");
                    layoutBotoesInternet.setVisibility(View.VISIBLE);
                    textViewInfoSala.setVisibility(View.GONE);
                    break;
                case "PRI":
                    textViewTituloSala.setText("Sala Privada - CÓDIGO: " + codigo);
                    layoutBotoesInternet.setVisibility(View.GONE);
                    textViewInfoSala.setVisibility(View.VISIBLE);
                    break;
                case "BLT":
                    textViewTituloSala.setText("Bluetooth");
                    break;
            }
            textViewStatus.setText("Modo: " + Partida.textoModo(modo));
            // Para atualizar a mensagem, levamos em conta que:
            // - Salas públicas só iniciam quando estão cheias (e fazem isso
            //   automaticamente, sem interação do gerente)
            // - Salas privadas e Bluetooth precisam de pelo menos dois
            //   jogadores humanos (e o gerente decide quando iniciar)
            setMensagem(null);
            if (tipoSala.equals("PUB")) {
                if (numJogadores < 4) {
                    setMensagem("Aguardando mais pessoas");
                }
            } else if (isGerente) {
                if (numJogadores == 1) {
                    setMensagem("Aguardando pelo menos uma pessoa");
                } else if (numJogadores < 4) {
                    setMensagem("Aguardando mais pessoas");
                } else {
                    setMensagem("Organize as pessoas na mesa e clique em JOGAR!");
                }
            } else {
                if (numJogadores < 4) {
                    setMensagem("Aguardando mais pessoas ou gerente iniciar partida");
                } else {
                    setMensagem("Aguardando gerente organizar a mesa e iniciar partida");
                }
            }
        });
    }

    protected void mostraAlertBox(String titulo, String texto) {
        runOnUiThread(() -> {
            if (isFinishing()) {
                return;
            }
            new AlertDialog.Builder(this).setTitle(titulo)
                    .setMessage(Html.fromHtml(texto))
                    .setNeutralButton("Ok", (dialog, which) -> {
                    }).show();
        });
    }

    protected void msgErroFatal(String texto) {
        msgErroFatal("Aviso", texto);
    }

    protected void msgErroFatal(String titulo, String texto) {
        runOnUiThread(() -> {
            encerraTrucoActivity();
            if (isFinishing()) {
                return;
            }
            if (layoutJogadoresEBotoesGerente != null) {
                layoutJogadoresEBotoesGerente.setVisibility(View.INVISIBLE);
            }
            if (layoutBotoesInternet != null) {
                layoutBotoesInternet.setVisibility(View.GONE);
            }
            if (layoutRegras != null) {
                layoutRegras.setVisibility(View.GONE);
            }
            setMensagem(null);
            new AlertDialog.Builder(this)
                    .setTitle(titulo)
                    .setMessage(texto)
                    .setNeutralButton("Fechar", (dialog, which) -> finish())
                    .setOnCancelListener(v -> finish())
                    .show();
        });
    }

    /**
     * Cria uma nova partida.
     * <p>
     * Normalmente é chamado pelo CriadorDePartida, que sabe qual Sala
     * acionar (e, portanto, qual o tipo de partida apropriado).
     *
     * @param jogadorHumano jogador humano que será associado à partida e
     *                      à activity (para intermediação de eventos)
     */
    public abstract Partida criaNovaPartida(JogadorHumano jogadorHumano);

    /**
     * Envia uma linha de texto para a conexão remota.
     * <p>
     * Se houver mais de uma, envia para todas.
     *
     * @param linha texto a ser enviado.
     */
    public abstract void enviaLinha(String linha);

    /**
     * Envia uma linha de texto para uma das conexões remotas (se houver mais
     * de uma).
     *
     * @param linha texto a ser enviado.
     * @param slot especifica qual das conexões remotas deve receber a mensagem
     */
    public abstract void enviaLinha(int slot, String linha);

    protected void iniciaTrucoActivitySePreciso() {
        if (!TrucoActivity.isViva()) {
            startActivity(
                new Intent(this, TrucoActivity.class)
                    .putExtra("multiplayer", true));
        }
    }

    public void encerraTrucoActivity() {
        if (TrucoActivity.isViva()) {
            Intent intent = new Intent(TrucoActivity.BROADCAST_IDENTIFIER);
            intent.putExtra("evento", "desconectado");
            sendBroadcast(intent);
        }
    }

    protected void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // não precisa tratar
        }
    }

    /**
     * Mostra mensagem permanente em uma caixa no meio da tela.
     * <p>
     * Requer layout que contenha textViewMensagem (ex.: sala.xml).
     *
     * @param mensagem Mensagem a mostrar, ou null para esconder a caixa
     */
    protected void setMensagem(String mensagem) {
        runOnUiThread(() -> {
            TextView textViewMensagem = findViewById(R.id.textViewMensagem);
            if (textViewMensagem == null) {
                return;
            }
            if (mensagem == null) {
                textViewMensagem.setVisibility(View.GONE);
            } else {
                textViewMensagem.setVisibility(View.VISIBLE);
                textViewMensagem.setText(mensagem);
            }
        });
    }
}
