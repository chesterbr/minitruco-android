package me.chester.minitruco.core;

import java.util.ArrayList;
import java.util.List;

import me.chester.minitruco.android.JogadorHumano;

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
 * Jogo em andamento (independente de estar rodando local ou remotamente).
 * <p>
 * As implementações desta classe irão cuidar de executar o jogo (no caso de
 * <code>JogoLocal</code>) ou manter a comunicação com um jogo em execução
 * remota (<code>JogoRemoto</code>). Em qualquer caso, os objetos Jogador não
 * terão ciência de onde o jogo está se passando.
 * <p>
 * A classe também faz o log (para fins de debug). Idealmente haveria uma classe
 * para isso, mas economizamos uns Ks deixando aqui (não pode ser na classe
 * principal porque esta depende das classes MIDP, que o servidor não tem).
 * 
 * @see JogoLocal
 * @author Chester
 * 
 */
public abstract class Jogo implements Runnable {

	/**
	 * Referência para determinar a ordem das cartas no truco
	 */
	protected static final String letrasOrdenadas = "4567QJKA23";

	/**
	 * Rodada que estamos jogando (de 1 a 3).
	 * <p>
	 * (as implementações devem manter atualizado)
	 */
	int numRodadaAtual;

	/**
	 * Calcula um valor relativo para a carta, considerando as manilhas em jogo
	 * <p>
	 * Este método está na superclasse porque, no início da rodada, toda a
	 * informação necessária consiste na manilha e em sua regra, e essas já
	 * foram transmitidas, evitando assim, dúzias de comandos.
	 * 
	 * @param c
	 *            Carta cujo valor desejamos
	 */
	public static int getValorTruco(Carta c, char letraManilha) {

		if (c.isFechada()) {
			// Cartas fechadas sempre têm valor 0
			return 0;
		}

		if (letraManilha == SituacaoJogo.MANILHA_INDETERMINADA) {
			if (c.getLetra() == '7' && c.getNaipe() == Carta.NAIPE_OUROS) {
				return 11;
			} else if (c.getLetra() == 'A'
					&& c.getNaipe() == Carta.NAIPE_ESPADAS) {
				return 12;
			} else if (c.getLetra() == '7' && c.getNaipe() == Carta.NAIPE_COPAS) {
				return 13;
			} else if (c.getLetra() == '4' && c.getNaipe() == Carta.NAIPE_PAUS) {
				return 14;
			}
		}

		if (c.getLetra() == letraManilha) {
			// Valor de 11 a 14, conforme o naipe
			switch (c.getNaipe()) {
			case Carta.NAIPE_OUROS:
				return 11;
			case Carta.NAIPE_ESPADAS:
				return 12;
			case Carta.NAIPE_COPAS:
				return 13;
			case Carta.NAIPE_PAUS:
				return 14;
			default:
				return 0;
			}
		} else {
			// Valor de 1 a 10 conforme a letra
			return letrasOrdenadas.indexOf(c.getLetra()) + 1;
		}
	}

	/**
	 * Jogadores adicionados a este jogo
	 */
	private Jogador[] jogadores = new Jogador[4];

	/**
	 * Número de jogadores adicionados até agora
	 */
	protected int numJogadores = 0;

	/**
	 * Guarda quais cartas foram jogadas em cada rodada.
	 * <p>
	 * (as implementações devem alimentar este array)
	 */
	protected Carta[][] cartasJogadasPorRodada;

	/**
	 * Inicia o jogo.
	 * <p>
	 * O jogo deve ser inicializado numa thread separada da principal, desta
	 * forma é mais conveniente que ele seja o Runnable desta thread, daí o nome
	 * do método.
	 */
	public abstract void run();

	/**
	 * Informa que o jogador vai descartar aquela carta.
	 * <p>
	 * Tem que ser a vez dele e não pode haver ninguém trucando.
	 * <p>
	 * A rotina não verifica se o jogador realmente possuía aquela carta -
	 * assume-se que as instâncias de Jogador são honestas e se protegem de
	 * clientes remotos desonestos
	 * 
	 * @param j
	 * @param c
	 */
	public abstract void jogaCarta(Jogador j, Carta c);

	/**
	 * Informa ao jogo o resultado de aceite daquela mão de 11
	 * 
	 * @param j
	 *            Jogador que está respondendo
	 * @param aceita
	 *            true se o jogador topa jogar, false se deixar para o parceiro
	 *            decidir
	 */
	public abstract void decideMao11(Jogador j, boolean aceita);

	/**
	 * Informa que o jogador solicitou um aumento de aposta ("truco", "seis",
	 * etc.).
	 * <p>
	 * Os jogadores são notificados, e a aposta será efetivamente aumentada se
	 * um dos adversários responder positivamente.
	 * <p>
	 * Observe-se que a vez do jogador fica "suspensa", já que lançamentos de
	 * cartas só são aceitos se não houver ninguém trucando. Como o jogador
	 * atualmente só pode trucar na sua vez, isso não é problema.
	 * 
	 * @param j
	 *            Jogador que está solicitando o aumento
	 */
	public abstract void aumentaAposta(Jogador j);

	/**
	 * Informa que o jogador respondeu a um pedido de aumento de aposta
	 * 
	 * @param j
	 *            Jogador que respondeu ao pedido
	 * @param aceitou
	 *            <code>true</code> se ele mandou descer, <code>false</code> se
	 *            correu
	 */
	public abstract void respondeAumento(Jogador j, boolean aceitou);

	/**
	 * Retorna as cartas jogadas por cada jogador naquela rodada
	 * 
	 * @param rodada
	 *            número de 1 a 3
	 * @return cartas jogadas naquela rodada (índice = posição do Jogador-1)
	 */
	public Carta[] getCartasDaRodada(int rodada) {
		return cartasJogadasPorRodada[rodada - 1];
	}

	/**
	 * Carta que determina a manilha (em jogo que não usa manilha velha)
	 */
	public Carta cartaDaMesa;

	/**
	 * Atualiza um objeto que contém a situação do jogo (exceto pelas cartas do
	 * jogador)
	 * 
	 * @param s
	 *            objeto a atualizar
	 * @param j
	 *            Jogador que receberá a situação
	 */
	public abstract void atualizaSituacao(SituacaoJogo s, Jogador j);

	/**
	 * @return True para jogo sem os 4,5,6 e 7.
	 */
	public abstract boolean isBaralhoLimpo();

	/**
	 * @return True para manilhas fixas (sem "vira")
	 */
	public abstract boolean isManilhaVelha();

	protected int getValorTruco(Carta c) {
		return getValorTruco(c, this.getManilha());
	}

	/**
	 * Objetos interessados em receber notificações deste jogo
	 */
	protected List<Interessado> interessados = new ArrayList<Interessado>();

	/**
	 * Adiciona um interessado (isto é, um jogador ou uma mesa) neste jogo.
	 * <p>
	 * Se for um Jogador, será colocado na próxima posição disponível. Se for
	 * uma mesa, ficará num local à parte. Em qualquer caso, o interessado passa
	 * a receber eventos do jogo.
	 * 
	 * @param i
	 *            Objeto interessado em ser adicionado ao jogo
	 * @return true se adicionou o jogador, false se não conseguiu
	 */
	public synchronized boolean adiciona(Interessado i) {

		// Se for jogador, só entra se a mesa ainda tiver vaga.
		if (i instanceof Jogador) {
			Jogador j = (Jogador) i;
			if (numJogadores == 4) {
				return false;
			}
			jogadores[numJogadores] = j;
			numJogadores++;
			j.setPosicao(numJogadores);
		}

		// Adiciona na lista e notifica a todos (incluindo ele) de sua presença
		interessados.add(i);
		for (Interessado interessado : interessados) {
			interessado.entrouNoJogo(i, this);
		}
		return true;

	}

	/**
	 * Recupera um jogador inscrito
	 * 
	 * @param posicao
	 *            valor de 1 a 4
	 * @return Objeto correspondente àquela posição
	 */
	protected Jogador getJogador(int posicao) {
		return jogadores[posicao - 1];
	}

	private char manilha;

	/**
	 * Pontos de cada equipe na partida.
	 * <p>
	 * As implementações devem atualizar (para se saber quando é mão de 11)
	 */
	protected int[] pontosEquipe = { 0, 0 };

	/**
	 * Indica que o jogo foi finalizado (para evitar que os jogadoresCPU fiquem
	 * "rodando em falso" caso o jogo seja abortado
	 */
	public boolean jogoFinalizado = false;

	/**
	 * @return Letra correspondente à manilha, ou constante em caso de manilha
	 *         fixa
	 * @see SituacaoJogo#MANILHA_INDETERMINADA
	 */
	public char getManilha() {
		return manilha;
	}

	/**
	 * Determina a letra da manilha, baseado na carta virada (o "vira").
	 * <p>
	 * Deve ser chamado a cada inicialização de mão.
	 * 
	 * @param c
	 *            Carta virada. Ignorado se for jogo com manilha velha
	 */
	public void setManilha(Carta c) {

		cartaDaMesa = c;

		if (isManilhaVelha()) {
			manilha = SituacaoJogo.MANILHA_INDETERMINADA;
			return;
		}

		int posManilha = letrasOrdenadas.indexOf(c.getLetra()) + 1;
		if (posManilha == letrasOrdenadas.length()) {
			posManilha = 0;
		}
		manilha = letrasOrdenadas.charAt(posManilha);

		// Detalhe: no baralho limpo, a manilha do vira 3 é a dama (e não o 4)
		if (isBaralhoLimpo() && c.getLetra() == '3') {
			manilha = 'Q';
		}

	}

	/**
	 * Informa se alguma das equipes tem 11 pontos (para fins de permitir
	 * trucar)
	 * <p>
	 * Isso não tem a ver com a "mão de 11" - aquela em que uma das equipes
	 * apenas tem 11. Toda mão de 11 retorna true aqui, mas o 11x11 também.
	 */
	public boolean isAlguemTem11Pontos() {
		return pontosEquipe[0] == 11 || pontosEquipe[1] == 11;
	}

	/**
	 * Indica que o jogo foi finalizado por iniciativa do jogador naquela
	 * posição.
	 * <p>
	 * Implementações podem sobrescrever (ex.: para notificar o servidor) mas
	 * devem chamar o super()
	 * <p>
	 * 
	 * @param posicao
	 *            posição (1 a 4) do jogador que motivou o abort
	 */
	public void abortaJogo(int posicao) {
		jogoFinalizado = true;
		for (Interessado interessado : interessados) {
			interessado.jogoAbortado(posicao);
		}
	}

	/**
	 * Recupera, se existir, o jogador humano deste jogo na posição 1
	 * 
	 * @return o JogadorHumano, ou <code>null</code> se não houver nenhum
	 */
	public JogadorHumano getJogadorHumano() {
		if (jogadores[0] instanceof JogadorHumano) {
			return (JogadorHumano) jogadores[0];
		} else {
			return null;
		}
	}

}