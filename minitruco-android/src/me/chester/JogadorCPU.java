package me.chester;

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

import java.util.Vector;

import android.util.Log;

/**
 * Jogador controlado pelo celular ou pelo servidor.
 * <p>
 * É preciso "plugar" uma estratégia para que o jogador funcione.
 * 
 * @author Chester
 * @see Estrategia
 * 
 */
public class JogadorCPU extends Jogador implements Runnable {

	/**
	 * Cria um novo jogador CPU, usando a estratégia fornecida.
	 * 
	 * @param estrategia
	 *            Estratégia a ser adotada por este jogador
	 */
	public JogadorCPU(Estrategia estrategia) {
		this.estrategia = estrategia;
		this.setNome(estrategia.getNomeEstrategia());
		this.thread = new Thread(this);
		thread.start();
	}

	/**
	 * Cria um novo jogador CPU, buscando a estratégia pelo nome.
	 * <p>
	 * 
	 * @param nomeEstrategia
	 *            Nome da estratégia (ex.: "Willian")
	 */
	public JogadorCPU(String nomeEstrategia) {
		this(criaEstrategiaPeloNome(nomeEstrategia));
	}

	/**
	 * Cria um novo jogador CPU, com uma estratégia aleatória
	 */
	public JogadorCPU() {
		this(criaEstrategiaPeloNome("x"));
	}

	/**
	 * Thread que processa as notificações recebidas pelo jogador (para não
	 * travar o jogo enquanto isso oacontece)
	 */
	Thread thread;

	/**
	 * Estrategia que está controlando este jogador
	 */
	private Estrategia estrategia;

	/**
	 * Situação atual do jogo (para o estrategia)
	 */
	SituacaoJogo situacaoJogo = new SituacaoJogo();

	/**
	 * Quantidade de jogadores cuja resposta estamos esperando para um pedido de
	 * truco.
	 */
	private int numRespostasAguardando = 0;

	/**
	 * Sinaliza se os adversários aceitaram um pedido de truco
	 */
	private boolean aceitaramTruco;

	/**
	 * Indica que é a vez deste jogador (para que a thread execute a jogada)
	 */
	private boolean minhaVez = false;

	/**
	 * Indica se o jogador pode jogar uma carta fechada (sendo a vez dele)
	 */
	private boolean podeFechada = false;

	public void vez(Jogador j, boolean podeFechada) {
		if (this.equals(j)) {
			this.podeFechada = podeFechada;
			this.minhaVez = true;
		}
	}

	public void run() {

		while (jogo == null || !jogo.jogoFinalizado) {
			sleep(100);

			if (minhaVez) {

				Log.i("JogadorCPU", "Jogador " + this.getPosicao()
						+ " viu que e' sua vez");

				// Dá um tempinho, pra fingir que está "pensando"
				try {
					Thread.sleep(Math.abs(random.nextInt()) % 250 + 200);
				} catch (InterruptedException e) {
					// Nada, apenas timing...
				}

				// Atualiza a situação do jogo (incluindo as cartas na mão)
				atualizaSituacaoJogo();
				situacaoJogo.podeFechada = podeFechada;

				// Solicita que o estrategia jogue
				int posCarta = estrategia.joga(situacaoJogo);

				// Por enquanto vamos ignorar pedidos de truco
				/*
				 * 
				 * // Se a estratégia pediu truco, processa, e, após tudo
				 * resolvido, repete a jogada if ((posCarta == -1) &&
				 * (situacaoJogo.valorProximaAposta != 0)) { // Faz a
				 * solicitação de truco numa nova thread // (usando o próprio
				 * JogadorCPU como Runnable - era uma inner // class, mas
				 * otimizei para reduzir o .jar) aceitaramTruco = false;
				 * numRespostasAguardando = 2; Thread t = new Thread(this);
				 * t.start(); // Aguarda pelas respostas while
				 * ((numRespostasAguardando > 0) && (this.jogo != null) &&
				 * !this.jogo.jogoFinalizado) { try { Thread.sleep(100); } catch
				 * (InterruptedException e) { // Não precisa tratar, basta
				 * seguir } } // Se não aceitaram, desencana... if
				 * (!aceitaramTruco) return; // ...caso contrário, vamos seguir
				 * o jogo // atualizaSituacaoJogo();
				 * situacaoJogo.valorProximaAposta = 0; posCarta =
				 * estrategia.joga(situacaoJogo); }
				 */
				// Se a estratégia pediu truco fora de hora, ignora e joga a
				// primeira carta
				if (posCarta == -1) {
					posCarta = 0;
				}

				// Joga a carta selecionada e remove ela da mão
				boolean isFechada = posCarta >= 10;
				if (isFechada) {
					posCarta -= 10;
				}

				Carta c = (Carta) cartasRestantes.elementAt(posCarta);
				c.setFechada(isFechada && podeFechada);
				cartasRestantes.removeElement(c);
				Log.i("JogadorCPU", "Jogador " + this.getPosicao()
						+ " vai pedir para jogar " + c);
				jogo.jogaCarta(this, c);
				minhaVez = false;

			}
		}

	}

	// /**
	// * Envia a notificação de aumento de aposta.
	// * <p>
	// * É feito em thread separada para que o vez() aguarde as respostas sem se
	// * perder.
	// */
	// public void run() {
	// jogo.aumentaAposta(this);
	// }

	public void pediuAumentoAposta(Jogador j, int valor) {

		// Notifica o estrategia
		estrategia.pediuAumentoAposta(j.getPosicao(), valor);

		// Se foi a equipe oposta que pediu, gera uma resposta
		if (j.getEquipe() == this.getEquipeAdversaria()) {
			atualizaSituacaoJogo();
			// O if e o synchronzied garantem que, se um jogador aceitar o
			// truco, o estrategia do outro não é consultado (caso o fosse, ele
			// receberia informacoes posteriores ao aceite)
			synchronized (jogo) {
				if (situacaoJogo.posJogadorPedindoAumento != 0) {
					boolean resposta = estrategia.aceitaTruco(situacaoJogo);
					jogo.respondeAumento(this, resposta);
				}
			}
		}
	}

	/**
	 * Atualiza a situação do jogo (para as estratégias)
	 */
	private void atualizaSituacaoJogo() {
		jogo.atualizaSituacao(situacaoJogo, this);
		if (jogo.isAlguemTem11Pontos()) {
			situacaoJogo.valorProximaAposta = 0;
		} else {
			situacaoJogo.valorProximaAposta = valorProximaAposta;
		}
		int numCartas = cartasRestantes.size();
		situacaoJogo.cartasJogador = new Carta[numCartas];
		for (int i = 0; i < numCartas; i++) {
			Carta c = (Carta) cartasRestantes.elementAt(i);
			situacaoJogo.cartasJogador[i] = new Carta(c.getLetra(), c
					.getNaipe());
		}
	}

	int valorProximaAposta;

	public void aceitouAumentoAposta(Jogador j, int valor) {

		// Notifica o estrategia
		estrategia.aceitouAumentoAposta(j.getPosicao(), valor);

		// Se estou esperando resposta, contabiliza
		if (numRespostasAguardando > 0) {
			numRespostasAguardando = 0;
			aceitaramTruco = true;
		}

		if (j.getEquipe() == this.getEquipe()) {
			// Nós aceitamos um truco, então podemos aumentar
			// (i.e., se foi truco, podemos pedir 6, se for 6, podemos pedir 9,
			// etc.) até o limite de 12
			if (valor != 12) {
				valorProximaAposta = valor + 3;
			}
		} else {
			// Eles aceitaram um truco, temos que esperar eles pedirem
			valorProximaAposta = 0;
		}

	}

	public void recusouAumentoAposta(Jogador j) {

		// Notifica o estrategia
		estrategia.recusouAumentoAposta(j.getPosicao());

		// Se estivermos aguardando resposta, contabiliza (e deixa o adversário
		// perceber)
		if (numRespostasAguardando > 0) {
			numRespostasAguardando--;
			Thread.yield();
		}

	}

	public void jogadaRecusada(int numJogadores, int equipeTrucando,
			Jogador jogadorDaVez) {
		// Não faz nada
	}

	public void rodadaFechada(int numMao, int resultado, Jogador jogadorQueTorna) {
		// Não faz nada
	}

	public void maoFechada(int[] pontosEquipe) {
		// Não faz nada
	}

	public void jogoFechado(int numEquipeVencedora) {
		// Não faz nada
	}

	public void cartaJogada(Jogador j, Carta c) {
		// Não faz nada
	}

	public void inicioMao() {

		// Notifica o estrategia
		estrategia.inicioMao();

		// Guarda as cartas que estão na mão do jogador
		cartasRestantes.removeAllElements();
		for (int i = 0; i <= 2; i++) {
			cartasRestantes.addElement(this.getCartas()[i]);
		}

		// Libera o jogador para pedir truco (se nao estivermos em mao de 11)
		valorProximaAposta = (jogo.isAlguemTem11Pontos() ? 0 : 3);

	}

	/**
	 * Cartas que ainda não foram jogadas
	 */
	private Vector cartasRestantes = new Vector(3);

	public void inicioPartida() {
		// Avisa o estrategia
		estrategia.inicioPartida();
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		// Por ora não faz nada
	}

	public void informaMao11(Carta[] cartasParceiro) {
		// Pergunta ao estrategia se ele topa a mão de 11, devolvendo
		// a resposta para o jogo
		atualizaSituacaoJogo();
		jogo.decideMao11(this, estrategia.aceitaMao11(cartasParceiro,
				situacaoJogo));

	}

	public void jogoAbortado(int posicao) {
		// Não precisa tratar
	}

	private void sleep(int i) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
