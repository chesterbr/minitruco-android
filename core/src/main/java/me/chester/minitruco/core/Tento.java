package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2011 Guilherme Caram <gcaram@gmail.com> */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Encapsula diferenças de pontuação entre as diferentes variedades
 * de truco (paulista, mineiro, etc.)
 */
public interface Tento {

	int pontuacaoQueDeterminaMaoDeFerro();

	int valorInicialDaMao();

	int valorDaMaoDeFerro();

	int valorSeHouverAumento(int valorMao);
}
