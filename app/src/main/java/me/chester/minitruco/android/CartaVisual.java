package me.chester.minitruco.android;

import java.io.FileNotFoundException;
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
import android.graphics.RectF;
import android.util.Log;

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
 * Uma carta que está sendo exibida no celular. É uma subclasse separada de
 * <code>Carta</code>para não gerar uma dependência não-Android no core do jogo.
 * <p>
 * Esta classe faz o desenho da carta, executa sua animação, e ajusta sua
 * proporção para a resolução do celular.
 * 
 * 
 */
public class CartaVisual extends Carta {

	public static final int COR_MESA = Color.argb(255, 27, 142, 60);

	/**
	 * Cria uma nova carta na posição indicada
	 * 
	 * @param left
	 *            posição em relação à esquerda
	 * @param top
	 *            posição em relação ao topo
	 * @param sCarta
	 *            valor que esta carta terá (ex.: "Kc"). Se null, entra virada.
	 */
	public CartaVisual(MesaView mesa, int left, int top, String sCarta) {
		super(sCarta == null ? LETRA_NENHUMA + "" + NAIPE_NENHUM : sCarta);
		this.mesa = mesa;
		movePara(left, top);
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
		Log.d("CartaVisual", "Tamanho (largura x altura):" + largura + ","
				+ altura);
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
		mesa.aguardaFimAnimacoes();
		this.destLeft = left;
		this.destTop = top;
		ultimoTime = System.currentTimeMillis();
		destTime = ultimoTime + tempoMS;
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
			long agora = System.currentTimeMillis();
			if (agora < destTime) {
				double passado = agora - ultimoTime;
				double total = destTime - ultimoTime;
				double ratio = passado / total;
				left += (int) ((destLeft - left) * ratio);
				top += (int) ((destTop - top) * ratio);
				ultimoTime = System.currentTimeMillis();
			} else {
				movePara(destLeft, destTop);
			}
		}
		if (getBitmap() != null) {
			int raio_canto = calculaRaioCanto();
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			Rect rect = new Rect(left, top, left + largura - 1, top + altura
					- 1);
			RectF rectf = new RectF(rect);
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawRoundRect(rectf, raio_canto, raio_canto, paint);
			paint.setColor(COR_MESA);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawBitmap(getBitmap(), left, top, paint);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(1);
			canvas.drawRoundRect(rectf, raio_canto, raio_canto, paint);

			if (destacada) {
				paint.setStrokeWidth(2);
				paint.setColor(Color.BLUE);
				canvas.drawRoundRect(rectf, raio_canto, raio_canto, paint);
			} else if (escura) {
				Paint paintEscura = new Paint();
				paintEscura.setColor(Color.BLACK);
				paintEscura.setAlpha(128);
				canvas.drawRoundRect(rectf, raio_canto, raio_canto, paintEscura);
			}

		}

	}

	private int calculaRaioCanto() {
		return altura / 12;
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
		if (c == null) {
			this.setLetra(LETRA_NENHUMA);
			this.setNaipe(NAIPE_NENHUM);
			this.setFechada(true);
		} else {
			this.setLetra(c.getLetra());
			this.setNaipe(c.getNaipe());
			this.setFechada(c.isFechada());
		}
	}

	/**
	 * Recupera o bitmap correspondente a essa carta (levando em conta se ela
	 * está "fechada" ou não, e se tem um valor atribuído). Pode ser chamado
	 * múltiplas vezes, pois guarda referência do cache a cada mudança de estado
	 * 
	 * @return bitmap do cache.
	 */
	private Bitmap getBitmap() {
		if (this.bitmap == null || this.bitmap.isRecycled()) {
			String valor = "fundo";
			if ((!isFechada()) && (getLetra() != LETRA_NENHUMA)
					&& (getNaipe() != NAIPE_NENHUM)) {
				valor = this.toString();
			}
			this.bitmap = bitmapCache.get(valor);
			Bitmap bmpOrig = BitmapFactory.decodeResource(resources,
					getCartaResourceByValor(valor));
			Bitmap bmpFinal = Bitmap.createScaledBitmap(bmpOrig, largura,
					altura, true);
			bitmapCache.put(valor, bmpFinal);
			this.bitmap = bmpFinal;
		}
		return this.bitmap;
	}

	public void resetBitmap() {
		this.bitmap = null;
	}

	@Override
	public void setLetra(char letra) {
		super.setLetra(letra);
		this.bitmap = null;
	}

	@Override
	public void setNaipe(int naipe) {
		super.setNaipe(naipe);
		this.bitmap = null;
	}

	@Override
	public void setFechada(boolean fechada) {
		super.setFechada(fechada);
		this.bitmap = null;
	}

	/**
	 * Recupera o bitmap da carta a partir dos resources
	 * 
	 * @param valor
	 *            string que representa o bitmap. Ex.: "Ko" para rei de ouros.
	 * @return ID de resource do bitmap
	 */
	@SuppressWarnings("rawtypes")
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
	private long destTime = System.currentTimeMillis();

	/**
	 * Momento em que a carta avançou para o valor atual de x e y (em uma
	 * animação)
	 */
	private long ultimoTime = System.currentTimeMillis();

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
	 * Se true, mostra a carta mais escura (para simbolizar que ela é de uma
	 * rodada anterior)
	 */
	public boolean escura = false;

	/**
	 * Se true, desenha uma borda de destaque na carta (ex.: quando ela vence a
	 * rodada)
	 */
	public boolean destacada = false;

	/**
	 * Acessor dos resources da aplicação (deve ser setado antes de chamar
	 * onDraw por uma Activity que tenha acesso a getResources())
	 */
	public static Resources resources;

	/**
	 * Mesa à qual esta carta pertence
	 */
	private MesaView mesa;

}
