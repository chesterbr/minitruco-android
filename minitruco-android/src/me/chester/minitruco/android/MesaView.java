package me.chester.minitruco.android;

import java.util.Date;
import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Representa visualmente o andamento de um jogo.
 * <p>
 * No futuro, permitirá que o usuário interaja em nome de um JogadorHumano
 * inserido na partida (ex.: permitindo a ele jogar ou pedir truco em sua vez)
 * 
 * @author chester
 * 
 */
public class MesaView extends View {

	public MesaView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MesaView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MesaView(Context context) {
		super(context);
	}

	/**
	 * Cartas que estão na mesa, na ordem de empilhamento. cartas[0] é o vira,
	 * cartas[1..3] são o baralho decorativo, cartas[4..6] são as do jogador na
	 * posição 1 (inferior), cartas[7..9] o jogador 2 e assim por diante para os
	 * jogadores 3 e 4.
	 */
	public CartaVisual[] cartas = new CartaVisual[16];

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (oldw == 0) {
			// TODO tosco isso, não considera mudanças de tamanho
			// no meio do caminho

			// Define a posição e tamanho dos elementos da mesa
			CartaVisual.ajustaTamanho(getWidth(), getHeight());
			leftBaralho = this.getWidth() - CartaVisual.largura - MARGEM - 4;
			topBaralho = this.getHeight() - CartaVisual.altura - MARGEM - 4;

			// Inicializa, se necessário, as cartas em jogo
			for (int i = 0; i < cartas.length; i++) {
				if (cartas[i] == null) {
					cartas[i] = new CartaVisual(leftBaralho, topBaralho);
					cartas[i].movePara(leftBaralho, topBaralho);
				}
			}

			// Posiciona as cartas decorativas do baralho
			cartas[1].movePara(leftBaralho + 2, topBaralho + 2);
			cartas[0].movePara(leftBaralho + 4, topBaralho + 4);

			/*
			 * topCartaDaMesa = c.top; leftCartaDaMesa = c.left -
			 * CartaVisual.largura * 3 / 5; // TODO // ajustar // (era 5 p/ //
			 * cartas // grandes, // 4 p/ // pequenas
			 */

			// Inicia a thread que vai cuidar das animações (acho)
			animacaoJogo.start();
			Log.i("MesaView.onSizeChanged", "Inicializacao");

			// Inicia o jogo
			Thread t = new Thread(MenuPrincipal.jogo);
			t.start();

		}
	}

	Thread animacaoJogo = new Thread(new Runnable() {
		public void run() {
			while (true) {
				postInvalidate();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});

	public void distribuiMao() {

		// Limpa a mesa
		// mesa.limpa();

		// Distribui as cartas em círculo
		for (int i = 0; i <= 2; i++) {
			for (int j = 1; j <= 4; j++) {
				distribui(j, i);
			}
		}

		// // Distribui a carta da mesa (se for manilha nova)
		// if (!jogo.isManilhaVelha()) {
		// mesa.distribuiCartaDaMesa(jogo.cartaDaMesa);
		// }
		//
		// // Atualiza o placar
		// mesa.atualizaPlacar(pontosNos, pontosEles);
		//
		// // Libera o jogador para pedir truco
		// valorProximaAposta = 3;
		//
		// // Informa que ninguém aceitou mão de 11 (para não duplicar o balão)
		// jaAceitou = false;
	}

	/**
	 * Entrega uma carta na posição apropriada
	 * <p>
	 * 
	 * @param numJogador
	 *            Posição do jogador, de 1 a 4 (1 = humano).
	 * @param i
	 *            posição da carta na mão do jogador (0 a 2)
	 */
	private void distribui(int numJogador, int i) {

		// Determina onde vamos colocar a carta (e se ela vem virada)
		int topFinal, leftFinal;
		switch (numJogador) {
		case 1:
			leftFinal = getWidth() / 2 - CartaVisual.largura + i
					* (CartaVisual.largura * 2 / 3);
			topFinal = getHeight() - (CartaVisual.altura + MARGEM);
			// c.setVirada(true);
			break;
		case 2:
			leftFinal = getWidth() - CartaVisual.largura - MARGEM;
			topFinal = getHeight() / 2 - CartaVisual.altura / 2 - (i - 1) * 4;
			break;
		case 3:
			leftFinal = getWidth() / 2 - CartaVisual.largura + (2 - i)
					* (CartaVisual.largura * 2 / 3);
			topFinal = MARGEM;
			break;
		case 4:
			leftFinal = MARGEM;
			topFinal = getHeight() / 2 - CartaVisual.altura / 2 + (i - 1) * 4;
			break;
		default:
			leftFinal = topFinal = 0;
			// Carta da mesa
			// leftFinal = leftCartaDaMesa;
			// topFinal = topCartaDaMesa;
			// c.setVirada(true);
			// c.setCartaEmJogo(false);
			// break;
		}

		// Para o jogador da posição superior, inverte a ordem
		// (senão a exibição na mão de 11 fica bagunçada)
		if (numJogador == 3) {
			i = 2 - i;
		}

		// Adiciona a carta na mesa, em cima do baralho, e anima até a posição
		CartaVisual c = cartas[4 + i + 3 * (numJogador - 1)];
		// c.movePara(topBaralho, leftBaralho);
		c.movePara(leftFinal, topFinal, 100);
	}

	/**
	 * Recolhe as cartas jogadas de volta para o baralho
	 */
	public void recolheMao() {
		for (int i = 4; i <= 15; i++) {
			CartaVisual c = cartas[i];
			if ((c.top != topBaralho) || (c.left != leftBaralho)) {
				c.movePara(leftBaralho, topBaralho, 100);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Fundo verde - é um clássico do miniTruco
		canvas.drawRGB(0, 255, 0);

		// Desenha as cartas na mesa (na mão ou no descarte)
		// for (int i = cartas.length - 1; i >= 0; i--) {
		// if (cartas[i] != null) {
		// cartas[i].draw(canvas);
		// }
		// }
		for (CartaVisual carta : cartas) {
			if (carta != null) {
				carta.draw(canvas);
			}
		}
		// for (CartaVisual[] cartasJogador : cartasJogadores) {
		// for (CartaVisual carta : cartasJogador) {
		// if (carta != null) {
		// carta.draw(canvas);
		// }
		// }
		// }
	}

	/**
	 * Margem entre a mesa e as cartas
	 */
	public static final int MARGEM = 2;

	/**
	 * Posição do baralho na mesa
	 */
	private int topBaralho, leftBaralho;

	/**
	 * Posição da carta virada na mesa
	 */
	private int topCartaDaMesa, leftCartaDaMesa;

	/**
	 * Timestamp em que as animações em curso irão acabar
	 */
	private static Date animandoAte = new Date();

	/**
	 * Informa à mesa que uma animação começou.
	 * 
	 * @param fim
	 *            timestamp de quando a animação vai acabar
	 */
	public static void notificaAnimacao(Date fim) {
		if (animandoAte.before(fim)) {
			animandoAte = fim;
		}
	}

	/**
	 * Aguarda (em sleep da thread atual) o fim de quaisquer animações que
	 * estejam rolando.
	 */
	public static void aguardaFimAnimacoes() {
		long segundosAteFimAnimacao;
		while ((segundosAteFimAnimacao = animandoAte.getTime()
				- (new Date()).getTime()) > 0) {
			try {
				Thread.sleep(segundosAteFimAnimacao);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

}
