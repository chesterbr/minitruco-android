package me.chester.minitruco.android;

import me.chester.minitruco.core.Carta;

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

	public void movePara(int destinoX, int destinoY) {
		x = destinoX;
		y = destinoY;

	}

}
