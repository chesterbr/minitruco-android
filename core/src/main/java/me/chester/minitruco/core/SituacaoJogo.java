package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Fotografia da situação atual da partida no momento em que um bot vai jogar,
 * responder a um aumento ou decidir se joga uma mão de 10/11.
 * <p>
 * Ela garante que a estratégia não trapaceie, disponibilizando apenas o que
 * o bot vê: cartas na mão, histórico de cartas jogadas, placares da mão e
 * da partida, etc. A única classe do truco que ela enxerga é <code>Carta</code>.
 */
public class SituacaoJogo {

    /**
     * A representação string será usada para treinar a AI
     *
     * @return elementos da tupla que contém o estado do jogo, separados por
     *         espaço (para converter facilmente em tupla com .split())
     */
    @Override
    public String toString() {
        return
            posJogador + " " +
            numRodadaAtual + " " +
            (numRodadaAtual > 1 ? resultadoRodada[0] : -1) + " " +
            (numRodadaAtual > 2 ? resultadoRodada[1] : -1) + " " +
            valorMao + " " +
            valorProximaAposta + " " +
            posJogadorPedindoAumento + " " +
            posJogadorQueAbriuRodada + " " +
            pontosEquipe[0] + " " +
            pontosEquipe[1] + " " +
            valorCarta(cartasJogadas[0][0]) + " " +
            valorCarta(cartasJogadas[0][1]) + " " +
            valorCarta(cartasJogadas[0][2]) + " " +
            valorCarta(cartasJogadas[0][3]) + " " +
            valorCarta(cartasJogadas[1][0]) + " " +
            valorCarta(cartasJogadas[1][1]) + " " +
            valorCarta(cartasJogadas[1][2]) + " " +
            valorCarta(cartasJogadas[1][3]) + " " +
            valorCarta(cartasJogadas[2][0]) + " " +
            valorCarta(cartasJogadas[2][1]) + " " +
            valorCarta(cartasJogadas[2][2]) + " " +
            valorCarta(cartasJogadas[2][3]) + " " +
            valorCarta(cartasJogador.length > 0 ? cartasJogador[0] : null) + " " +
            valorCarta(cartasJogador.length > 1 ? cartasJogador[1] : null) + " " +
            valorCarta(cartasJogador.length > 2 ? cartasJogador[2] : null) + " " +
            (baralhoSujo ? 1 : 0) + " " +
            (podeFechada ? 1 : 0);
    }

    private int valorCarta(Carta c) {
        if (c == null) {
            return -1;
        }
        return Partida.getValorTruco(c, manilha);
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
     * Partida.getValorTruco(), pois, no caso de partida com manilha velha, seu valor
     * não é o de uma carta
     */
    public char manilha;

    /**
     * Valor que a proprieade manilha assume quando estamos jogando com manilha
     * velha (não-fixa)
     */
    public static final char MANILHA_VELHA = 'X';

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
