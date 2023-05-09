package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2011 Guilherme Caram <gcaram@gmail.com> */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

public class ModoPaulista implements Modo {

	public int pontuacaoQueDeterminaMaoDeFerro() {
		return 11;
	}

	public int valorInicialDaMao() {
		return 1;
	}

	public int valorDaMaoDeFerro() {
		return 3;
	}

	public int valorSeHouverAumento(int valorMao) {
		switch (valorMao) {
		case 1:
			return 3;
		case 3:
			return 6;
		case 6:
			return 9;
		case 9:
			return 12;
		}
		return 0;
	}

	@Override
	public boolean isBaralhoLimpo() {
		return false;
	}

	@Override
	public boolean isManilhaVelha() {
		return false;
	}

}
