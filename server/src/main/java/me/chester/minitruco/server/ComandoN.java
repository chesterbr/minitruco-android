package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import me.chester.minitruco.core.Jogador;

/**
 * Atribui um nome ao jogador.
 * <p>
 * Parâmetro: Nome a atribuir
 * <p>
 * O comando é ignorado se o jogador já estiver em uma sala. Se o nome for
 * inválido, o servidor atribui um nome default (sem_nome_NNN).
 */
public class ComandoN extends Comando {

    private static final String CARACTERES_PERMITIDOS = "!@()-_.";

    @Override
    public void executa(String[] args, JogadorConectado j) {
        if (j.getSala() != null) {
            return;
        }
        String comando = String.join(" ", args);
        String nome = Jogador.sanitizaNome(
            (comando + "  ").substring(2)
        );
        ServerLogger.evento("Nome mudou de " + j.getNome() + " para " + nome);
        j.setNome(nome);
        j.println("N " + nome);
    }
}
