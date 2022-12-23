package me.chester.minitruco.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;

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
 * Jogador que controla o celular.
 * <p>
 * Esta classe trabalha em conjunto com uma <code>TrucoActivity</code> e uma
 * <code>MesaView</code>, que mostram o jogo ao usuário, capturam seu input e
 * executam as jogadas.
 * <p>
 * Num jogo remoto, esta carta "traduz" as posicoes recebidas pelo jogo para as
 * posicoes corretas da mesa
 * 
 * 
 */
public class JogadorHumano extends Jogador {

	private final TrucoActivity activity;

	private final MesaView mesa;

	int valorProximaAposta;

	public JogadorHumano(TrucoActivity partida, MesaView mesa) {
		this.activity = partida;
		this.mesa = mesa;
	}

	@Override
	public void cartaJogada(Jogador j, Carta c) {
		mesa.mostrarPerguntaMao11 = false;
		mesa.mostrarPerguntaAumento = false;
		mesa.setPosicaoVez(0);
		activity.handler.sendMessage(Message.obtain(activity.handler,
				TrucoActivity.MSG_ESCONDE_BOTAO_AUMENTO));
		activity.handler.sendMessage(Message.obtain(activity.handler,
				TrucoActivity.MSG_ESCONDE_BOTAO_ABERTA_FECHADA));
		mesa.descarta(c, posicaoNaTela(j));
		Log.i("Partida", "Jogador na posicao de tela " + posicaoNaTela(j)
				+ " jogou " + c);
	}

	@Override
	public void decidiuMao11(Jogador j, boolean aceita) {
		if (posicaoNaTela(j) == 3 && aceita) {
			mesa.mostrarPerguntaMao11 = false;
		}
		mesa.diz(aceita ? "mao11_sim" : "mao11_nao", posicaoNaTela(j), 1500);
	}

	@Override
	public void entrouNoJogo(Jogador i, Jogo j) {

	}

	@Override
	public void informaMao11(Carta[] cartasParceiro) {
		mesa.mostraCartasMao11(cartasParceiro);
		mesa.mostrarPerguntaMao11 = true;
	}

	@Override
	public void inicioMao() {
		valorProximaAposta = 3;
		for (int i = 0; i <= 2; i++) {
			mesa.resultadoRodada[i] = 0;
		}
		mesa.distribuiMao();
		activity.handler.sendMessage(Message.obtain(activity.handler,
				TrucoActivity.MSG_TIRA_DESTAQUE_PLACAR));
	}

	@Override
	public void inicioPartida(int placarEquipe1, int placarEquipe2) {
		incrementaEstatistica("statPartidas");
		activity.placar[0] = placarEquipe1;
		activity.placar[1] = placarEquipe2;
		activity.handler.sendMessage(Message
				.obtain(activity.handler, TrucoActivity.MSG_ATUALIZA_PLACAR,
						placarEquipe1, placarEquipe2));
	}

	@Override
	public void jogoAbortado(int posicao) {
		if (posicao != 0 && mesa != null) {
			mesa.diz("abortou", convertePosicaoJogadorParaPosicaoTela(posicao),
					1000);
			mesa.aguardaFimAnimacoes();
		}
		if (activity != null) {
			activity.jogoAbortado = true;
			activity.finish();
		}
	}

	@Override
	public void jogoFechado(int numEquipeVencedora) {
		boolean ganhei = (numEquipeVencedora == this.getEquipe());
		incrementaEstatistica(ganhei ? "statVitorias" : "statDerrotas");
		mesa.diz(ganhei ? "vitoria" : "derrota", 1, 1000);
		mesa.aguardaFimAnimacoes();
		activity.handler.sendMessage(Message.obtain(activity.handler,
				TrucoActivity.MSG_OFERECE_NOVA_PARTIDA));
	}

	@Override
	public void maoFechada(int[] pontosEquipe) {
		int pontosNos = pontosEquipe[getEquipe() - 1];
		int pontosEles = pontosEquipe[getEquipeAdversaria() - 1];
		activity.handler.sendMessage(Message.obtain(activity.handler,
				TrucoActivity.MSG_ESCONDE_BOTAO_AUMENTO));
		activity.handler.sendMessage(Message.obtain(activity.handler,
				TrucoActivity.MSG_ESCONDE_BOTAO_ABERTA_FECHADA));
		activity.handler.sendMessage(Message.obtain(activity.handler,
				TrucoActivity.MSG_ATUALIZA_PLACAR, pontosNos, pontosEles));
		mesa.recolheMao();

	}

	@Override
	public void pediuAumentoAposta(Jogador j, int valor) {
		// TODO so funciona para tento paulista
		int ordem_valor = valor / 3;
		mesa.diz("aumento_" + ordem_valor, posicaoNaTela(j),
				1500 + 200 * (valor / 3));
		if (j.getEquipe() != this.getEquipe()) {
			Log.d("TrucoActivity", "pedindo para mostrar pergunta aumento");
			mesa.mostrarPerguntaAumento = true;
		}
	}

	@Override
	public void aceitouAumentoAposta(Jogador j, int valor) {
		if (j.getEquipe() == this.getEquipe()) {
			// Num jogo sem bluetooth/etc, a CPU não aumenta, ela só
			// sinaliza a intenção de aumentar
			if (j instanceof JogadorCPU && jogo.semJogadoresRemotos()) {
				mesa.diz("aumento_quero", posicaoNaTela(j), 1500);
				return;
			}
			// Nós aceitamos um truco, então podemos pedir 6, 9 ou 12
			if (valor != 12) {
				valorProximaAposta = valor + 3;
			}
		} else {
			// Eles aceitaram um truco, temos que esperar eles pedirem
			valorProximaAposta = 0;
		}
		mesa.mostrarPerguntaAumento = false;
		mesa.diz("aumento_sim", posicaoNaTela(j), 1500);
		mesa.aceitouAumentoAposta(j, valor);
	}

	@Override
	public void recusouAumentoAposta(Jogador j) {
		mesa.diz("aumento_nao", posicaoNaTela(j), 1300);
	}

	@Override
	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		if (getEquipe() == 2) {
			// Se o humano nao é equipe 1 e não for empate, troca o resultado
			if (resultado == 1) {
				resultado = 2;
			} else if (resultado == 2) {
				resultado = 1;
			}
		}
		mesa.mostrarPerguntaMao11 = false;
		mesa.mostrarPerguntaAumento = false;
		mesa.setPosicaoVez(0);
		mesa.atualizaResultadoRodada(numRodada, resultado, jogadorQueTorna);
	}

	@Override
	public void vez(Jogador j, boolean podeFechada) {
		Log.d("TrucoActivity", "vez do jogador " + posicaoNaTela(j));
		mesa.vaiJogarFechada = false;
		boolean mostraBtnAumento = (j instanceof JogadorHumano)
				&& (valorProximaAposta > 0) && (activity.placar[0] != 11)
				&& (activity.placar[1] != 11);
		boolean mostraBtnAbertaFechada = (j instanceof JogadorHumano)
				&& podeFechada;
		activity.handler.sendMessage(Message.obtain(activity.handler,
				mostraBtnAumento ? TrucoActivity.MSG_MOSTRA_BOTAO_AUMENTO
						: TrucoActivity.MSG_ESCONDE_BOTAO_AUMENTO));
		activity.handler
				.sendMessage(Message
						.obtain(activity.handler,
								mostraBtnAbertaFechada ? TrucoActivity.MSG_MOSTRA_BOTAO_ABERTA_FECHADA
										: TrucoActivity.MSG_ESCONDE_BOTAO_ABERTA_FECHADA));
		mesa.setStatusVez(j instanceof JogadorHumano ? MesaView.STATUS_VEZ_HUMANO_OK
				: MesaView.STATUS_VEZ_OUTRO);
		mesa.setPosicaoVez(posicaoNaTela(j));
	}

	/**
	 * Soma um a uma estatística (no. de partidas jogadas, no. de vitórias,
	 * etc.)
	 * 
	 * @param chave
	 *            identificador da estatística (ex.: "statPartidas" para número
	 *            de partidas jogadas)
	 */
	private void incrementaEstatistica(String chave) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(activity);
		int partidas = preferences.getInt(chave, 0);
		Editor editor = preferences.edit();
		editor.putInt(chave, ++partidas);
		editor.commit();
	}

	/**
	 * Retorna a posição do jogador na tela.
	 * <p>
	 * Num jogo local, o 1 é o humano *e* a posição inferior da tela. Em jogos
	 * remotos, o jogador 1 pode não ser o inferior, e esta função calcula a
	 * posição que aquele jogador ocupa na tela sob o ponto de vista local.
	 * <p>
	 * 
	 * @param j
	 * @return 1 para a posição inferior, 2 para a direita, 3 para cima, 4 para
	 *         esquerda
	 */
	private int posicaoNaTela(Jogador j) {
		int pos = j.getPosicao() - this.getPosicao() + 1;
		if (pos < 1) {
			pos = pos + 4;
		}
		return pos;
	}

	private int convertePosicaoJogadorParaPosicaoTela(int posicaoJogador) {
		int pos = posicaoJogador - this.getPosicao() + 1;
		if (pos < 1) {
			pos = pos + 4;
		}
		return pos;
	}

}
