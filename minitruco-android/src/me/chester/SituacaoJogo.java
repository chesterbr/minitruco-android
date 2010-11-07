package me.chester;

/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Copyright © 2007 Sandro Gasparotto (sandro.gasparoto@gmail.com)
 * (modo confronto de estratégias)
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
 * Fotografia da situação atual do jogo.
 * <p>
 * Foi isolada da classe Jogo para poder passar às <code>Estrategia</code>s a
 * situação do jogo de forma a facilitar sua implementação e, ao mesmo tempo,
 * impedir que elas trapaceiem (não dando acesso ao <code>Jogo</code>.
 * 
 * @author Chester
 * 
 */
public class SituacaoJogo {

	/**
	 * Posição do jogador. 1 e 3 são parceiros entre si, assim como 2 e 4, e
	 * jogam na ordem numérica.
	 */
	int posJogador;

	/**
	 * Rodada que estamos jogando (de 1 a 3)
	 */
	int numRodadaAtual;

	/**
	 * Resultados de cada rodada (1 para vitória da equipe 1/3, 2 para vitória
	 * da equipe 2/4 e 3 para empate)
	 */
	int resultadoRodada[] = new int[3];

	/**
	 * Valor atual da mão (1, 3, 6, 9 ou 12)
	 */
	int valorMao;

	/**
	 * Valor da mão caso o jogador peça aumento de aposta (se for 0, significa
	 * que não pode ser pedido aumento)
	 */
	int valorProximaAposta;

	/**
	 * Jogador que está pedindo aumento de aposta (pedindo truco, 6, 9 ou 12).
	 * Se for null, ninguém está pedindo
	 */
	int posJogadorPedindoAumento;

	/**
	 * Posição (1 a 4) do do jogador que abriu a rodada
	 */
	int posJogadorQueAbriuRodada;

	/**
	 * Letra da manilha (quando aplicável).
	 * <p>
	 * Esta propriedade deve ser usada APENAS para chamar o método
	 * Jogo.getValorTruco(), pois, no caso de jogo com manilha velha, seu valor
	 * não é o de uma carta
	 */
	char manilha;

	/**
	 * Valor que a proprieade manilha assume quando estamos jogando com manilha
	 * velha (não-fixa)
	 */
	static char MANILHA_INDETERMINADA = 'X';

	/**
	 * Pontos de cada equipe na partida
	 */
	int[] pontosEquipe = new int[2];

	/**
	 * Número de vaquinhas no pasto de cada equipe
	 */
	int[] vaquinhasNoPasto = new int[2];
	
	/**
	 * Para cada rodada (0-2) dá as cartas jogadas pelas 4 posicões (0-3)
	 */
	Carta[][] cartasJogadas = new Carta[3][4];

	/**
	 * Cartas que ainda estão na mão do jogador
	 */
	Carta[] cartasJogador;

	/**
	 * Determina se o baralho inclui as cartas 4, 5, 6 e 7 (true) ou não
	 * (false).
	 * <p>
	 */
	boolean baralhoSujo;

	/**
	 * Informa se vale jogar carta fechada
	 */
	boolean podeFechada;

}
