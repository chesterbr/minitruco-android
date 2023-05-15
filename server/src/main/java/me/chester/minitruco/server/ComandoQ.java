package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Informa ao servidor que o jogador deseja iniciar a partida na sala em que está.
 * <p>
 */

public class ComandoQ extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {

		Sala s = j.getSala();
		if (s!=null) {
			j.querJogar = true;
			s.mandaInfoParaTodos();
			s.verificaMesaCompleta();
		} else {
			j.println("X FS");
		}
	}

}
