package me.chester.minitruco.android;

import java.util.Date;

import android.graphics.Canvas;
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
		timestampFim.setTime((new Date()).getTime() + tempoMS);
		Balao.frase = frase;
	}

	/**
	 * Desenha o balão no lugar certo, se ele estiver visível
	 * 
	 * @param canvas
	 *            canvas onde ele será (ou não) desenhado.
	 */
	public static void draw(Canvas canvas) {
		Log.i("draw", frase);
		Log.i("draw", "" + new Date());
		Log.i("draw", "" + timestampFim);
		if (frase != null && timestampFim.after(new Date())) {
			Log.i("draw", "entrou");
			Paint paint = new Paint();
			canvas.drawArc(new RectF(0, 0, 50, 50), 0, 360, false, paint);
			frase = null;
		}

	}

}
