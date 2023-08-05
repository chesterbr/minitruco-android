package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Entra numa sala.
 * <p>
 * Recebe o tipo de sala e o modo de jogo. Tipos:
 * <ul>
 *     <li>PUB - procura uma sala pública, se não achar, cria</li>
 *     <li>NPU - cria uma nova sala pública</li>
 *     <li>PRI - cria uma nova sala privada</li>
 *     <li>PRI-nnnnn - entra numa sala privada existente</li>
 * </ul>
 * TODO: NPU, PRI, PRI-nnnn
 * Modos: "P" para truco paulista, "M" para mineiro, etc; vide classe `Modo`
 */
public class ComandoE extends Comando {

    @Override
    public void executa(String[] args, JogadorConectado j) {

        try {
            if (j.getNome().equals("unnamed")) {
                j.println("X NO");
                return;
            }
            if (j.getSala() != null) {
                // TODO: mostrar o código se for sala pública?
                j.println("X JE " + j.getSala().codigo);
            } else {
                if ("PUB".equals(args[1])) {
                    Sala s = Sala.colocaEmSalaPublica(j, args[2]);
                    s.mandaInfoParaTodos();
                } else if ("NPU".equals(args[1])) {
                    Sala s = new Sala(true, args[2]);
                    s.adiciona(j);
                    s.mandaInfoParaTodos();
                } else {
                    j.println("X AI");
                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            j.println("X AI");
        }

    }

}
