package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Corre (recusa aumento de aposta)
 *
 * 
 */
public class ComandoC extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		if (!j.jogando)
			return;
		j.getSala().getJogo().respondeAumento(j,false);
	}

}
