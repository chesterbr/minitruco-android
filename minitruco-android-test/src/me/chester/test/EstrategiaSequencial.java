package me.chester.test;

import me.chester.Carta;
import me.chester.Estrategia;
import me.chester.SituacaoJogo;

/**
 * Estratégia que joga as cartas na ordem oferecida (i.e., sempre a 1a. carta da
 * mão)
 * 
 * @author chester
 * 
 */
public class EstrategiaSequencial implements Estrategia {

	public boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s) {
		return false;
	}

	public boolean aceitaTruco(SituacaoJogo s) {
		return false;
	}

	public void aceitouAumentoAposta(int posJogador, int valor) {

	}

	public String getInfoEstrategia() {
		return null;
	}

	public String getNomeEstrategia() {
		return null;
	}

	public void inicioMao() {

	}

	public void inicioPartida() {

	}

	public int joga(SituacaoJogo s) {
		return 0;
	}

	public void pediuAumentoAposta(int posJogador, int valor) {

	}

	public void recusouAumentoAposta(int posJogador) {

	}

}
