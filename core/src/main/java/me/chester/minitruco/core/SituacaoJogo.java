package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Fotografia da situação atual do jogo.
 * <p>
 * Foi isolada da classe Jogo para poder passar às <code>Estrategia</code>s a
 * situação do jogo de forma a facilitar sua implementação e, ao mesmo tempo,
 * impedir que elas trapaceiem (não dando acesso ao <code>Jogo</code>.
 */
public class SituacaoJogo {

    @Override
    public String toString() {
        return "pos:" + posJogador + ",pontos:" + pontosEquipe[0] + ","
                + pontosEquipe[1] + ",rodada:" + numRodadaAtual + ",results:"
                + resultadoRodada[0] + "," + resultadoRodada[1] + ","
                + resultadoRodada[2] + ",valMao:" + valorMao;
    }

    /**
     * Posição do jogador. 1 e 3 são parceiros entre si, assim como 2 e 4, e
     * jogam na ordem numérica.
     */
    public int posJogador;

    /**
     * Rodada que estamos jogando (de 1 a 3)
     */
    public int numRodadaAtual;

    /**
     * Resultados de cada rodada (1 para vitória da equipe 1/3, 2 para vitória
     * da equipe 2/4 e 3 para empate)
     */
    public final int[] resultadoRodada = new int[3];

    /**
     * Valor atual da mão (1, 3, 6, 9 ou 12)
     */
    public int valorMao;

    /**
     * Valor da mão caso o jogador peça aumento de aposta (se for 0, significa
     * que não pode ser pedido aumento)
     */
    public int valorProximaAposta;

    /**
     * Jogador que está pedindo aumento de aposta (pedindo truco, 6, 9 ou 12).
     * Se for null, ninguém está pedindo
     */
    public int posJogadorPedindoAumento;

    /**
     * Posição (1 a 4) do do jogador que abriu a rodada
     */
    public int posJogadorQueAbriuRodada;

    /**
     * Letra da manilha (quando aplicável).
     * <p>
     * Esta propriedade deve ser usada APENAS para chamar o método
     * Jogo.getValorTruco(), pois, no caso de jogo com manilha velha, seu valor
     * não é o de uma carta
     */
    public char manilha;

    /**
     * Valor que a proprieade manilha assume quando estamos jogando com manilha
     * velha (não-fixa)
     */
    public static final char MANILHA_INDETERMINADA = 'X';

    /**
     * Pontos de cada equipe na partida
     */
    public final int[] pontosEquipe = new int[2];

    /**
     * Para cada rodada (0-2) dá as cartas jogadas pelas 4 posicões (0-3)
     */
    public final Carta[][] cartasJogadas = new Carta[3][4];

    /**
     * Cartas que ainda estão na mão do jogador
     */
    public Carta[] cartasJogador;

    /**
     * Determina se o baralho inclui as cartas 4, 5, 6 e 7 (true) ou não
     * (false).
     * <p>
     */
    public boolean baralhoSujo;

    /**
     * Informa se vale jogar carta fechada
     */
    public boolean podeFechada;

}
