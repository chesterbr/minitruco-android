package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2011 Guilherme Caram <gcaram@gmail.com> */

public interface Tento {

	int calcValorTento(int valorMao);

	int calcValorMao(int valorMao);

	int inicializaMao();

	int inicializaPenultimaMao();

	int valorPenultimaMao();
}
