package me.chester.minitruco.android;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/*
 * Copyright © 2005-2011 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (na sua opnião) qualquer versão.
 *
 * Este programa é distribuido na esperança que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO
 * a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença
 * Pública Geral GNU para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa, se não, escreva para a Fundação do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Representa um jogador controlado pelo celular que está rodando o jogo.
 * <p>
 * Esta classe trabalha mais como um "marker", porque a Partida (que entra como
 * <code>Interessado</code>) no jogo é que efetua todas as ações em nome do
 * jogador.
 * 
 * @author chester
 * 
 */
public class JogadorHumano extends Jogador {

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

	public void inicioPartida() {

	}

	public void jogoAbortado(int posicao) {

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

}
