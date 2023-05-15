package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Desce (confirma aumento de aposta)
 *
 * 
 */
public class ComandoD extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		if (!j.jogando)
			return;
		j.getSala().getJogo().respondeAumento(j,true);
	}

}
