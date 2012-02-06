package me.chester.minitruco.android.bluetooth;

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

/**
 * Jogador (no cliente) que participa de um jogo bluetooth (além do
 * JogadorHumano).
 * <p>
 * A classe não faz nada - é só para o JogadorHumano não se sentir sozinho
 * (i.e., ter a quem referenciar nos eventos remotos).
 * 
 */
public class JogadorDummy extends Jogador {

	public void aceitouAumentoAposta(Jogador j, int valor) {

	}

	public void cartaJogada(Jogador j, Carta c) {

	}

	public void decidiuMao11(Jogador j, boolean aceita) {

	}

	public void informaMao11(Carta[] cartasParceiro) {

	}

	public void inicioMao() {

	}

	public void inicioPartida(int placar1, int placar2) {

	}

	public void jogoAbortado(int posicao) {

	}

	public void jogoAbortadoPorComando() {

	}

	public void jogoFechado(int numEquipeVencedora) {

	}

	public void maoFechada(int[] pontosEquipe) {

	}

	public void pediuAumentoAposta(Jogador j, int valor) {

	}

	public void recusouAumentoAposta(Jogador j) {

	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {

	}

	public void vez(Jogador j, boolean podeFechada) {

	}

	public void setGameLevel(int gameLevel) {
		// n�o precisa tratar
	}

	public void setNickEstrategia() {
		// n�o precisa tratar
	}

	public String getNickEstrategia() {
		// n�o precisa tratar
		return getNome(); // just to avoid unexpected usage of this method
	}

	public void mensagemEstrategia(Jogador j, String s) {
		// n�o precisa tratar
	}

}
