package me.chester.minitruco.android;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

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
 * @author chester
 * 
 */
public class JogadorHumano extends Jogador {

	private TrucoActivity activity;

	private MesaView mesa;

	int valorProximaAposta;

	public JogadorHumano(TrucoActivity partida, MesaView mesa) {
		this.activity = partida;
		this.mesa = mesa;
	}

	@Override
	public void aceitouAumentoAposta(Jogador j, int valor) {
		if (j.getEquipe() == this.getEquipe()) {
			// Nós aceitamos um truco, então podemos pedir 6, 9 ou 12
			if (valor != 12) {
				valorProximaAposta = valor + 3;
			}
		} else {
			// Eles aceitaram um truco, temos que esperar eles pedirem
			valorProximaAposta = 0;
		}
		mesa.diz("aumento_sim", posicaoNaTela(j), 1500);
		mesa.aceitouAumentoAposta(j, valor);
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
			activity.finish();
		}
	}

	@Override
	public void jogoFechado(int numEquipeVencedora) {
		boolean ganhei = (numEquipeVencedora == this.getEquipe());
		incrementaEstatistica(ganhei ? "statVitorias" : "statDerrotas");
		mesa.diz(ganhei ? "vitoria" : "derrota", 1, 1000);
		mesa.aguardaFimAnimacoes();
		if (jogo instanceof JogoLocal) {
			activity.handler.sendMessage(Message.obtain(activity.handler,
					TrucoActivity.MSG_MOSTRA_BTN_NOVA_PARTIDA));
		} else {
			activity.finish();
		}
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
