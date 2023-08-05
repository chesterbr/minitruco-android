package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Entra numa sala. Sintaxe:
 * <ul>
 *     <li>PUB modo - procura uma sala pública compatível, se não achar, cria</li>
 *     <li>NPU modo - cria nova sala pública</li>
 *     <li>PRI modo - cria nova sala privada</li>
 *     <li>PRI-nnnnn - entra numa sala privada existente</li>
 * </ul>
 * modos: "P" para truco paulista, "M" para mineiro, etc; vide classe `Modo`
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
                } else if ("PRI".equals(args[1])) {
                    Sala s = new Sala(false, args[2]);
                    s.adiciona(j);
                    s.mandaInfoParaTodos();
                } else if (args[1].startsWith("PRI-")) {
                    Sala s = Sala.colocaEmSalaPrivada(j, args[1].substring(4));
                    // TODO tratar null
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
