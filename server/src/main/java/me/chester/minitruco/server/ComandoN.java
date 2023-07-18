package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import me.chester.minitruco.core.Jogador;

/**
 * Atribui um nome ao jogador.
 * <p>
 * Parâmetro: Nome a atribuir
 * <p>
 * O nome deve ser único, e conter apenas letras, números e os caracteres em
 * CARACTERES_PERMITIDOS. Também não pode começar com "Robo_".
 * <p>
 * O servidor guarda o upper/lowercase, mas o nome tem que ser único de forma case-insensitive.
 * Ex.: se o "Roberto" entrou, o "roberto" ou o "ROBERTO" não pdoem entrar.
 */
public class ComandoN extends Comando {

    private static final String CARACTERES_PERMITIDOS = "!@()-_.";

    // TODO sanitizar nomes reservados, case insensitive: bot, chester, chesterbr, minitruco
    // (aqui ou em Sala?)

    @Override
    public void executa(String[] args, JogadorConectado j) {
        // TODO a gente podia receber o comando também
        String comando = String.join(" ", args);
        String nome = Jogador.sanitizaNome(
            (comando + "  ").substring(2)
        );
        j.setNome(nome);
        j.println("N " + nome);
    }
}
