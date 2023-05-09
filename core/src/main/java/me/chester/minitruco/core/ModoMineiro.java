package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2011 Guilherme Caram <gcaram@gmail.com> */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

public class ModoMineiro implements Modo {

	public int pontuacaoQueDeterminaMaoDeFerro() {
		return 10;
	}

	public int valorInicialDaMao() {
		return 2;
	}

	public int valorDaMaoDeFerro() {
		return 4;
	}

	public int valorSeHouverAumento(int valorMao) {
		switch (valorMao) {
		case 2:
			return 4;
		case 4:
			return 6;
		case 6:
			return 8;
		case 8:
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
		return true;
	}

}
