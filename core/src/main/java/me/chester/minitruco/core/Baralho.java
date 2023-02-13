package me.chester.minitruco.core;

/*
 * Copyright © 2005-2012 Carlos Duarte do Nascimento "Chester" <cd@pobox.com>
 * Todos os direitos reservados.
 *
 * A redistribuição e o uso nas formas binária e código fonte, com ou sem
 * modificações, são permitidos contanto que as condições abaixo sejam
 * cumpridas:
 * 
 * - Redistribuições do código fonte devem conter o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente.
 * 
 * - Redistribuições na forma binária devem reproduzir o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente na documentação e/ou materiais fornecidos com
 *   a distribuição.
 *   
 * - Nem o nome do Chester, nem o nome dos contribuidores podem ser
 *   utilizados para endossar ou promover produtos derivados deste
 *   software sem autorização prévia específica por escrito.
 * 
 * ESTE SOFTWARE É FORNECIDO PELOS DETENTORES DE DIREITOS AUTORAIS E
 * CONTRIBUIDORES "COMO ESTÁ", ISENTO DE GARANTIAS EXPRESSAS OU TÁCITAS,
 * INCLUINDO, SEM LIMITAÇÃO, QUAISQUER GARANTIAS IMPLÍCITAS DE
 * COMERCIABILIDADE OU DE ADEQUAÇÃO A FINALIDADES ESPECÍFICAS. EM NENHUMA
 * HIPÓTESE OS TITULARES DE DIREITOS AUTORAIS E CONTRIBUIDORES SERÃO
 * RESPONSÁVEIS POR QUAISQUER DANOS, DIRETOS, INDIRETOS, INCIDENTAIS,
 * ESPECIAIS, EXEMPLARES OU CONSEQUENTES, (INCLUINDO, SEM LIMITAÇÃO,
 * FORNECIMENTO DE BENS OU SERVIÇOS SUBSTITUTOS, PERDA DE USO OU DADOS,
 * LUCROS CESSANTES, OU INTERRUPÇÃO DE ATIVIDADES), CAUSADOS POR QUAISQUER
 * MOTIVOS E SOB QUALQUER TEORIA DE RESPONSABILIDADE, SEJA RESPONSABILIDADE
 * CONTRATUAL, RESTRITA, ILÍCITO CIVIL, OU QUALQUER OUTRA, COMO DECORRÊNCIA
 * DE USO DESTE SOFTWARE, MESMO QUE HOUVESSEM SIDO AVISADOS DA
 * POSSIBILIDADE DE TAIS DANOS.
 * 
 */

import java.util.Random;
import java.util.Vector;

/**
 * Gerencia as cartas já distribuídas, garantindo que não se sorteie duas vezes
 * a mesma carta.
 * 
 * 
 */
public class Baralho {

	private final boolean limpo;

	private final Random random = new Random();

	private Vector<Carta> sorteadas = new Vector<Carta>();

	/**
	 * Cria um novo bararalho de truco
	 * 
	 * @param isLimpo
	 *            true para baralho limpo (sem 4, 5, 6 e 7), false para sujo
	 *            (default)
	 */
	public Baralho(boolean isLimpo) {
		limpo = isLimpo;
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
	 * 
	 * @param limiteSuperior
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
