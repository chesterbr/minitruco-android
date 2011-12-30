package me.chester.minitruco.android.bluetooth;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
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
 * Jogador (no cliente) que participa de um jogo bluetooth (além do
 * JogadorHumano).
 * <p>
 * A classe não faz nada - é só para o JogadorHumano não se sentir sozinho
 * (i.e., ter a quem referenciar nos eventos remotos).
 * 
 * @author Chester
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
