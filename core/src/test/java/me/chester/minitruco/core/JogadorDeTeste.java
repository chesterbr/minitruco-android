package me.chester.minitruco.core;

import java.util.Arrays;

/**
 * Jogador que joga automaticamente, sem precisar de interação do usuário.
 */
class JogadorDeTeste extends Jogador {
    @Override
    public void vez(Jogador j, boolean podeFechada) {
        if (j == this) {
            new Thread(() -> {
                Carta[] cartasJogadas = Arrays.stream(partida.cartasJogadasPorRodada)
                    .flatMap(Arrays::stream)
                    .toArray(Carta[]::new);
                Carta[] cartasEmMaos = Arrays.stream(getCartas())
                    .filter(e -> !Arrays.asList(cartasJogadas).contains(e))
                    .toArray(Carta[]::new);
                Arrays.stream(partida.cartasJogadasPorRodada).flatMap(Arrays::stream).toArray();
                partida.jogaCarta(j, cartasEmMaos[0]);
            }).start();
        }
    }

    @Override
    public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {
        if (j.getEquipe() == this.getEquipeAdversaria()) {
            partida.respondeAumento(this, true);
        }
    }

    @Override
    public void informaMaoDeX(Carta[] cartasParceiro) {
        partida.decideMaoDeX(this, true);
    }

    @Override
    public void cartaJogada(Jogador j, Carta c) {

    }

    @Override
    public void inicioMao(Jogador jogadorQueAbre) {

    }

    @Override
    public void inicioPartida(int placarEquipe1, int placarEquipe2) {

    }

    @Override
    public void aceitouAumentoAposta(Jogador j, int valor, int rndFrase) {

    }

    @Override
    public void recusouAumentoAposta(Jogador j, int rndFrase) {

    }

    @Override
    public void rodadaFechada(int numRodada, int resultado, Jogador jogadorQueTorna) {

    }

    @Override
    public void maoFechada(int[] pontosEquipe) {

    }

    @Override
    public void jogoFechado(int numEquipeVencedora, int rndFrase) {

    }

    @Override
    public void decidiuMaoDeX(Jogador j, boolean aceita, int rndFrase) {

    }

    @Override
    public void jogoAbortado(int posicao, int rndFrase) {

    }

}
