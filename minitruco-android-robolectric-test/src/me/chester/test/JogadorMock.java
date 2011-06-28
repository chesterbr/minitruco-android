package me.chester.test;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

public class JogadorMock extends Jogador {

	private boolean aceitaMao11 = true;
	
	@Override
	public void aceitouAumentoAposta(Jogador j, int valor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cartaJogada(Jogador j, Carta c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void decidiuMao11(Jogador j, boolean aceita) {
		// TODO Auto-generated method stub

	}

	@Override
	public void informaMao11(Carta[] cartasParceiro) {
		final Jogador jogador = this;
		new Thread() {
			@Override
			public void run() {
				jogo.decideMao11(jogador, aceitaMao11);
			}
		}.start();

	}

	int cartaAtual;

	@Override
	public void inicioMao() {
		cartaAtual = 0;
		// TODO Auto-generated method stub

	}

	@Override
	public void inicioPartida() {
		// TODO Auto-generated method stub

	}

	@Override
	public void jogoAbortado(int posicao) {
		// TODO Auto-generated method stub

	}

	@Override
	public void jogoFechado(int numEquipeVencedora) {
		// TODO Auto-generated method stub

	}

	@Override
	public void maoFechada(int[] pontosEquipe) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pediuAumentoAposta(Jogador j, int valor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void recusouAumentoAposta(Jogador j) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vez(Jogador j, boolean podeFechada) {
		final Jogador jogador = this;
		if (j.equals(this)) {
			new Thread() {
				@Override
				public void run() {
					jogo.jogaCarta(jogador, jogador.getCartas()[cartaAtual++]);
				}
			}.start();
		}
	}

}
