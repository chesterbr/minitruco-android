package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Superclasse de todos os comandos que podem ser emitidos por um cliente.
 * <p>
 * Para criar um novo comando, basta criar uma sublcasse de <code>Comando</code>
 * neste mesmo package.
 * <p>
 * Os comandos do cliente são representados por uma letra. O nome da classe
 * deverá ter o formato <code>Comando_</code>, onde _ é a letra que irá
 * acionar o comando.
 * <p>
 * Por exemplo, o comando <code>E</code>, que entra numa sala é representado
 * pela classe <code>ComandoE</code>.
 *
 * @author Chester
 */
public abstract class Comando {

    /**
     * Executa o comando
     *
     * @param args argumentos recebidos pelo comando (o 1o. elemento é o próprio
     *             comando)
     * @param j    Jogador que solicitou o comando
     */
    public abstract void executa(String[] args, JogadorConectado j);

}
