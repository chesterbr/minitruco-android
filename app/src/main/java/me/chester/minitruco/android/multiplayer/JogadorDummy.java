package me.chester.minitruco.android.multiplayer;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Jogador (no cliente) que participa de um jogo bluetooth (além do
 * JogadorHumano).
 * <p>
 * A classe não faz nada - é só para o JogadorHumano não se sentir sozinho
 * (i.e., ter a quem referenciar nos eventos remotos).
 */
public class JogadorDummy extends Jogador {

    public void aceitouAumentoAposta(Jogador j, int valor, int rndFrase) {

    }

    public void cartaJogada(Jogador j, Carta c) {

    }

    public void decidiuMaoDeX(Jogador j, boolean aceita, int rndFrase) {

    }

    public void informaMaoDeX(Carta[] cartasParceiro) {

    }

    public void inicioMao() {

    }

    public void inicioPartida(int placar1, int placar2) {

    }

    public void jogoAbortado(int posicao, int rndFrase) {

    }

    public void jogoAbortadoPorComando() {

    }

    public void jogoFechado(int numEquipeVencedora, int rndFrase) {

    }

    public void maoFechada(int[] pontosEquipe) {

    }

    public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {

    }

    public void recusouAumentoAposta(Jogador j, int rndFrase) {

    }

    public void rodadaFechada(int numRodada, int resultado,
            Jogador jogadorQueTorna) {

    }

    public void vez(Jogador j, boolean podeFechada) {

    }

    public void setGameLevel(int gameLevel) {
        // n�o precisa tratar
    }

    public void setNickEstrategia() {
        // n�o precisa tratar
    }

    public String getNickEstrategia() {
        // n�o precisa tratar
        return getNome(); // just to avoid unexpected usage of this method
    }

    public void mensagemEstrategia(Jogador j, String s) {
        // n�o precisa tratar
    }

}
