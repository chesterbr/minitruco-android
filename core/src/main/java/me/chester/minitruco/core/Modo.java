package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2011 Guilherme Caram <gcaram@gmail.com> */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Encapsula diferenças de pontuação entre as diferentes modalidades
 * de truco (paulista, mineiro, baralho limpo)
 */
public interface Modo {

	int pontuacaoQueDeterminaMaoDeFerro();

	int valorInicialDaMao();

	int valorDaMaoDeFerro();

	/**
	 * @return valor para o qual um jogo cujo valor seja valorMao vai se houver um aumento,
	 *         ou 0 se estivermos num valor que não permite aumento (ex.: 12 no truco paulista)
	 */
	int valorSeHouverAumento(int valorMao);

	boolean isBaralhoLimpo();

	/**
	 * @return True para manilhas fixas (sem "vira")
	 */
	public abstract boolean isManilhaVelha();
}
