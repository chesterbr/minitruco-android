package me.chester.test;

import me.chester.Baralho;
import me.chester.Carta;

/**
 * Uma versão do baralho para testes que solta cartas pré-definidas.
 * 
 * @author chester
 */
public class BaralhoOrdenado extends Baralho {

	@Override
	public Carta sorteiaCarta() {
		return ultimoIdCarta < cartas[serieAtual].length - 1 ? new Carta(
				cartas[serieAtual][++ultimoIdCarta]) : null;
	}

	@Override
	public void embaralha() {
		if (ultimoIdCarta != -1) {
			ultimoIdCarta = -1;
			serieAtual++;
		}
	}

	private String cartas[][];

	private int ultimoIdCarta = -1;
	private int serieAtual = 0;

	/**
	 * Cria um baralho que retornará as cartas correspondentes à lista passada
	 * 
	 * @param cartas
	 *            Séries de representações string das cartas a retornar. Ex.:
	 *            {{"Ao","Kp"},{"4e"}} retornará ás de ouros e rei de paus na
	 *            primeira embaralhada e quatro de espadas nas segunda.
	 * @see me.chester.Carta
	 */
	public BaralhoOrdenado(String[][] cartas) {
		super(false);
		this.cartas = cartas;
	}

}
