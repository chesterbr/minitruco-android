package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Entra numa sala.
 *
 * Parâmetros:
 *   - "R ___" para entrar em uma sala (ou criar uma) com as regras especificadas
 *     (cada _ é T ou F para baralho limpo, manilha velha e tento mineiro)
 *
 * Obs.: Se o jogador não tiver nome, um nome implícito será dado
 *
 * @author Chester
 *
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
				switch (args[1]) {
					case "PUB":
						boolean baralhoLimpo = args[2].charAt(0) == 'T';
						boolean manilhaVelha = args[2].charAt(1) == 'T';
						boolean tentoMineiro = args[2].charAt(2) == 'T';
						if (baralhoLimpo && manilhaVelha) {
							j.println("X TT");
						} else {
							Sala s = Sala.colocaEmSalaPublica(j, baralhoLimpo, manilhaVelha, tentoMineiro);
							s.mandaInfoParaTodos();
						}
						break;
						// TODO: implementar comandos para sala privada
					default:
						j.println("X AI");
				}
//				Sala s = Sala.getSala(Integer.parseInt(args[1]));
//				if (s == null) {
//					j.println("X SI");
//				} else if (j.getNome().equals("unnamed")) {
//					j.println("X NO");
//				} else if (s.adiciona(j)) {
//					j.querJogar = false;
//					// j.enviaTexto("A "+s.getNumSala()+" "+s.getPosicao(j));
//					// s.notificaJogadores("E "+s.getPosicao(j)+"
//					// "+j.getNome());
//					j.println("E "+s.getNumSala());
//					s.notificaJogadores(s.getInfo());
//				} else {
//					j.println("X CH");
//				}
			}
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			j.println("X AI");
		}

	}

}
