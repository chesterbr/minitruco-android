package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import me.chester.minitruco.core.Jogador;

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
            if (sala.isPublica()) {
                sala.getPartida().trocaPorBot(j);
                for (int i = 1; i <= 4; i++) {
                    Jogador outroJogador = sala.getPartida().getJogador(i);
                    if (outroJogador instanceof JogadorConectado) {
                        ((JogadorConectado) outroJogador).println("! T " + j.getNome() + " saiu; bot🤖 entrou no lugar.");
                    }
                }
            } else {
                sala.getPartida().abandona(j.getPosicao());
            }
        }
    }
}
