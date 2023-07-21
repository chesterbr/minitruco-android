package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Informa ao servidor que o jogador deseja iniciar a partida na sala em que está.
 * <p>
 * Ignorado se não for o gerente da sala (mas dá erro se não estivermos numa sala).
 */

public class ComandoQ extends Comando {

    @Override
    public void executa(String[] args, JogadorConectado j) {

        Sala s = j.getSala();
        if (s != null) {
            s.iniciaPartida(j);
        } else {
            j.println("X FS");
        }
    }

}
