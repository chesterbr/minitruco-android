package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Fotografia da situação atual da partida no momento em que um bot vai jogar,
 * responder a um aumento ou decidir se joga uma mão de 10/11.
 * <p>
 * Ela garante que a estratégia não trapaceie, disponibilizando apenas o que
 * o bot vê: cartas na mão, histórico de cartas jogadas, placares da mão e
 * da partida, etc. A única classe do truco que ela enxerga é <code>Carta</code>.
 */
public class SituacaoJogo {

    private int numEquipeVencedora;

    /**
     * Cria uma situação de jogo em andamento. O chamamador deve popular as
     * propriedades de acordo com o que ele sabe sobre o jogo.
     */
    public SituacaoJogo() {
        this.numEquipeVencedora = 0;
    }

    /**
     * Cria uma situação de jogo finalizado.
     *
     * @param numEquipeVencedora Equipe que venceu o jogo (1 ou 2)
     */
    public SituacaoJogo(int numEquipeVencedora) {
        this.numEquipeVencedora = numEquipeVencedora;
    }

    /**
     * Representação desse estado que será usada para treinar a AI.
     * <p>
     * Estados possíveis:<br/>
     *   - posições: 1=inferior, 2=direita, 3=superior, 4=esquerda<br/>
     *   - equipes: 1=posições 1 e 3; 2=posições 2 e 4<br/>
     *   - cartas: podem valer -1 (null), 0 (fechada) ou um valor de 1
     *             a 14, conforme o valor relativo delas (cartas normais de 1
     *             a 10, manilhas de 11 a 14)<br/>
     *   - rodadas: 1 a 3<br/>
     *   - resultado da rodada: a equipe que venceu (1 ou 2), 3 para empate
     *                          ou -1 para rodada não conlcuída<br/>
     *   - booleanos (ex.: podeFechada) são 0 ou 1<br/>
     * TODO posJogadorPedindoAumento (acho que não zera depois do aumento)<br/>
     * TODO tento mineiro (talvez só varie as recompensas, mas é preciso especificar)<br/>
     * TODO baralho limpo (provavelmente só vamos excluir o range 1-4)<br/>
     *
     * @return valores do espaço de observação, separados por espaço.
     *         Se for um estado terminal (fim de jogo),
     *         "EQUIPE 1 VENCEU" ou "EQUIPE 2 VENCEU".
     */
    public String toObservation() {
        if (numEquipeVencedora > 0) {
            return "EQUIPE " + numEquipeVencedora + " VENCEU";
        }
        Carta[] maoJogador = (cartasJogador == null ? new Carta[0] : cartasJogador);
        int[] estado = {
            valorCarta(maoJogador.length > 0 ? maoJogador[0] : null),
            valorCarta(maoJogador.length > 1 ? maoJogador[1] : null),
            valorCarta(maoJogador.length > 2 ? maoJogador[2] : null),
//            posJogadorPedindoAumento,
//            posJogador,
//            (baralhoSujo ? 1 : 0),
//            (podeFechada ? 1 : 0),
//            numRodadaAtual,
//            (numRodadaAtual > 1 ? resultadoRodada[0] : -1),
//            (numRodadaAtual > 2 ? resultadoRodada[1] : -1),
//            valorMao,
//            valorProximaAposta,
//            posJogadorQueAbriuRodada,
            pontosEquipe[0],
            pontosEquipe[1],
            valorCarta(cartasJogadas[0][0]),
            valorCarta(cartasJogadas[0][1]),
            valorCarta(cartasJogadas[0][2]),
            valorCarta(cartasJogadas[0][3]),
            valorCarta(cartasJogadas[1][0]),
            valorCarta(cartasJogadas[1][1]),
            valorCarta(cartasJogadas[1][2]),
            valorCarta(cartasJogadas[1][3]),
            valorCarta(cartasJogadas[2][0]),
            valorCarta(cartasJogadas[2][1]),
            valorCarta(cartasJogadas[2][2]),
            valorCarta(cartasJogadas[2][3])
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < estado.length; i++) {
            sb.append(estado[i]);
            if (i < estado.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * Valores mínimo e máximo que cada valor do espaço de observação pode assumir
     */
    public static int[][] ranges = {
        { -1, 14}, // carta1Jogador
        { -1, 14}, // carta2Jogador
        { -1, 14}, // carta3Jogador
//        {  0,  4}, // posJogadorPedindoAumento
//        {  1,  4}, // posJogador
//        {  0,  1}, // baralhoSujo
//        {  0,  1}, // podeFechada
//        {  1,  3}, // numRodadaAtual
//        { -1,  3}, // resultadoRodada1
//        { -1,  3}, // resultadoRodada2
//        {  1, 12}, // valorMao
//        {  0, 12}, // valorProximaAposta
//        {  1,  4}, // posJogadorQueAbriuRodada
        {  0, 23}, // pontosEquipe1
        {  0, 23}, // pontosEquipe2
        { -1, 14}, // cartaJogadaRodada1Pos1
        { -1, 14}, // cartaJogadaRodada1Pos2
        { -1, 14}, // cartaJogadaRodada1Pos3
        { -1, 14}, // cartaJogadaRodada1Pos4
        { -1, 14}, // cartaJogadaRodada2Pos1
        { -1, 14}, // cartaJogadaRodada2Pos2
        { -1, 14}, // cartaJogadaRodada2Pos3
        { -1, 14}, // cartaJogadaRodada2Pos4
        { -1, 14}, // cartaJogadaRodada3Pos1
        { -1, 14}, // cartaJogadaRodada3Pos2
        { -1, 14}, // cartaJogadaRodada3Pos3
        { -1, 14}, // cartaJogadaRodada3Pos4
    };

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
