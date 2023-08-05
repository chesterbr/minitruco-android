package me.chester.minitruco.android;

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
    protected Button btnNovaSala;
    protected Button btnEntrarSala;
    private View layoutJogadoresEBotoesGerente;
    protected View layoutBotoesGerente;
    protected View layoutBotoesSala;
    protected TextView textViewStatus;
    protected TextView textViewJogador1;
    protected TextView textViewJogador2;
    protected TextView textViewJogador3;
    protected TextView textViewJogador4;
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
        layoutBotoesSala = findViewById(R.id.layoutBotoesSala);
        btnNovaSala = findViewById(R.id.btnNovaSala);
        btnEntrarSala = findViewById(R.id.btnEntrarSala);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewTituloSala = findViewById(R.id.textViewTituloSala);
        textViewInfoSala = findViewById(R.id.textViewInfoSala);
        textViewJogador1 = findViewById(R.id.textViewJogador1);
        textViewJogador2 = findViewById(R.id.textViewJogador2);
        textViewJogador3 = findViewById(R.id.textViewJogador3);
        textViewJogador4 = findViewById(R.id.textViewJogador4);
        layoutJogadoresEBotoesGerente.setVisibility(View.GONE);
        layoutBotoesGerente.setVisibility(View.INVISIBLE);
        layoutBotoesSala.setVisibility(View.GONE);
        textViewStatus.setVisibility(View.GONE);
        textViewInfoSala.setVisibility(View.GONE);
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
                tipoSala = "PRI";
                codigo = tipoSala.substring(4);
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

            // Ajusta os nomes para que o jogador local fique sempre na
            // parte inferior da tela (textViewJogador1), sucedido por
            // "(você)"; precede a pessoa que é gerente com "Gerente:"
            int p = (posJogador - 1) % 4;
            textViewJogador1.setText(nomes[p] + (p == 0 ? " (você/gerente)" : "(você)"));
            p = (p + 1) % 4;
            textViewJogador2.setText(nomes[p] + (p == 0 ? " (gerente)" : ""));
            p = (p + 1) % 4;
            textViewJogador3.setText(nomes[p] + (p == 0 ? " (gerente)" : ""));
            p = (p + 1) % 4;
            textViewJogador4.setText(nomes[p] + (p == 0 ? " (gerente)" : ""));

            // Atualiza outros itens do display
            layoutJogadoresEBotoesGerente.setVisibility(View.VISIBLE);
            findViewById(R.id.layoutBotoesGerente).setVisibility(
                isGerente ? View.VISIBLE : View.INVISIBLE);
            if (isGerente) {
                btnIniciar.setEnabled(numJogadores > 1);
                btnInverter.setEnabled(numJogadores > 1);
                btnTrocar.setEnabled(numJogadores > 1);
            }
            switch (tipoSala) {
                case "PUB":
                    textViewTituloSala.setText("Sala Pública");
                    break;
                case "PRI":
                    textViewTituloSala.setText("Sala Privada");
                    break;
                case "BLT":
                    textViewTituloSala.setText("Bluetooth");
                    break;
            }
            textViewStatus.setText("Modo: " + Partida.textoModo(modo));
            if (numJogadores < 4) {
                setMensagem("Aguardando mais pessoas ou gerente iniciar partida");
            } else {
                setMensagem("Aguardando gerente iniciar partida");
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
            if (mensagem == null) {
                textViewMensagem.setVisibility(View.GONE);
            } else {
                textViewMensagem.setVisibility(View.VISIBLE);
                textViewMensagem.setText(mensagem);
            }
        });
    }
}
