package me.chester.minitruco.android;

import java.util.Date;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * Balãozinho que aparece para indicar que um jogador pediu truco, aceitou, etc.
 * <p>
 * Não é preciso instanciar, pois só existe um balão no jogo.
 * 
 * @author chester
 * 
 */
public class Balao {

	private static String frase = null;

	private static long timestampFim = System.currentTimeMillis();

	private static int posicao = 1;

	/**
	 * Faz com que o balão mostre uma frase por um tempo para um jogador
	 * 
	 * @param frase
	 *            texto no balão
	 * @param posicao
	 *            posição (1 a 4) do jogador que "dirá" a frase
	 * @param tempoMS
	 *            tempo em que ela aparecerá
	 */
	public static void diz(String frase, int posicao, int tempoMS) {
		Balao.timestampFim = System.currentTimeMillis() + tempoMS;
		Balao.frase = frase;
		Balao.posicao = posicao;
		MesaView.notificaAnimacao(Balao.timestampFim);
	}

	/**
	 * Desenha o balão no lugar certo, se ele estiver visível
	 * 
	 * @param canvas
	 *            canvas onde ele será (ou não) desenhado.
	 */
	public static void draw(Canvas canvas) {
		if (frase != null && timestampFim > System.currentTimeMillis()) {

			// Determina o tamanho e a posição do balão e o quadrante da
			// ponta
			final int MARGEM_BALAO_LEFT = 10;
			final int MARGEM_BALAO_TOP = 3;
			Paint paintFonte = new Paint();
			Rect bounds = new Rect();
			paintFonte.setColor(Color.BLACK);
			paintFonte.getTextBounds(frase, 0, frase.length(), bounds);

			int largBalao = bounds.width() + 2 * MARGEM_BALAO_LEFT;
			int altBalao = bounds.height() + 2 * MARGEM_BALAO_TOP;
			int x = 0, y = 0;
			int quadrantePonta = 0;
			switch (posicao) {
			case 1:
				x = (canvas.getWidth() - largBalao) / 2 - CartaVisual.largura;
				y = canvas.getHeight() - altBalao - CartaVisual.altura
						- MesaView.MARGEM - 3;
				quadrantePonta = 4;
				break;
			case 2:
				x = canvas.getWidth() - largBalao - MesaView.MARGEM - 3;
				y = (canvas.getHeight() - altBalao) / 2;
				quadrantePonta = 1;
				break;
			case 3:
				x = (canvas.getWidth() - largBalao) / 2 + CartaVisual.largura;
				y = MesaView.MARGEM + 3 + altBalao / 2;
				quadrantePonta = 2;
				break;
			case 4:
				x = MesaView.MARGEM + 3;
				y = (canvas.getHeight() - altBalao) / 2 - CartaVisual.altura;
				quadrantePonta = 3;
				break;
			}

			// O balão tem que ser branco, com uma borda preta. Como
			// ele só aparece em um refresh, vamos pela força bruta,
			// desenhando ele deslocado em torno da posição final em
			// preto e em seguida desenhando ele em branco na posição
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.BLACK);
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					if (i != 0 && j != 0) {
						desenhaBalao(canvas, x + i, y + j, largBalao, altBalao,
								quadrantePonta, paint);
					}
				}
			}
			paint.setColor(Color.WHITE);
			desenhaBalao(canvas, x, y, largBalao, altBalao, quadrantePonta,
					paint);

			// Finalmente, escreve o texto do balão
			canvas.drawText(frase, x + MARGEM_BALAO_LEFT, y + altBalao
					- MARGEM_BALAO_TOP - 2, paintFonte);

		} else {
			frase = null;
		}

	}

	/**
	 * Desenha o balão de texto do cenário
	 * 
	 * @param canvas
	 *            onde ele será desenhado
	 * @param x
	 *            esquerda
	 * @param y
	 *            topo
	 * @param largBalao
	 *            largura
	 * @param altBalao
	 *            altura
	 * @param quadrantePonta
	 *            Quadrante (cartesiano) onde aparece a ponta do balão (com
	 *            relação a ele mesmo)
	 */
	private static void desenhaBalao(Canvas canvas, int x, int y,
			int largBalao, int altBalao, int quadrantePonta, Paint paint) {
		// Elipse principal
		canvas.drawArc(new RectF(x, y, x + largBalao - 1, y + altBalao - 1), 0,
				360, false, paint);
		// Ponta (é um triângulo que desenhamos linha a linha)
		int xi;
		for (int i = 0; i < altBalao; i++) {
			if (quadrantePonta == 2 || quadrantePonta == 3) {
				xi = x + altBalao * 3 / 2 - i;
			} else {
				xi = x - altBalao * 3 / 2 + i + largBalao;
			}
			int sinaly = quadrantePonta < 3 ? -1 : 1;
			canvas.drawLine(xi, y + altBalao / 2, xi, y + altBalao / 2 + i
					* sinaly, paint);
		}
	}
}
