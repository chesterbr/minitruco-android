package me.chester.minitruco.android;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.chester.minitruco.core.Carta;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

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
	 * @param left
	 *            posição em relação à esquerda
	 * @param top
	 *            posição em relação ao topo
	 */
	public CartaVisual(int left, int top) {
		movePara(left, top);
		setCarta(null);
	}

	/**
	 * Cria uma carta no canto superior esquerdo
	 */
	public CartaVisual() {
		this(0, 0);
	}

	/**
	 * Ajusta a altura/largura das cartas para caberem na mesa (considerando a
	 * folga necessária para o descarte as cartas ao redor dele)
	 * 
	 * @param larguraCanvas
	 *            largura da mesa
	 * @param alturaCanvas
	 *            altura da mesa
	 */
	public static void ajustaTamanho(int larguraCanvas, int alturaCanvas) {
		// A carta "canônica" tem 180x252, e tem que caber 6 delas
		// na largura e 5 na altura. Motivo: a largura pede 1 carta para cada
		// jogador da dupla, 0.5 carta de folga e 3 cartas de área de descarte;
		// a altura idem, mas com 2 cartas na área de descarte.
		//
		// A estratégia é pegar o menor entre o ratio que faz caber na largura
		// e o que faz caber na largura
		double ratioLargura = larguraCanvas / (180 * 6.0);
		double ratioAltura = alturaCanvas / (252 * 5.0);
		double ratioCarta = Math.min(ratioLargura, ratioAltura);
		largura = (int) (180 * ratioCarta);
		altura = (int) (252 * ratioCarta);
	}

	/**
	 * Move uma carta diretamente para uma posição, sem animar.
	 * <p>
	 * Qualuqer animação em curso será cancelada.
	 * 
	 * @param left
	 *            posição em relação à esquerda
	 * @param top
	 *            posição em relação ao topo
	 */

	public void movePara(int left, int top) {
		this.left = this.destLeft = left;
		this.top = this.destTop = top;
	}

	/**
	 * Move uma carta para uma posição, animando o movimento até o destino.
	 * <p>
	 * O método só guarda esses valores - o movimento real acontece à medida em
	 * que a carta é redesenhada (isto é, no método draw).
	 * 
	 * @param left
	 *            posição em relação à esquerda
	 * @param top
	 *            posição em relação ao topo
	 * @param tempoMS
	 *            quantidade de milissegundos que a animação deve durar.
	 */
	public void movePara(int left, int top, int tempoMS) {
		MesaView.aguardaFimAnimacoes();
		this.destLeft = left;
		this.destTop = top;
		ultimoTime = new Date();
		destTime = new Date();
		destTime.setTime(destTime.getTime() + tempoMS);
		MesaView.notificaAnimacao(destTime);
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
		if (!visible) { 
			return;
		}
		// Se a carta não chegou ao destino, avançamos ela direção e na
		// velocidade necessárias para atingi-lo no momento desejado. Se
		// passamos desse momento, movemos ela direto para o destino.
		if (left != destLeft || top != destTop) {
			Date agora = new Date();
			if (agora.before(destTime)) {
				double passado = agora.getTime() - ultimoTime.getTime();
				double total = destTime.getTime() - ultimoTime.getTime();
				double ratio = passado / total;
				left += (int) ((destLeft - left) * ratio);
				top += (int) ((destTop - top) * ratio);
				ultimoTime = new Date();
			} else {
				movePara(destLeft, destTop);
			}
		}
		if (bitmap != null) {
			Paint paint = new Paint();
			paint.setColor(Color.GREEN);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawBitmap(bitmap, left, top, paint);
			Rect rect = new Rect(left, top, left + largura - 1, top + altura
					- 1);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawRect(rect, paint);
		}

	}

	/**
	 * Diz se um ponto do canvas está dentro dessa carta ou não.
	 * 
	 * @param x
	 *            coordenada x do ponto
	 * @param y
	 *            coordenada y do ponto
	 * @return true se estiver na carta, false se não.
	 */
	public boolean isDentro(float x, float y) {
		return (x >= this.left && x <= this.left + largura && y >= this.top && y <= this.top
				+ altura);
	}

	/**
	 * Associa uma carta do jogo (i.e., não visual) a esse objeto carta visual
	 * 
	 * @param c
	 *            carta a ser associada. Se <code>null</code>, desassocia a
	 *            carta visual de qualquer carta real.
	 */
	public void setCarta(Carta c) {
		if (resources == null) {
			throw new IllegalStateException(
					"CartaVisual tem que ter a propriedade resources inicializada");
		}
		this.carta = c;
		String valor = carta == null ? "fundo" : c.toString();
		this.bitmap = bitmapCache.get(valor);
		if (this.bitmap == null) {
			Bitmap bmpOrig = BitmapFactory.decodeResource(resources,
					getCartaResourceByValor(valor));
			Bitmap bmpFinal = Bitmap.createScaledBitmap(bmpOrig, largura,
					altura, true);
			bitmapCache.put(valor, bmpFinal);
			this.bitmap = bmpFinal;
		}
	}

	/**
	 * Recuper a carta do jogo associada a esse objeto visual
	 * 
	 * @return carta não-visual
	 */
	public Carta getCarta() {
		return carta;
	}

	/**
	 * Recupera o bitmap da carta a partir dos resources
	 * 
	 * @param valor
	 *            string que representa o bitmap. Ex.: "Ko" para rei de ouros.
	 * @return ID de resource do bitmap
	 */
	@SuppressWarnings("unchecked")
	private static int getCartaResourceByValor(String valor) {
		valor = valor.toLowerCase();
		try {
			for (Class c : Class.forName("me.chester.minitruco.R").getClasses()) {
				if (c.getCanonicalName().endsWith(".drawable")) {
					return c.getField("carta" + valor).getInt(null);
				}
			}
			throw new FileNotFoundException("Carta não encontrada. Valor: "
					+ valor);
		} catch (Exception e) {
			throw new RuntimeException(
					"Erro irrecuperável ao obter carta pelo valor. Valor: "
							+ valor, e);
		}
	}

	/**
	 * Guarda os bitmaps em que fizemos resize para o tamanho da carta visual
	 */
	private static Map<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();

	/**
	 * Carta do jogo que este objeto representa. Se for null, é uma carta
	 * "fechada", isto é, que aparece virada para baixo na mesa
	 */
	private Carta carta;

	/**
	 * Bitmap (já no tamanho certo) para esta carta
	 */
	private Bitmap bitmap = null;

	/**
	 * Posição do canto superior esquerdo carta em relação à esquerda da mesa
	 */
	public int left;

	/**
	 * Posição do canto superior esquerdo da carta em relação ao topo da mesa
	 */
	public int top;

	/**
	 * Altura das cartas, em pixels
	 */
	public static int largura;

	/**
	 * Largura das cartas, em pixels
	 */
	public static int altura;

	/**
	 * X em que a carta deve estar no final da animação
	 */
	private int destLeft;

	/**
	 * Y em que a carta deve estar no final da animação
	 */
	private int destTop;

	/**
	 * Momento em que a animação deve se encerrar
	 */
	private Date destTime = new Date();

	/**
	 * Momento em que a carta avançou para o valor atual de x e y (em uma
	 * animação)
	 */
	private Date ultimoTime = new Date();

	/**
	 * Se true, a carta foi lançada à mesa. Se false, está na mão de um jogador
	 * (ou é decorativa/vira)
	 */
	public boolean descartada = false;

	/**
	 * Se false, não mostra a carta
	 */
	public boolean visible = true;

	/**
	 * Acessor dos resources da aplicação (deve ser setado antes de chamar
	 * onDraw por uma Activity que tenha acesso a getResources())
	 */
	public static Resources resources;

}
