package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2011 Guilherme Caram <gcaram@gmail.com> */

/**
 * Forma de pontuação dos tentos
 *
 */
public class TentoPaulista implements Tento {

	public int calcValorTento(int valorMao) {
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

	public int calcValorMao(int valorMao) {
		switch (valorMao) {
		case 1:
			return 1;
		case 3:
			return 2;
		case 6:
			return 3;
		case 9:
			return 4;
		}
		return 0;
	}

	public int inicializaMao() {
		return 1;
	}

	public int inicializaPenultimaMao() {
		return 3;
	}

	public int valorPenultimaMao() {
		return 11;
	}
}
