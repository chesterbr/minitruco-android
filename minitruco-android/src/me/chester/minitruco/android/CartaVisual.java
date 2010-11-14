package me.chester.minitruco.android;

import java.util.Date;

import me.chester.minitruco.core.Carta;
import android.graphics.Canvas;
import android.util.Log;

/**
 * Uma carta "visual", isto é, o objeto estilo View (embora não seja uma View)
 * que aparece na mesa com o desenho da carta.
 * <p>
 * Não confundir com a classe Carta, que representa uma carta que faz parte de
 * um Jogo, pertence a um Jogador, etc.
 * 
 * @author chester
 * 
 */
public class CartaVisual {

	/**
	 * Objeto carta que esta carta representa. Se for null, é uma carta
	 * "fechada", isto é, que aparece virada para baixo na mesa
	 */
	private Carta carta = null;

	/**
	 * Posição da carta em relação à esquerda da mesa
	 */
	public int x;

	/**
	 * Posição da carta em relação ao topo da mesa
	 */
	public int y;

	private int destX;

	private int destY;

	private Date destTime;

	private Date ultimoTime;

	public void movePara(int x, int y) {
		this.x = x;
		this.y = y;
		ultimoTime = new Date();
	}

	public void movePara(int x, int y, int tempoMS) {
		this.destX = x;
		this.destY = y;
		ultimoTime = new Date();
		destTime = new Date();
		destTime.setTime(destTime.getTime() + tempoMS);
	}

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
				movePara((int) ((destX - x) * ratio),
						(int) ((destY - y) * ratio));
			} else {
				movePara(destX, destY);
			}
		}
		// TODO efetivamente desenhar a carta

	}
}
