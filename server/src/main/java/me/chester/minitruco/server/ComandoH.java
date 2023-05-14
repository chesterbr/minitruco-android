package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Decide (aceitar ou recusar) mão de 10/11
 * <p>
 * parâmetro: T ou F (aceita ou recusa)
 * @author chester
 *
 */
public class ComandoH extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		if (!j.jogando)
			return;
		j.getSala().getJogo().decideMaoDeX(j,args[1].equals("T"));
	}

}
