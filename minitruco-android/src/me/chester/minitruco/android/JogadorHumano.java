package me.chester.minitruco.android;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/**
 * Representa um jogador controlado pelo celular que está rodando o jogo.
 * <p>
 * Esta classe trabalha mais como um "marker", porque a Partida (que entra como
 * <code>Interessado</code>) no jogo é que efetua todas as ações em nome do
 * jogador.
 * 
 * @author chester
 * 
 */
public class JogadorHumano extends Jogador {

	public void aceitouAumentoAposta(Jogador j, int valor) {
	
	}

	public void cartaJogada(Jogador j, Carta c) {
	
	}

	public void decidiuMao11(Jogador j, boolean aceita) {

	}

	public void informaMao11(Carta[] cartasParceiro) {

	}

	public void inicioMao() {

	}

	public void inicioPartida() {

	}

	public void jogoAbortado(int posicao) {

	}

	public void jogoFechado(int numEquipeVencedora) {

	}

	public void maoFechada(int[] pontosEquipe) {

	}

	public void pediuAumentoAposta(Jogador j, int valor) {

	}

	public void recusouAumentoAposta(Jogador j) {

	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {

	}

	public void vez(Jogador j, boolean podeFechada) {

	}

}
