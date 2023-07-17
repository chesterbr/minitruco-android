package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.lang.reflect.InvocationTargetException;

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

    /**
     * Interpreta uma linha de comando e executa com a classe apropriada
     *
     * @param linha comando recebido do cliente (ex.: "N Carlos" para "meu nome é Carlos")
     * @param j Jogador que solicitou o comando
     * @see /docs/desenvolvimento.md#protocolo-de-comunicação-multiplayer
     */
    public static void interpreta(String linha, JogadorConectado j) {
        // Quebra a solicitação em tokens
        String[] args = linha.split(" ");
        if (args.length == 0 || args[0].length() == 0) {
            return;
        }

        // Encontra a implementação do comando solicitado e chama
        if (args[0].length() != 1) {
            return;
        }
        char comando = Character.toUpperCase(args[0].charAt(0));
        try {
            Comando c = (Comando) Class.forName(
                    "me.chester.minitruco.server.Comando"
                            + comando).getDeclaredConstructor().newInstance();
            c.executa(args, j);
        } catch (ClassNotFoundException e) {
            j.println("X CI");
        } catch (InstantiationException e) {
            j.println("X CI");
        } catch (IllegalAccessException e) {
            j.println("X CI");
        } catch (InvocationTargetException e) {
            j.println("X CI");
        } catch (NoSuchMethodException e) {
            j.println("X CI");
        }
    }

}
