package me.chester.minitruco.android;

import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
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

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (oldw==0) {
			CartaVisual.ajustaTamanho(getWidth(), getHeight());
			montaBaralhoCenario();
			animacaoJogo.start();
		}
	}

	public MesaView(Context context) {
		super(context);
	}

	public MesaView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public MesaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
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

	/**
	 * Desenha o baralho do cenário (e guarda as coordenadas para cartas que
	 * entram ou saem dele
	 */
	public void montaBaralhoCenario() {

		baralhoCenario.setSize(3);
		for (int i = 0; i <= 2; i++) {
			CartaVisual c = new CartaVisual(getWidth() - CartaVisual.width
					- MARGEM - (2 - i) * 2 - 2, getHeight()
					- CartaVisual.height - MARGEM - i * 2);
			baralhoCenario.setElementAt(c, i);
			if (i == 2) {
				topBaralho = c.y;
				leftBaralho = c.x;
			} else if (i == 0) {
				topCartaDaMesa = c.y;
				leftCartaDaMesa = c.x - CartaVisual.width * 3 / 5; // TODO
				// ajustar
				// (era 5 p/
				// cartas
				// grandes,
				// 4 p/
				// pequenas
			}
		}
	}

	/**
	 * Cartas que compõem o baralhinho desenhado no cenário.
	 * <p>
	 * (não confundir com a classe Baralho, usada pela Jogo para sortear cartas)
	 */
	public Vector<CartaVisual> baralhoCenario = new Vector<CartaVisual>(3);
	
	public void distribuiMao() {

		// Limpa a mesa
//		mesa.limpa();

		// Distribui as cartas em círculo
		for (int i = 0; i <= 2; i++) {
			for (int j = 1; j <= 4; j++) {
//				if (j == 1) {
//					mesa.distribui(getCartas()[i], j, i);
//				} else {
					distribui(new CartaVisual(), j, i);
//				}
			}
		}

//		// Distribui a carta da mesa (se for manilha nova)
//		if (!jogo.isManilhaVelha()) {
//			mesa.distribuiCartaDaMesa(jogo.cartaDaMesa);
//		}
//
//		// Atualiza o placar
//		mesa.atualizaPlacar(pontosNos, pontosEles);
//
//		// Libera o jogador para pedir truco
//		valorProximaAposta = 3;
//
//		// Informa que ninguém aceitou mão de 11 (para não duplicar o balão)
//		jaAceitou = false;
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
	private void distribui(CartaVisual c, int numJogador, int i) {

		// Determina onde vamos colocar a carta (e se ela vem virada)
		int topFinal, leftFinal;
		switch (numJogador) {
		case 1:
			leftFinal = getWidth() / 2 - CartaVisual.width + i
					* (CartaVisual.width * 2 / 3);
			topFinal = getHeight() - (CartaVisual.height + MARGEM);
			// c.setVirada(true);
			break;
		case 2:
			leftFinal = getWidth() - CartaVisual.width - MARGEM;
			topFinal = getHeight() / 2 - CartaVisual.height / 2 - (i - 1) * 4;
			break;
		case 3:
			leftFinal = getWidth() / 2 - CartaVisual.width + (2 - i)
					* (CartaVisual.width * 2 / 3);
			topFinal = MARGEM;
			break;
		case 4:
			leftFinal = MARGEM;
			topFinal = getHeight() / 2 - CartaVisual.height / 2 + (i - 1) * 4;
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
		cartas.setSize(13);
		cartas.setElementAt(c, i + 3 * (numJogador - 1));
		c.movePara(topBaralho, leftBaralho);
		c.movePara(topFinal, leftFinal, 100);
	}

	/**
	 * Guarda as cartas que foram jogadas pelos jogadores
	 */
	private Vector<CartaVisual> cartasJogadas = new Vector<CartaVisual>(12);

	/**
	 * Guarda todas as cartas na mesa (jogadas, não-jogadas e a virada)
	 */
	private Vector<CartaVisual> cartas = new Vector<CartaVisual>(13);

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Fundo verde - é um clássico do miniTruco
		canvas.drawRGB(0, 255, 0);

		// Desenha as cartas na mesa (na mão ou no descarte)
		for (CartaVisual carta : cartas) {
			if (carta != null) {
				carta.draw(canvas);
			}
		}
		for (CartaVisual carta : baralhoCenario) {
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
	public static final int MARGEM = 1;

	/**
	 * Posição do baralho na mesa
	 */
	private int topBaralho, leftBaralho;

	/**
	 * Posição da carta virada na mesa
	 */
	private int topCartaDaMesa, leftCartaDaMesa;



}
