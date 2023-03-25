package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2011 Guilherme Caram <gcaram@gmail.com> */

/**
 * Forma de pontuação dos tentos
 *
 */
public class TentoMineiro implements Tento {

	public int calcValorTento(int valorMao) {
		switch (valorMao) {
		case 2:
			return 4;
		case 4:
			return 8;
		case 8:
			return 10;
		case 10:
			return 12;
		}
		return 0;
	}

	public int calcValorMao(int valorMao) {
		switch (valorMao) {
		case 2:
			return 1;
		case 4:
			return 2;
		case 8:
			return 3;
		case 10:
			return 4;
		}
		return 0;
	}

	public int inicializaMao() {
		return 2;
	}

	public int inicializaPenultimaMao() {
		return 4;
	}

	public int valorPenultimaMao() {
		return 10;
	}
}
