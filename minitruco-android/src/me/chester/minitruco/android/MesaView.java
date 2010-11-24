package me.chester.minitruco.android;

import java.util.Date;
import java.util.Vector;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Representa visualmente o andamento de um jogo, permitindo que o usuário
 * interaja.
 * <p>
 * Para simplificar o acesso, alguns métodos/propriedades são static - o que só
 * reitera que só deve existir uma instância desta View.
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
	 * Informa à mesa que uma animação começou (garantindo refreshes da tela
	 * enquanto ela durar).
	 * 
	 * @param fim
	 *            timestamp de quando a animação vai acabar
	 */
	public static void notificaAnimacao(Date fim) {
		comecouAnimacao = true;
		if (animandoAte.before(fim)) {
			animandoAte = fim;
		}
	}

	/**
	 * Coloca a thread chamante em sleep até que as animações acabem.
	 */
	public static void aguardaFimAnimacoes() {
		long milisecAteFimAnimacao;
		while ((milisecAteFimAnimacao = animandoAte.getTime()
				- (new Date()).getTime()) > 0) {
			try {
				Thread.sleep(milisecAteFimAnimacao);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	/**
	 * Cartas que estão na mesa, na ordem de empilhamento. cartas[0] é o vira,
	 * cartas[1..3] são o baralho decorativo, cartas[4..6] são as do jogador na
	 * posição 1 (inferior), cartas[7..9] o jogador 2 e assim por diante para os
	 * jogadores 3 e 4.
	 */
	public CartaVisual[] cartas = new CartaVisual[16];

	/**
	 * Ajusta o tamanho das cartas e sua posição de acordo com a resolução
	 */
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
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

			// Posiciona as cartas decorativas do baralho e o vira
			cartas[0].visible = false;
			cartas[0].movePara(leftBaralho - 16, topBaralho - 4);
			cartas[1].movePara(leftBaralho + 4, topBaralho + 4);
			cartas[2].movePara(leftBaralho + 2, topBaralho + 2);

			// Inicia a thread que vai cuidar das animações (acho)
			animacaoJogo.start();
			Log.i("MesaView.onSizeChanged", "Inicializacao");

			// Inicia o jogo
			Thread t = new Thread(MenuPrincipal.jogo);
			t.start();
			// Balao.diz("rosquinha", 1, 100000);

		}
	}

	/**
	 * Joga a carta tocada (se for a vez do jogador e ela não tiver sido
	 * descartada)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (vezHumano) {
			for (int i = 6; i >= 4; i--) {
				if ((!cartas[i].descartada)
						&& cartas[i].isDentro(event.getX(), event.getY())) {
					JogadorHumano jh = jogo.getJogadorHumano();
					jogo.jogaCarta(jh, cartas[i].getCarta());
					return true;
				}
			}
		}
		return false;
	}

	private long calcTempoAteFimAnimacaoMS() {
		return animandoAte.getTime() - (new Date()).getTime();
	}

	/**
	 * Thread/runnable que faz as animações acontecerem.
	 * <p>
	 */
	Thread animacaoJogo = new Thread(new Runnable() {

		// Para economizar CPU/bateria, o jogo trabalha a um máximo de 4 FPS
		// (1000/(200+50)) quando não tem nenhuma animação rolando, e sobe para
		// um máximo de 20 FPS (1000/50) quando tem (é sempre um pouco menos
		// porque periga não ter dado tempo de redesenhar a tela entre um
		// postInvalidate() e outro.
		public void run() {
			while (MenuPrincipal.jogo != null) {
				sleep(200);
				do {
					postInvalidate();
					sleep(50);
				} while (calcTempoAteFimAnimacaoMS() >= 0);
			}
		}

		private void sleep(int tempoMS) {
			try {
				Thread.sleep(tempoMS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	});

	/**
	 * Permite à partida informar que (não) é a vez de deixar o humano jogar
	 * 
	 * @param vezHumano
	 *            true se for a vez dele, false se não
	 */
	public static void setVezHumano(boolean vezHumano) {
		MesaView.vezHumano = vezHumano;
	}

	/**
	 * Entrega as cartas iniciais na mão de cada jogador
	 * 
	 * @cartas array com as três cartas do jogador na posição 1. Se
	 *         <code>null</code>, elas vêm fechadas como as dos outros
	 */
	public void distribuiMao() {

		// Distribui as cartas em círculo
		for (int i = 0; i <= 2; i++) {
			for (int j = 1; j <= 4; j++) {
				Carta c = null;
				JogadorHumano jh = jogo.getJogadorHumano();
				if (j == 1 && jh != null) {
					c = jh.getCartas()[i];
				}
				distribui(j, i, c);
			}
		}

		// Abre o vira, se for manilha nova
		if (!jogo.isManilhaVelha()) {
			cartas[0].setCarta(jogo.cartaDaMesa);
			cartas[0].visible = true;
		}
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
	 * @valor Carta (do jogo, não visual) que foi jogada. Se <code>null</code>,
	 *        entrega fechada (sem valor)
	 */
	private void distribui(int numJogador, int i, Carta carta) {

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
		c.setCarta(carta);
		// c.movePara(topBaralho, leftBaralho);
		c.movePara(leftFinal, topFinal, 100);
	}

	/**
	 * Recolhe o vira e as cartas jogadas de volta para o baralho
	 */
	public void recolheMao() {
		cartas[0].visible = false;
		for (int i = 4; i <= 15; i++) {
			CartaVisual c = cartas[i];
			if ((c.top != topBaralho) || (c.left != leftBaralho)) {
				c.movePara(leftBaralho, topBaralho, 100);
				c.setCarta(null);
				c.descartada = false;
				cartasJogadas.remove(c);
			}
		}
	}

	/**
	 * Joga a carta no meio da mesa, // TODO marcando-a como jogada.
	 * 
	 * @param c
	 */
	public void descarta(Carta c, int posicao) {

		// Coloca a carta no meio da tela, mas "puxando" na direção
		// de quem jogou
		int topFinal, leftFinal;
		topFinal = getHeight() / 2 - CartaVisual.altura / 2;
		leftFinal = getWidth() / 2 - CartaVisual.largura / 2;
		switch (posicao) {
		case 1:
			topFinal += CartaVisual.altura / 2;
			break;
		case 2:
			leftFinal += CartaVisual.largura;
			break;
		case 3:
			topFinal -= CartaVisual.altura / 2;
			break;
		case 4:
			leftFinal -= CartaVisual.largura;
			break;
		}

		// Insere um ligeiro fator aleatório, para dar uma bagunçada na mesa
		topFinal += System.currentTimeMillis() % 5 - 2;
		leftFinal += System.currentTimeMillis() % 5 - 2;

		// Pega uma carta visual naquela posição...
		CartaVisual cv = null;
		for (int i = 0; i <= 2; i++) {
			CartaVisual cvCandidata = cartas[i + 1 + posicao * 3];
			// ...que não tenha sido descartada...
			if (cvCandidata.descartada) {
				continue;
			}
			// ...e, no caso de um humano, que corresponda à carta do jogo
			cv = cvCandidata;
			if (c.equals(cvCandidata.getCarta())) {
				break;
			}
		}

		// Executa a animação de descarte
		cv.setCarta(c);
		cv.movePara(leftFinal, topFinal, 200);
		cv.descartada = true;
		cartasJogadas.addElement(cv);

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

		// Desenha as cartas que já foram jogadas (se houverem),
		// na ordem em que foram jogadas
		for (int i = 0; i < cartasJogadas.size(); i++) {
			cartasJogadas.elementAt(i).draw(canvas);
		}

		// Desenha as cartas restantes.
		for (CartaVisual carta : cartas) {
			if (carta != null && !cartasJogadas.contains(carta)) {
				carta.draw(canvas);
			}
		}

		// // Desenha a carta destacada por cima das outras, com uma firula
		// Carta cv = cartaVencedora;
		// if (cv != null) {
		// cv.desenhaCarta(g);
		// cv.destacaVitoriosa(g);
		// }

		// for (CartaVisual[] cartasJogador : cartasJogadores) {
		// for (CartaVisual carta : cartasJogador) {
		// if (carta != null) {
		// carta.draw(canvas);
		// }
		// }
		// }

		// Pontuação
		Paint p = new Paint();
		p.setColor(Color.BLACK);
		p.setTextAlign(Align.LEFT);
		canvas.drawText("Nós: " + placar[0], MARGEM, getHeight() - MARGEM, p);
		p.setTextAlign(Align.RIGHT);
		canvas.drawText("Eles: " + placar[1], getWidth() - MARGEM, MARGEM
				+ p.getTextSize(), p);

		// ícones das rodadas
		if (iconesRodadas != null) {
			for (int i = 0; i <= 2; i++) {
				canvas.drawBitmap(iconesRodadas[resultadoRodada[i]], MARGEM + i
						* (2 + iconesRodadas[0].getWidth()), MARGEM + 1, p);
			}
		}

		Balao.draw(canvas);

	}

	/**
	 * Placar atual do jogo
	 */
	int[] placar = new int[2];

	/**
	 * Cache dos ícones que informam o resultado das rodadas
	 */
	public static Bitmap[] iconesRodadas;

	/**
	 * Resultado das rodadas (0=não jogada; 1=vitória; 2=derrota; 3=empate)
	 */
	protected int[] resultadoRodada = { 0, 0, 0 };

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
	 * Diz se a thread animadora deve começar a invalidar
	 */
	private static boolean comecouAnimacao = false;

	/**
	 * Diz se é a vez do jogador humano dessa mesa
	 */
	private static boolean vezHumano = false;

	/**
	 * Jogo em que a mesa está interagindo
	 */
	public static Jogo jogo;

	/**
	 * Guarda as cartas que foram jogadas pelos jogadores (para saber em que
	 * ordem desenhar)
	 */
	private Vector<CartaVisual> cartasJogadas = new Vector<CartaVisual>(12);
}
