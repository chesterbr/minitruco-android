package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Faz o jogador sair da sala onde está.
 * <p>
 * Se houver algum jogo em andamento, será encerrado.
 * <p>
 * Os participantes da sala são notificados.
 */
public class ComandoS extends Comando {

    public void executa(String[] args, JogadorConectado j) {
        Sala s = j.getSala();
        if (s != null) {
            s.remove(j);
            j.println("S");
            s.mandaInfoParaTodos();
        } else {
            j.println("X FS");
        }

    }

}
