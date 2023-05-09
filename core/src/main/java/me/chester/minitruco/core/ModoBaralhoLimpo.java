package me.chester.minitruco.core;

/**
 * O modo "baralho limpo" segue as mesmas regras do paulista,
 * mas remove as cartas de 4 a 7 do baralho
 */
public class ModoBaralhoLimpo extends ModoPaulista {
    @Override
	public boolean isBaralhoLimpo() {
		return true;
	}
}
