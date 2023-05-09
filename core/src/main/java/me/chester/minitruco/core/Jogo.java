package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Jogo (partida) em andamento (independente de estar rodando local ou
 * remotamente).
 * <p>
 * As implementações desta classe irão cuidar de executar o jogo (no caso de
 * <code>JogoLocal</code>) ou manter a comunicação com um jogo em execução
 * remota (<code>JogoRemoto</code>). Em qualquer caso, os objetos Jogador não
 * terão ciência de onde o jogo está se passando.
 * <p>
 * A classe é um Runnable para permitir tanto a execução em Thread (que "viverá"
 * o tempo de uma partida completa) quanto a chamada direta ao método run()
 * (para rodar um teste, por exemplo).
 *
 * @see JogoLocal
 *
 */
public abstract class Jogo implements Runnable {

	/**
	 * Referência para determinar a ordem das cartas no truco
	 */
	protected static final String letrasOrdenadas = "4567QJKA23";

	/**
	 * Modo do jogo: "P"aulista, "M"ineiro ou paulista com baralho "L"impo
	 */
	protected String modoStr;
	/**
	 * Modalidade do jogo (paulista, mineiro, etc.)
	 */
	protected Modo modo;

	/**
	 * Rodada que estamos jogando (de 1 a 3).
	 * <p>
	 * (as implementações devem manter atualizado)
	 */
	int numRodadaAtual;

	public Jogo(String modo) {
		this.modoStr = modo;
		switch (modo) {
			case "M":
				this.modo = new ModoMineiro();
				break;
			case "P":
				this.modo = new ModoPaulista();
				break;
			case "L":
				this.modo = new ModoBaralhoLimpo();
				break;
			default:
				throw new IllegalArgumentException("Modo deve ser M, P ou L");
		}
	}

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
	protected Jogador[] jogadores = new Jogador[4];

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

	public static String textoModo(String modo) {
		switch (modo) {
			case "P": return "Truco Paulista";
			case "M": return "Truco Mineiro";
			case "L": return "Truco Paulista com baralho limpo";
		}
		return null;
	}

	public String getModoStr() {
		return modoStr;
	}

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
	 */
	public abstract void jogaCarta(Jogador j, Carta c);

	/**
	 * Informa ao jogo o resultado de aceite daquela mão de ferro
	 *
	 * @param j
	 *            Jogador que está respondendo
	 * @param aceita
	 *            true se o jogador topa jogar, false se deixar para o parceiro
	 *            decidir
	 */
	public abstract void decideMaoDeFerro(Jogador j, boolean aceita);

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
	 * Adiciona um jogador a este jogo.
	 * <p>
	 * Ele será colocado na próxima posição disponível, e passa a receber
	 * eventos do jogo.
	 *
	 * @param jogador
	 *            Jogador que será adicionado (CPU, humano, etc)
	 * @return true se adicionou o jogador, false se não conseguiu (ex.: mesa
	 *         lotada)
	 */
	public synchronized boolean adiciona(Jogador jogador) {

		// Se for jogador, só entra se a mesa ainda tiver vaga.
		if (numJogadores == 4) {
			return false;
		}

		// Adiciona na lista e notifica a todos (incluindo ele) de sua presença
		jogadores[numJogadores] = jogador;
		numJogadores++;
		jogador.jogo = this;
		jogador.setPosicao(numJogadores);
		for (Jogador j : jogadores) {
			if (j != null) {
				j.entrouNoJogo(jogador, this);
			}
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
	 * As implementações devem atualizar (para se saber quando é mão de ferro)
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
	 * Informa se não estamos impedidos de disponibilizar aumento (por conta
	 * de ser uma mão de ferro, ou mesmo um empate acima do limite, ex.:
	 * 11x11 no Truco Paulista)
	 */
	public boolean isPlacarPermiteAumento() {
		int max = modoStr.equals("M") ? 10 : 11;
		return pontosEquipe[0] < max && pontosEquipe[1] < max;
	}

	/**
	 * @return true se este jogo não tem nenhum jogador remoto (bluetooth, etc)
	 */
	public boolean semJogadoresRemotos() {
		return false;
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
		for (Jogador j : jogadores) {
			if (j != null) {
				j.jogoAbortado(posicao);
			}
		}
	}

	/**
	 * Configuração que faz o jogador humano jogar automaticamente
	 *
	 * @return false (a não ser em JogoLocal _se_ a opção for habilitada)
	 */
	public boolean isJogoAutomatico() {
		return false;
	}

}
