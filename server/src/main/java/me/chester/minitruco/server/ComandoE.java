package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import me.chester.minitruco.core.Modo;

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
        String subcomando, modo = null;
        if (args.length < 2 || args.length > 3) {
            return;
        }
        subcomando = args[1];
        if (args.length == 3) {
            modo = args[2];
            if (!Modo.isModoValido(modo)) {
                return;
            }
        }
        if (j.getSala() != null) {
            (new ComandoS()).executa(new String[]{"S"}, j);
        }
        try {
            Sala s;
            if ("PUB".equals(subcomando)) {
                s = Sala.colocaEmSalaPublica(j, modo);
            } else if ("NPU".equals(subcomando)) {
                s = Sala.colocaEmNovaSala(j,true, modo);
            } else if ("PRI".equals(subcomando)) {
                s = Sala.colocaEmNovaSala(j,false, modo);
            } else if (subcomando.startsWith("PRI-")) {
                s = Sala.colocaEmSalaPrivada(j, subcomando.substring(4));
            } else {
                return;
            }
            if (s != null) {
                s.mandaInfoParaTodos();
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            // Simplesmente ignoramos qualquer erro causado pelo comando
        }

    }

}
