package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import me.chester.minitruco.core.Jogador;

/**
 * Informa ao servidor que o jogador abandonou a mesa.
 *
 * Se houver uma partida em andamento, troca ele por um bot e desconecta.
 *
 * Caso contrário (ex.: a partida acabou), remove quaisquer bots da sala e
 * leva os jogadores de volta a ela, permitindo que novos jogadores completem
 * e novas partidas se iniciem
 */
public class ComandoA extends Comando {

    @Override
    public void executa(String[] args, JogadorConectado j) {
        Sala sala = j.getSala();
        if (sala == null) {
            return;
        }
        if (sala.getPartida() == null) {
            if (sala.isPublica()) {
                sala.removeBots();
            }
            sala.mandaInfoParaTodos();
        } else {
            if (sala.isPublica()) {
                sala.trocaPorBot(j);
                for (int i = 1; i <= 4; i++) {
                    Jogador outroJogador = sala.getPartida().getJogador(i);
                    if (outroJogador instanceof JogadorConectado) {
                        ((JogadorConectado) outroJogador).println("! T " + j.getNome() + " saiu; bot🤖 entrou no lugar.");
                    }
                }
                j.setSala(null);
                j.desconecta();
            } else {
                sala.getPartida().abandona(j.getPosicao());
            }
        }
    }
}
