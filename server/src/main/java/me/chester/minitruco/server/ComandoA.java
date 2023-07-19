package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Informa ao servidor que o jogador abandonou a partida.
 */
public class ComandoA extends Comando {

    @Override
    public void executa(String[] args, JogadorConectado j) {
        if (!j.jogando)
            return;
        j.getSala().getPartida().abandona(j.getPosicao());
    }
}
