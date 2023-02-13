package me.chester.minitruco.android.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class JogadorBluetooth extends Jogador implements Runnable {

	private final static Logger LOGGER = Logger.getLogger("JogadorBluetooth");

	private InputStream in;
	private final BluetoothSocket socket;
	private final ServidorBluetoothActivity servidor;
	private final Thread threadProcessaMensagens;

	public JogadorBluetooth(BluetoothSocket socket,
			ServidorBluetoothActivity servidor) {
		this.socket = socket;
		this.servidor = servidor;
		threadProcessaMensagens = new Thread(this);
		threadProcessaMensagens.start();
	}

	/**
	 * Processa as mensagens vindas do cliente (i.e., do JogoBT no celular
	 * remoto), transformando-as novamente em eventos no Jogo local
	 */
	public void run() {
		// Aguarda a definição da posição (importante, pois ela determina o slot
		// no servidor para o envio de mensagens)
		while (getPosicao() == 0) {
			Thread.yield();
		}
		// Caractere lido e buffer que acumula a linha lida
		int c = 0;
		StringBuffer sbLinha = new StringBuffer();
		try {
			in = socket.getInputStream();
			// O loop dura enquanto o InputStream não for null. Sim, eu poderia
			// usar uma leitura mais eficiente, com blocking, mas aí não consigo
			// detectar o fim da partida (sem perder o primeiro caractere do
			// primeiro comando da partida seguinte)
			do {
				while (in != null && in.available() == 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// Alguém já tratou isso um dia?
					}
				}
				if (in == null)
					break;
				// Lê o próximo caractre
				c = in.read();
				if (c != BluetoothBaseActivity.SEPARADOR_REC) {
					// Acumula caracteres até formar uma linha
					sbLinha.append((char) c);
				} else {
					// Processa linhas (não-vazias)
					if (sbLinha.length() > 0) {
						LOGGER.log(Level.INFO,
								"Linha acumulada: " + sbLinha);
						char tipoNotificacao = sbLinha.charAt(0);
						String[] args = sbLinha.toString().split(" ");
						switch (tipoNotificacao) {
						case 'J':
							// Procura a carta correspondente ao parâmetro
							Carta[] cartas = getCartas();
							for (int i = 0; i < cartas.length; i++) {
								if (cartas[i] != null
										&& cartas[i].toString().equals(args[1])) {
									cartas[i].setFechada(args.length > 2
											&& args[2].equals("T"));
									jogo.jogaCarta(this, cartas[i]);
								}
							}
							break;
						case 'H':
							jogo.decideMao11(this, args[1].equals("T"));
							break;
						case 'T':
							jogo.aumentaAposta(this);
							break;
						case 'D':
							jogo.respondeAumento(this, true);
							break;
						case 'A':
							jogo.abortaJogo(getPosicao());
							break;
						case 'C':
							jogo.respondeAumento(this, false);
							break;
						}
						sbLinha.setLength(0);
					}
				}
			} while (in != null);
		} catch (IOException e) {
			LOGGER.log(Level.INFO, "Exceção no jogador (fim de jogo?)", e);
			// Não precisa tratar - ou é fim de jogo, ou o servidor cuida
		}
		LOGGER.log(Level.INFO, "encerrando loop JogadorBT");
	}

	/**
	 * Encerra a thread principal, efetivamente finalizando o JogadorBT
	 */
	void finaliza() {
		in = null;
	}

	/**
	 * Manda uma linha de texto para o celular do cliente.
	 * <p>
	 * Estas linhas representam eventos gerados pelo JogoLocal.
	 * 
	 * @param linha
	 */
	public synchronized void enviaMensagem(String linha) {
		servidor.enviaMensagem(getPosicao() - 2, linha);
	}

	// Os métodos restantes convertem as notificações do JogoLocal em mensagens
	// de texto, que serão reconvertidas em solicitações no cliente para o
	// JogadorHumano.

	public void cartaJogada(Jogador j, Carta c) {
		String param;
		if (c.isFechada()) {
			if (j.equals(this)) {
				param = " " + c + " T";
			} else {
				param = "";
			}
		} else {
			param = " " + c;
		}
		enviaMensagem("J " + j.getPosicao() + param);
	}

	public void inicioMao() {
		StringBuffer comando = new StringBuffer("M");
		for (int i = 0; i <= 2; i++)
			comando.append(" " + getCartas()[i]);
		// Se for manilha nova, também envia o "vira"
		if (!jogo.isManilhaVelha()) {
			comando.append(" " + jogo.cartaDaMesa);
		}
		enviaMensagem(comando.toString());
	}

	public void inicioPartida(int p1, int p2) {
		enviaMensagem("P");
	}

	public void vez(Jogador j, boolean podeFechada) {
		enviaMensagem("V " + j.getPosicao() + ' ' + (podeFechada ? 'T' : 'F'));
	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		enviaMensagem("T " + j.getPosicao() + ' ' + valor);
	}

	public void aceitouAumentoAposta(Jogador j, int valor) {
		enviaMensagem("D " + j.getPosicao() + ' ' + valor);
	}

	public void recusouAumentoAposta(Jogador j) {
		enviaMensagem("C " + j.getPosicao());
	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		enviaMensagem("R " + resultado + ' ' + jogadorQueTorna.getPosicao());
	}

	public void maoFechada(int[] pontosEquipe) {
		enviaMensagem("O " + pontosEquipe[0] + ' ' + pontosEquipe[1]);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		enviaMensagem("H " + j.getPosicao() + (aceita ? " T" : " F"));
	}

	public void informaMao11(Carta[] cartasParceiro) {
		StringBuffer sbComando = new StringBuffer("F ");
		for (int i = 0; i <= 2; i++) {
			sbComando.append(cartasParceiro[i]);
			if (i != 2)
				sbComando.append(' ');
		}
		enviaMensagem(sbComando.toString());
	}

	// Eventos de fim-de-jogo

	public void jogoFechado(int numEquipeVencedora) {
		enviaMensagem("G " + numEquipeVencedora);
		finaliza();
	}

	public void jogoAbortado(int posicao) {
		enviaMensagem("A " + posicao);
		finaliza();
	}

	public void jogoAbortadoPorComando() {
		enviaMensagem("AB");
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
