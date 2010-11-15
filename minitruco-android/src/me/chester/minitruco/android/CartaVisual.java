package me.chester.minitruco.android;

import java.util.Date;

import me.chester.minitruco.core.Carta;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * Uma carta "visual", isto é, o objeto estilo View (embora não seja uma View)
 * que aparece na mesa com o desenho da carta.
 * <p>
 * Não confundir com a classe Carta, que representa uma carta que faz parte de
 * um Jogo, pertence a um Jogador, etc. Esta classe faz tanto o desenho da
 * carta, quanto sua animação e o ajuste da proporção para a resolução do
 * celular.
 * 
 * @author chester
 * 
 */
public class CartaVisual {

	/**
	 * Cria uma nova carta na posição indicada
	 * 
	 * @param x
	 *            posição em relação à esquerda
	 * @param y
	 *            posição em relação ao topo
	 */
	public CartaVisual(int x, int y) {
		movePara(x, y);
		movePara(x, y, 0); // só para inicializar os timestamps
	}

	public CartaVisual() {
		this(0, 0);
	}

	/**
	 * Ajusta a altura/largura das cartas para caberem na mesa (considerando a
	 * folga necessária para o descarte as cartas ao redor dele)
	 * 
	 * @param canvas
	 *            mesa (superfície) onde as cartas serão desenhadas.
	 */
	public static void ajustaTamanho(int largura, int altura) {
		// A carta "canônica" tem 180x252, e tem que caber 6 delas
		// na largura e 5 na altura. Motivo: a largura pede 1 carta para cada
		// jogador da dupla, 0.5 carta de folga e 3 cartas de área de descarte;
		// a altura idem, mas com 2 cartas na área de descarte.
		//
		// A estratégia é pegar o menor entre o ratio que faz caber na largura
		// e o que faz caber na largura
		double ratioLargura = largura / (180 * 6.0);
		double ratioAltura = altura / (252 * 5.0);
		double ratioCarta = Math.min(ratioLargura, ratioAltura);
		width = (int) (180 * ratioCarta);
		height = (int) (252 * ratioCarta);
	}

	/**
	 * Move uma carta diretamente para uma posição, sem animar.
	 * <p>
	 * Qualuqer animação em curso será cancelada.
	 * 
	 * @param x
	 *            posição em relação à esquerda
	 * @param y
	 *            posição em relação ao topo
	 */

	public void movePara(int x, int y) {
		this.x = this.destX = x;
		this.y = this.destY = y;
	}

	/**
	 * Move uma carta para uma posição, animando o movimento até o destino.
	 * <p>
	 * O método só guarda esses valores - o movimento real acontece à medida em
	 * que a carta é redesenhada (isto é, no método draw).
	 * 
	 * @param x
	 *            posição em relação à esquerda
	 * @param y
	 *            posição em relação ao topo
	 * @param tempoMS
	 *            quantidade de milissegundos que a animação deve durar.
	 */
	public void movePara(int x, int y, int tempoMS) {
		this.destX = x;
		this.destY = y;
		ultimoTime = new Date();
		destTime = new Date();
		destTime.setTime(destTime.getTime() + tempoMS);
	}

	/**
	 * Desenha a carta em uma superfície (tipicamente, a MesaView do jogo).
	 * <p>
	 * Caso a carta esteja em meio a uma animação, atualiza sua posição para
	 * corresponder ao instante atual.
	 * 
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		// Se a carta não chegou ao destino, avançamos ela direção e na
		// velocidade necessárias para atingi-lo no momento desejado. Se
		// passamos desse momento, movemos ela direto para o destino.
		if (x != destX || y != destY) {
			Date agora = new Date();
			if (agora.before(destTime)) {
				double passado = agora.getTime() - ultimoTime.getTime();
				double total = destTime.getTime() - ultimoTime.getTime();
				double ratio = passado / total;
				Log.i("draw", "passado: " + passado);
				Log.i("draw", "total: " + total);
				Log.i("draw", "ratio: " + ratio);
				x = (int) ((destX - x) * ratio);
				y = (int) ((destY - y) * ratio);
				ultimoTime = new Date();
			} else {
				movePara(destX, destY);
			}
		}
		// TODO desenhar de verdade
		Rect rect = new Rect(x, y, x + width - 1, y + height - 1);
		Log.i("CartaViual.draw rect", x + "," + y + "," + (x + width - 1) + ","
				+ (y + height - 1));
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(rect, paint);

	}

	/**
	 * Objeto carta que esta carta representa. Se for null, é uma carta
	 * "fechada", isto é, que aparece virada para baixo na mesa
	 */
	private Carta carta = null;

	/**
	 * Posição do canto superior esquerdo carta em relação à esquerda da mesa
	 */
	public int x;

	/**
	 * Posição do canto superior esquerdo da carta em relação ao topo da mesa
	 */
	public int y;

	/**
	 * Altura das cartas, em pixels
	 */
	public static int width;

	/**
	 * Largura das cartas, em pixels
	 */
	public static int height;

	/**
	 * X em que a carta deve estar no final da animação
	 */
	private int destX;

	/**
	 * Y em que a carta deve estar no final da animação
	 */
	private int destY;

	/**
	 * Momento em que a animação deve se encerrar
	 */
	private Date destTime;

	/**
	 * Momento em que a carta avançou para o valor atual de x e y
	 */
	private Date ultimoTime;

}
