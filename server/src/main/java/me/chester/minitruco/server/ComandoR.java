package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Informa ao servidor que o jogador abandonou a partida.
 */
public class ComandoR extends Comando {

    @Override
    public void executa(String[] args, JogadorConectado j) {
        Sala sala = j.getSala();
        if (sala == null || args.length != 2) {
            return;
        }
        switch (args[1]) {
            case "T":
                if (sala.trocaParceiro(j)) {
                    sala.mandaInfoParaTodos();
                }
                break;
            case "I":
                if (sala.inverteAdversarios(j)) {
                    sala.mandaInfoParaTodos();
                }
                break;
        }
    }
}
