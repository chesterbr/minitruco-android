package me.chester.minitruco.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.Html;

import androidx.appcompat.app.AppCompatActivity;

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

    protected void mostraAlertBox(String titulo, String texto) {
        runOnUiThread(() -> {
            if (this == null || this.isFinishing()) {
                return;
            }
            new AlertDialog.Builder(this).setTitle(titulo)
                    .setMessage(Html.fromHtml(texto))
                    .setNeutralButton("Ok", (dialog, which) -> {
                    }).show();
        });
    }

    protected void encerraTrucoActivity() {
        Intent intent = new Intent(TrucoActivity.BROADCAST_IDENTIFIER);
        intent.putExtra("evento", "desconectado");
        sendBroadcast(intent);
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

    protected void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // não precisa tratar
        }
    }
}
