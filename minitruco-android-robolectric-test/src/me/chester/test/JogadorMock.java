package me.chester.test;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

public class JogadorMock extends Jogador {

	private boolean aceitaMao11 = true;
	private boolean aceitaAumento = true;

	int cartaAtual;

	@Override
	public void inicioMao() {
		System.out.println("iniciomao"+this);
		cartaAtual = 0;
	}

	@Override
	public void vez(Jogador j, boolean podeFechada) {
		final Jogador jogador = this;
		System.out.println("vez "+j+","+jogador);
		if (j.equals(this)) {
			new Thread() {
				@Override
				public void run() {
					jogo.jogaCarta(jogador, jogador.getCartas()[cartaAtual++]);
				}
			}.start();
		}
	}

	@Override
	public void pediuAumentoAposta(Jogador j, int valor) {
		final Jogador jogador = this;
		if (j.getEquipe() != this.getEquipe()) {
			new Thread() {
				@Override
				public void run() {
					jogo.respondeAumento(jogador, aceitaAumento);
				}
			}.start();
		}
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

	@Override
	public void aceitouAumentoAposta(Jogador j, int valor) {
	}

	@Override
	public void cartaJogada(Jogador j, Carta c) {
	}

	@Override
	public void decidiuMao11(Jogador j, boolean aceita) {
	}

	@Override
	public void inicioPartida() {
	}

	@Override
	public void jogoAbortado(int posicao) {
	}

	@Override
	public void jogoFechado(int numEquipeVencedora) {
	}

	@Override
	public void maoFechada(int[] pontosEquipe) {
	}

	@Override
	public void recusouAumentoAposta(Jogador j) {
	}

	@Override
	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
	}

}
