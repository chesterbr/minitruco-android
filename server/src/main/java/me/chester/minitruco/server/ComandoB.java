package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Pergunta ao servidor se o cliente é compatível ou precisa de atualização.
 * <p>
 * Recebe como parâmetro o build do cliente (ex.: 20102 para versão 2.01.02)
 * <p>
 * Se o build for antigo, o servidor manda um toast pedindo para atualizar e
 * desconecta o cliente. Argumentos inválidos são ignorados.
 */
public class ComandoB extends Comando {
    @Override
    public void executa(String[] args, JogadorConectado j) {
        if (args.length == 2 && args[1].matches("\\d{5}")) {
            if (Integer.parseInt(args[1]) < MiniTrucoServer.BUILD_MINIMO_CLIENTE) {
                j.println("! T Atualize o jogo para a versão mais recente para jogar online");
                j.desconecta();
            }

        }
    }
}
