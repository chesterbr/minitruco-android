package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Informa ao servidor que o jogador abandonou a partida.
 */
public class ComandoA extends Comando {

    @Override
    public void executa(String[] args, JogadorConectado j) {
        Sala sala = j.getSala();
        if (sala == null) {
            return;
        }
        if (sala.getPartida() == null) {
            sala.mandaInfoParaTodos();
        } else {
            sala.getPartida().abandona(j.getPosicao());
        }
    }
}
