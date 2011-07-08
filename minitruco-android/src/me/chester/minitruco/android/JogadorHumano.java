package me.chester.minitruco.android;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Jogo;
import android.os.Message;
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
 * 
 * @author chester
 * 
 */
public class JogadorHumano extends Jogador {

	private TrucoActivity partida;

	private MesaView mesa;

	int valorProximaAposta;

	public JogadorHumano(TrucoActivity partida, MesaView mesa) {
		this.partida = partida;
		this.mesa = mesa;
	}

	public void aceitouAumentoAposta(Jogador j, int valor) {
		if (j.getEquipe() == 1) {
			// Nós aceitamos um truco, então podemos pedir 6, 9 ou 12
			if (valor != 12) {
				valorProximaAposta = valor + 3;
			}
		} else {
			// Eles aceitaram um truco, temos que esperar eles pedirem
			valorProximaAposta = 0;
		}
		mesa.aguardaFimAnimacoes();
		mesa.diz("aumento_sim", j.getPosicao(), 1500);
		mesa.aceitouAumentoAposta(j, valor);
	}

	public void cartaJogada(Jogador j, Carta c) {
		mesa.mostrarPerguntaMao11 = false;
		mesa.mostrarPerguntaAumento = false;
		partida.handler.sendMessage(Message.obtain(partida.handler,
				TrucoActivity.MSG_ESCONDE_BOTAO_AUMENTO));
		partida.handler.sendMessage(Message.obtain(partida.handler,
				TrucoActivity.MSG_ESCONDE_BOTAO_ABERTA_FECHADA));
		mesa.aguardaFimAnimacoes();
		mesa.descarta(c, j.getPosicao());
		Log.i("Partida", "Jogador " + j.getPosicao() + " jogou " + c);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		if (j.getPosicao() == 3 && aceita) {
			mesa.mostrarPerguntaMao11 = false;
		}
		mesa.aguardaFimAnimacoes();
		mesa.diz(aceita ? "mao11_sim" : "mao11_nao", j.getPosicao(), 1500);
	}

	@Override
	public void entrouNoJogo(Jogador i, Jogo j) {

	}

	public void informaMao11(Carta[] cartasParceiro) {
		// mesa.aguardaFimAnimacoes();
		mesa.mostraCartasMao11(cartasParceiro);
		mesa.mostrarPerguntaMao11 = true;
	}

	public void inicioMao() {
		valorProximaAposta = 3;
		mesa.aguardaFimAnimacoes();
		for (int i = 0; i <= 2; i++) {
			mesa.resultadoRodada[i] = 0;
		}
		mesa.distribuiMao();
		partida.handler.sendMessage(Message.obtain(partida.handler,
				TrucoActivity.MSG_TIRA_DESTAQUE_PLACAR));
	}

	public void inicioPartida(int placarEquipe1, int placarEquipe2) {
		partida.placar[0] = placarEquipe1;
		partida.placar[1] = placarEquipe2;
		partida.handler.sendMessage(Message.obtain(partida.handler,
				TrucoActivity.MSG_ATUALIZA_PLACAR, placarEquipe1,
				placarEquipe2));
	}

	public void jogoAbortado(int posicao) {

	}

	public void jogoFechado(int numEquipeVencedora) {
		mesa.aguardaFimAnimacoes();
		mesa.diz(numEquipeVencedora == 1 ? "vitoria" : "derrota", 1, 1000);
		mesa.aguardaFimAnimacoes();
		partida.handler.sendMessage(Message.obtain(partida.handler,
				TrucoActivity.MSG_MOSTRA_BTN_NOVA_PARTIDA));
		mesa = null;
		partida = null;
	}

	public void maoFechada(int[] pontosEquipe) {
		mesa.aguardaFimAnimacoes();
		partida.handler.sendMessage(Message.obtain(partida.handler,
				TrucoActivity.MSG_ESCONDE_BOTAO_AUMENTO));
		partida.handler.sendMessage(Message.obtain(partida.handler,
				TrucoActivity.MSG_ESCONDE_BOTAO_ABERTA_FECHADA));
		partida.handler.sendMessage(Message.obtain(partida.handler,
				TrucoActivity.MSG_ATUALIZA_PLACAR, pontosEquipe[0],
				pontosEquipe[1]));
		mesa.aguardaFimAnimacoes();
		mesa.recolheMao();

	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		mesa.aguardaFimAnimacoes();
		mesa.diz("aumento_" + valor, j.getPosicao(), 1500 + 200 * (valor / 3));
		Log.d("TrucoActivity", "Jogador " + j.getPosicao()
				+ " pediu aumento ");
		if (j.getEquipe() == 2) {
			Log.d("TrucoActivity", "pedindo para mostrar pergunta aumento");
			mesa.mostrarPerguntaAumento = true;
		}
	}

	public void recusouAumentoAposta(Jogador j) {
		mesa.aguardaFimAnimacoes();
		mesa.diz("aumento_nao", j.getPosicao(), 1300);
	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		mesa.mostrarPerguntaMao11 = false;
		mesa.mostrarPerguntaAumento = false;
		mesa.aguardaFimAnimacoes();
		mesa.atualizaResultadoRodada(numRodada, resultado, jogadorQueTorna);
	}

	public void vez(Jogador j, boolean podeFechada) {
		Log.d("TrucoActivity", "vez do jogador " + j.getPosicao());
		mesa.aguardaFimAnimacoes();
		mesa.vaiJogarFechada = false;
		boolean mostraBtnAumento = (j instanceof JogadorHumano)
				&& (valorProximaAposta > 0) && (partida.placar[0] != 11)
				&& (partida.placar[1] != 11);
		boolean mostraBtnAbertaFechada = (j instanceof JogadorHumano)
				&& podeFechada;
		partida.handler.sendMessage(Message.obtain(partida.handler,
				mostraBtnAumento ? TrucoActivity.MSG_MOSTRA_BOTAO_AUMENTO
						: TrucoActivity.MSG_ESCONDE_BOTAO_AUMENTO));
		partida.handler
				.sendMessage(Message
						.obtain(
								partida.handler,
								mostraBtnAbertaFechada ? TrucoActivity.MSG_MOSTRA_BOTAO_ABERTA_FECHADA
										: TrucoActivity.MSG_ESCONDE_BOTAO_ABERTA_FECHADA));
		mesa
				.setStatusVez(j instanceof JogadorHumano ? MesaView.STATUS_VEZ_HUMANO_OK
						: MesaView.STATUS_VEZ_OUTRO);
	}

}
