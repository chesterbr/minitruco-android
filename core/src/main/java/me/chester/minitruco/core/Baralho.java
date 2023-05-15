package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.util.Random;
import java.util.Vector;

/**
 * Gerencia as cartas já distribuídas, garantindo que não se sorteie duas vezes
 * a mesma carta.
 */
public class Baralho {

	private final boolean limpo;

	private final Random random = new Random();

	private Vector<Carta> sorteadas = new Vector<Carta>();

	/**
	 * Cria um novo bararalho de truco
	 *
	 * @param limpo true se o baralho for limpo (sem 4, 5, 6 e 7)
	 */

	public Baralho(boolean limpo) {
		this.limpo = limpo;
	}

	public boolean isLimpo() {
		return limpo;
	}

	/**
	 * Sorteia uma carta do baralho.
	 * <p>
	 * O método não verifica se o baralho foi todo sorteado. Para truco não há
	 * problema, mas outros jogos podem eventualmente retornar um null nesse
	 * caso.
	 *
	 * @return carta sorteada
	 */
	public Carta sorteiaCarta() {

		Carta c;
		String cartas = limpo ? "A23JQK" : "A234567JQK";
		do {
			char letra = cartas.charAt(sorteiaDeZeroA(cartas.length() - 1));
			int naipe = Carta.NAIPES[sorteiaDeZeroA(3)];
			c = new Carta(letra, naipe);
		} while (sorteadas.contains(c));
		sorteadas.addElement(c);
		return c;
	}

	/**
	 * Recolhe as cartas do baralho, zerando-o para um novo uso
	 */
	public void embaralha() {
		sorteadas = new Vector<Carta>();
	}

	/**
	 * Sortea numeros entre 0 e um valor especificado, inclusive
	 */
	private int sorteiaDeZeroA(int limiteSuperior) {
		return (random.nextInt(limiteSuperior + 1));
	}

	/**
	 * Tira uma carta do baralho, evitando que ela seja sorteada
	 *
	 * @param c
	 *            Carta a retirar
	 */
	public void tiraDoBaralho(Carta c) {
		sorteadas.addElement(c);
	}

}
