package me.chester.test;

import me.chester.minitruco.core.Baralho;
import me.chester.minitruco.core.Carta;

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

/**
 * Uma versão do baralho para testes que solta cartas pré-definidas.
 */
public class BaralhoOrdenado extends Baralho {

	@Override
	public Carta sorteiaCarta() {
		return ultimoIdCarta < cartas[serieAtual].length - 1 ? new Carta(
				cartas[serieAtual][++ultimoIdCarta]) : null;
	}

	@Override
	public void embaralha() {
		if (ultimoIdCarta != -1) {
			ultimoIdCarta = -1;
			serieAtual++;
		}
	}

	private String cartas[][];

	private int ultimoIdCarta = -1;
	private int serieAtual = 0;

	/**
	 * Cria um baralho que retornará as cartas correspondentes à lista passada
	 * 
	 * @param cartas
	 *            Séries de representações string das cartas a retornar. Ex.:
	 *            {{"Ao","Kp"},{"4e"}} retornará ás de ouros e rei de paus na
	 *            primeira embaralhada e quatro de espadas nas segunda.
	 * @see me.chester.minitruco.core.Carta
	 */
	public BaralhoOrdenado(String[][] cartas) {
		super(false);
		this.cartas = cartas;
	}

}
