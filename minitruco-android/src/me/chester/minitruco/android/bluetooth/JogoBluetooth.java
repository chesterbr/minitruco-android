package me.chester.minitruco.android.bluetooth;

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

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.core.Baralho;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.SituacaoJogo;
import android.util.Log;

/**
 * Representa, no cliente, o <code>Jogo</code> que está executando no servidor.
 * <p>
 * De maneira análoga à <code>JogadorBT</code>, ela converte as notificações do
 * jogador local em mensagens de texto (enviando-as ao servidor) e recebe
 * mensagens de texto do servidor, transformando-as em notificações para o
 * jogador local.
 * <p>
 * A conexão é gerenciada por <code>ClienteBT</code>, já que uma conexão pode
 * ser usada em jogos sucessivos.
 * 
 * @author chester
 * 
 */
public class JogoBluetooth extends Jogo {

	private JogadorHumano jogadorHumano;

	private ClienteBluetoothActivity clienteBT;

	/**
	 * Cria um novo proxy de jogo remoto associado a um cliente
	 * 
	 * @param clienteBT
	 *            Cliente que se conectou no jogo remoto
	 */
	public JogoBluetooth(ClienteBluetoothActivity clienteBT) {
		this.clienteBT = clienteBT;
	}

	/**
	 * Esse baralho é apenas para sortear cartas quando alguém joga uma fechada
	 * (as cartas, mesmo fechadas, têm que ser únicas)
	 */
	private Baralho baralho;

	private int numRodadaAtual;

	/**
	 * Retorna o jogador humano que está no jogo
	 * 
	 * @return objeto que representa o humano
	 */
	public JogadorHumano getJogadorHumano() {
		if (jogadorHumano == null)
			for (int i = 1; i <= 4; i++)
				if (getJogador(i) instanceof JogadorHumano)
					jogadorHumano = (JogadorHumano) getJogador(i);
		return jogadorHumano;

	}

	/**
	 * Processa uma notificação "in-game", gerando o evento apropriado no
	 * jogador humano
	 * 
	 * @param tipoNotificacao
	 *            caractere identificador
	 * @param parametros
	 *            dependem do caractere
	 */
	public void processaNotificacao(char tipoNotificacao, String parametros) {

		// Uso geral
		String[] tokens = parametros.split(" ");
		Jogador j;

		switch (tipoNotificacao) {
		case 'M':
			// Início da mão
			numRodadaAtual = 1;
			cartasJogadasPorRodada = new Carta[3][4];
			baralho = new Baralho(isBaralhoLimpo());
			// Gera as cartas e notifica
			Carta[] cartas = new Carta[3];
			for (int i = 0; i <= 2; i++) {
				cartas[i] = new Carta(tokens[i]);
				baralho.tiraDoBaralho(cartas[i]);
			}
			if (!isManilhaVelha()) {
				cartaDaMesa = new Carta(tokens[3]);
				baralho.tiraDoBaralho(cartaDaMesa);
			}
			setManilha(cartaDaMesa);
			getJogadorHumano().setCartas(cartas);
			getJogadorHumano().inicioMao();
			break;
		case 'J':
			// Recupera o jogador que jogou a carta
			int posicao = Integer.parseInt(tokens[0]);
			j = getJogador(posicao);
			// Recupera a carta jogada (isso depende do jogaodr ser local ou
			// remoto, e de a carta ser aberta ou fechada)
			Carta c;
			Log.w("MINITRUCO", "posicoes: " + getJogadorHumano().getPosicao()
					+ "," + posicao);
			if (getJogadorHumano().getPosicao() == posicao) {
				// Recupera a carta jogada pelo humano
				c = null;
				Carta[] cartasHumano = getJogadorHumano().getCartas();
				for (int i = 0; i < cartasHumano.length; i++) {
					if (cartasHumano[i].toString().equals(tokens[1])) {
						c = cartasHumano[i];
						break;
					}
				}
				// Se solicitou carta fechada, muda o status
				if (tokens.length > 2 && tokens[2].equals("T")) {
					c.setFechada(true);
				}
			} else {
				if (tokens.length > 1) {
					// Cria a carta jogada pela CPU
					c = new Carta(tokens[1]);
					baralho.tiraDoBaralho(c);
				} else {
					// Carta fechada, cria uma qualquer e seta o status
					c = baralho.sorteiaCarta();
					c.setFechada(true);
				}
			}
			// Guarda a carta no array de cartas jogadas, para consulta
			cartasJogadasPorRodada[numRodadaAtual - 1][posicao - 1] = c;
			// Avisa o jogador humano que a jogada foi feita
			getJogadorHumano().cartaJogada(j, c);
			break;
		case 'V':
			// Informa o jogador humano que é a vez de alguém
			getJogadorHumano().vez(getJogador(Integer.parseInt(tokens[0])),
					tokens[1].equals("T"));
			break;
		case 'T':
			getJogadorHumano().pediuAumentoAposta(
					getJogador(Integer.parseInt(tokens[0])),
					Integer.parseInt(tokens[1]));
			break;
		case 'D':
			getJogadorHumano().aceitouAumentoAposta(
					getJogador(Integer.parseInt(tokens[0])),
					Integer.parseInt(tokens[1]));
			break;
		case 'C':
			getJogadorHumano().recusouAumentoAposta(
					getJogador(Integer.parseInt(tokens[0])));
			break;
		case 'H':
			// Alguém aceitou mão de 11, informa
			getJogadorHumano().decidiuMao11(
					getJogador(Integer.parseInt(tokens[0])),
					tokens[1].equals("T"));
			break;
		case 'F':
			// Mão de 11. Recupera as cartas do parceiro e informa o jogador
			Carta[] cartasMao11 = new Carta[3];
			for (int i = 0; i <= 2; i++) {
				cartasMao11[i] = new Carta(tokens[i]);
			}
			getJogadorHumano().informaMao11(cartasMao11);
			break;
		case 'R':
			// Fim de rodada, recupera o resultado e o jogador que torna
			int resultado = Integer.parseInt(tokens[0]);
			j = getJogador(Integer.parseInt(tokens[1]));
			getJogadorHumano().rodadaFechada(numRodadaAtual, resultado, j);
			numRodadaAtual++;
			break;
		case 'O':
			// Fim de mão, recupera os placares
			pontosEquipe[0] = Integer.parseInt(tokens[0]);
			pontosEquipe[1] = Integer.parseInt(tokens[1]);
			getJogadorHumano().maoFechada(pontosEquipe);
			break;
		case 'G':
			// Fim de jogo
			getJogadorHumano().jogoFechado(Integer.parseInt(parametros));

			break;
		case 'A':
			// Jogo abortado por alguém
			getJogadorHumano().jogoAbortado(Integer.parseInt(parametros));
			break;
		}
	}

	/**
	 * Não implementado em jogo bluetooth (apenas o JogadorCPU usa isso, e ele
	 * não participa desses jogos).
	 */
	public void atualizaSituacao(SituacaoJogo s, Jogador j) {
		// não faz nada
	}

	public boolean isBaralhoLimpo() {
		return clienteBT.regras.charAt(0) == 'T';
	}

	public boolean isManilhaVelha() {
		return clienteBT.regras.charAt(1) == 'T';
	}

	public void run() {
		// Notifica o jogador humano que a partida começou
		getJogadorHumano().inicioPartida(0, 0);
	}

	public void jogaCarta(Jogador j, Carta c) {
		clienteBT.enviaLinha("J " + c + (c.isFechada() ? " T" : ""));
	}

	public void decideMao11(Jogador j, boolean aceita) {
		clienteBT.enviaLinha("H " + (aceita ? "T" : "F"));
	}

	public void aumentaAposta(Jogador j) {
		if (j.equals(getJogadorHumano()))
			clienteBT.enviaLinha("T");
	}

	public void respondeAumento(Jogador j, boolean aceitou) {
		if (j.equals(getJogadorHumano())) {
			if (aceitou)
				clienteBT.enviaLinha("D");
			else
				clienteBT.enviaLinha("C");
		}
	}

	public void enviaMensagem(Jogador j, String s) {
	}

	/**
	 * Se o jogador humano aborta, encaminha para o jogo "de verdade"
	 */
	@Override
	public void abortaJogo(int posicao) {
		if (posicao == 1) {
			clienteBT.enviaLinha("A");
		}
	}

}
