package me.chester.test;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

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

public class JogadorMock extends Jogador {

	private boolean aceitaMao11 = true;
	private boolean aceitaAumento = true;

	int cartaAtual;

	@Override
	public void inicioMao() {
		System.out.println("iniciomao" + this);
		cartaAtual = 0;
	}

	@Override
	public void vez(Jogador j, boolean podeFechada) {
		final Jogador jogador = this;
		System.out.println("vez " + j + "," + jogador);
		if (j.equals(this)) {
			new Thread() {
				@Override
				public void run() {
					jogo.jogaCarta(jogador, jogador.getCartas()[cartaAtual++]);
				}
			}.start();
		}
	}

	@Override
	public void pediuAumentoAposta(Jogador j, int valor) {
		final Jogador jogador = this;
		if (j.getEquipe() != this.getEquipe()) {
			new Thread() {
				@Override
				public void run() {
					jogo.respondeAumento(jogador, aceitaAumento);
				}
			}.start();
		}
	}

	@Override
	public void informaMao11(Carta[] cartasParceiro) {
		final Jogador jogador = this;
		new Thread() {
			@Override
			public void run() {
				jogo.decideMao11(jogador, aceitaMao11);
			}
		}.start();
	}

	@Override
	public void aceitouAumentoAposta(Jogador j, int valor) {
	}

	@Override
	public void cartaJogada(Jogador j, Carta c) {
	}

	@Override
	public void decidiuMao11(Jogador j, boolean aceita) {
	}

	@Override
	public void inicioPartida(int p1, int p2) {
	}

	@Override
	public void jogoAbortado(int posicao) {
	}

	@Override
	public void jogoFechado(int numEquipeVencedora) {
	}

	@Override
	public void maoFechada(int[] pontosEquipe) {
	}

	@Override
	public void recusouAumentoAposta(Jogador j) {
	}

	@Override
	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
	}

}
