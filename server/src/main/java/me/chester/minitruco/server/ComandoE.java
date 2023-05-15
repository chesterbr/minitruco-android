package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Entra numa sala.
 * <p>
 * Parâmetros:
 *   - "R ___" para entrar em uma sala (ou criar uma) com as regras especificadas
 *     (cada _ é T ou F para baralho limpo, manilha velha e modo mineiro)
 * <p>
 * Obs.: Se o jogador não tiver nome, um nome implícito será dado
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
                    // TODO: implementar comandos para sala privada
                } else {
                    j.println("X AI");
                }
//                Sala s = Sala.getSala(Integer.parseInt(args[1]));
//                if (s == null) {
//                    j.println("X SI");
//                } else if (j.getNome().equals("unnamed")) {
//                    j.println("X NO");
//                } else if (s.adiciona(j)) {
//                    j.querJogar = false;
//                    // j.enviaTexto("A "+s.getNumSala()+" "+s.getPosicao(j));
//                    // s.notificaJogadores("E "+s.getPosicao(j)+"
//                    // "+j.getNome());
//                    j.println("E "+s.getNumSala());
//                    s.notificaJogadores(s.getInfo());
//                } else {
//                    j.println("X CH");
//                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            j.println("X AI");
        }

    }

}
