package me.chester.minitruco.core;

/*
 * Copyright © 2005-2011 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença.
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
			Log.i("JogadorCPU", "Jogador " + this.getPosicao()
					+ " recebeu notificacao de vez");
			this.podeFechada = podeFechada;
			this.minhaVez = true;
		}
	}

	public void run() {

		Log.i("JogadorCPU", "JogadorCPU " + this + " (.run) iniciado");
		while (jogo == null || !jogo.jogoFinalizado) {
			sleep(100);

			if (minhaVez) {

				minhaVez = false;
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
				int posCarta;
				try {
					posCarta = estrategia.joga(situacaoJogo);
				} catch (Exception e) {
					Log.w("JogadorCPU", "Erro em joga", e);
					posCarta = 0;
				}

				// Se a estratégia pediu truco, processa e desencana de jogar
				// agora
				if ((posCarta == -1) && (situacaoJogo.valorProximaAposta != 0)) {
					// Faz a
					// solicitação de truco numa nova thread // (usando o
					// próprio
					// JogadorCPU como Runnable - era uma inner // class, mas
					// otimizei para reduzir o .jar)
					aceitaramTruco = false;
					numRespostasAguardando = 2;
					Log.i("JogadorCPU", "Jogador " + this.getPosicao()
							+ " vai aumentar aposta");
					estouAguardandoRepostaAumento = true;
					jogo.aumentaAposta(this);
					Log.i("JogadorCPU", "Jogador " + this.getPosicao()
							+ " aguardando resposta");
					continue;
				}

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

			}

			if (recebiPedidoDeAumento) {
				recebiPedidoDeAumento = false;
				atualizaSituacaoJogo();
				sleep(1000 + random.nextInt(1000));
				// O sync/if é só pra evitar resposta dupla entre 2 CPUs
				synchronized (jogo) {
					if (situacaoJogo.posJogadorPedindoAumento != 0) {
						boolean resposta = false;
						try {
							resposta = estrategia.aceitaTruco(situacaoJogo);
						} catch (Exception e) {
							Log.d("JogadorCPU", "Erro em aceite-aumento", e);
						}
						jogo.respondeAumento(this, resposta);
					}
				}
			}

			if (recebiPedidoDeMaoDe11) {
				recebiPedidoDeMaoDe11 = false;
				atualizaSituacaoJogo();
				sleep(1000 + random.nextInt(1000));
				boolean respostaMao11 = false;
				try {
					respostaMao11 = estrategia.aceitaMao11(cartasDoParceiroDaMaoDe11,
							situacaoJogo);
				} catch (Exception e) {
					Log.d("JogadorCPU", "Erro em aceite-11 no jogador"
							+ this.getPosicao(), e);
					respostaMao11 = random.nextBoolean();
				}
				jogo.decideMao11(this, respostaMao11);
			}

			if (estouAguardandoRepostaAumento && (numRespostasAguardando == 0)) {
				estouAguardandoRepostaAumento = false;
				// Se aceitaram, vamos seguir o jogo
				if (aceitaramTruco) {
					atualizaSituacaoJogo();
					situacaoJogo.valorProximaAposta = 0;
					minhaVez = true;
				}
			}

		}
		Log.i("JogadorCPU", "JogadorCPU " + this + " (.run) finalizado");

	}

	private boolean recebiPedidoDeAumento = false;
	private boolean estouAguardandoRepostaAumento = false;

	private boolean recebiPedidoDeMaoDe11 = false;

	private Carta[] cartasDoParceiroDaMaoDe11;

	public void pediuAumentoAposta(Jogador j, int valor) {
		// Notifica a estrategia
		estrategia.pediuAumentoAposta(j.getPosicao(), valor);
		// Se foi a equipe oposta que pediu, gera uma resposta
		if (j.getEquipe() == this.getEquipeAdversaria()) {
			recebiPedidoDeAumento = true;
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
	private Vector<Carta> cartasRestantes = new Vector<Carta>(3);

	public void inicioPartida() {
		// Avisa o estrategia
		estrategia.inicioPartida();
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		// Por ora não faz nada
	}

	public void informaMao11(Carta[] cartasParceiro) {
		cartasDoParceiroDaMaoDe11 = cartasParceiro;
		recebiPedidoDeMaoDe11 = true;
	}

	public void jogoAbortado(int posicao) {
		// Não precisa tratar
	}

	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			Log.i("JogadorCPU", "Interrupted during sleep", e);
		}
	}

}
