package me.chester.minitruco.android;

import java.util.Date;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

	private static Date timestampFim = new Date();

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
		Balao.timestampFim.setTime((new Date()).getTime() + tempoMS);
		Balao.frase = frase;
		Balao.posicao = posicao;
	}

	/**
	 * Desenha o balão no lugar certo, se ele estiver visível
	 * 
	 * @param canvas
	 *            canvas onde ele será (ou não) desenhado.
	 */
	public static void draw(Canvas canvas) {
		Log.i("draw", "" + new Date());
		Log.i("draw", "" + timestampFim);
		if (frase != null && timestampFim.after(new Date())) {

			// Determina o tamanho e a posição do balão e o quadrante da
			// ponta
			final int MARGEM_BALAO_LEFT = 10;
			final int MARGEM_BALAO_TOP = 3;
			// int largBalao = fonteBalao.stringWidth(textoBalao) + 2
			// * MARGEM_BALAO_LEFT;
			// int altBalao = fonteBalao.getHeight() + 2 * MARGEM_BALAO_TOP;
			int largBalao = 80;
			int altBalao = 30;
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
				y = (canvas.getHeight() - altBalao) / 2 + CartaVisual.altura;
				quadrantePonta = 1;
				break;
			case 3:
				x = (canvas.getWidth() - largBalao) / 2 + CartaVisual.largura;
				y = MesaView.MARGEM + 3 + altBalao;
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
			paint.setColor(Color.BLACK);
			// TODO
			// g.drawString(frase, x + MARGEM_BALAO_LEFT, y
			// + MARGEM_BALAO_TOP, Graphics.LEFT | Graphics.TOP);

		} else {
			frase = null;
		}

	}

	/**
	 * Desenha o balão de texto do cenário
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param largBalao
	 * @param altBalao
	 * @param quadrantePonta
	 *            Quadrante (cartesiano) onde aparece a ponat do balão
	 */
	private static void desenhaBalao(Canvas canvas, int x, int y,
			int largBalao, int altBalao, int quadrantePonta, Paint paint) {

		// Calcula o deslocamento correto da ponta
		int deltaX = largBalao / 2;
		int deltaY = altBalao / 2;
		if (quadrantePonta == 2 || quadrantePonta == 3)
			deltaX = -deltaX;
		if (quadrantePonta == 1 || quadrantePonta == 2)
			deltaY = -deltaY * 3;

		// Elipse principal
		canvas.drawArc(new RectF(x, y, x + largBalao - 1, y + altBalao - 1), 0,
				360, false, paint);
		// Ponta (desenhada como uma fração de elipse)
		canvas.drawArc(new RectF(x + deltaX, y + deltaY, x + largBalao - 1, y
				+ altBalao * 2 - 1), (90 * quadrantePonta) + 120, 50, false,
				paint);

	}

}
