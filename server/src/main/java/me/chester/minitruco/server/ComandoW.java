package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Recupera informações do servidor
 */
public class ComandoW extends Comando {

    @Override
    public void executa(String[] args, JogadorConectado j) {
        j.println("W " + MiniTrucoServer.VERSAO_SERVER);
    }

}
