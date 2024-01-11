package me.chester.minitruco.core;

import java.util.Arrays;

class JogadorQueJoga extends JogadorDeTeste {
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

}
