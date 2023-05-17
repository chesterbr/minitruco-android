package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Uma <code>Estratégia</code> define como o bot joga na sua vez (qual carta ou
 * se vai pedir aumento) e como ele responde a pedidos de truco e mão de 10/11.
 * <p>
 * Eses eventos chamam os métods definidos aqui, que recebem
 * <code>SituacaoJogo</code> - uma "fotografia" do jogo no momento em que a
 * ação foi solicitada.
 * <p></p>
 * Se for desejado guardar estado, o tempo de vida de
 * uma estratégia é o mesmo de <code>Jogo</code>, ou seja, o estado (não-
 * <code>static</code>) persistirá ao longo de uma partida, mas não entre
 * partidas.
 * TODO: rever o assertion acima
 * <p>
 * TODO: Instruções para fazer uma nova estratégia aparecer
 * <p>
 * Se você criar uma nova estratégia, pode contribui-la para o jogo (desde que
 * concorde em licenciá-la sob os termos acima, baseados na licença "new BSD").
 * Você será creditado e manterá seus direitos autorais. Basta fazer um fork e
 * pull request no github ou entrar em contato com o Chester no cd@pobox.com.
 */
public interface Estrategia {

    /**
     * Executa uma jogada.
     * <p>
     * Observe que, ao pedir aumento, o sistema irá interagir com a outra dupla.
     * Se a partida seguir, o método será chamado novamente para efetivar a real
     * jogada.
     * <p>
     * A estratégia é responsável por checar se o valor da próxima aposta é
     * diferente de 0 e só pedir aumento nesta situação.
     * <p>
     *
     * @param s
     *            Situação do jogo no momento
     * @return posição da carta na mão a jogar (em letrasCartasJogador), ou -1
     *         para pedir truco
     */
    int joga(SituacaoJogo s);

    /**
     * Decide se aceita um pedido de aumento.
     * <p>
     * O valor do aumento pode ser determinado verificando o valor atual da
     * partida (que ainda não foi aumentado)
     *
     * @param s
     *            Situação do jogo no momento
     * @return true para aceitar, false para desistir
     */
    boolean aceitaTruco(SituacaoJogo s);

    /**
     * Decide se aceita iniciar uma mão de 10/11
     *
     * @param cartasParceiro
     *            cartas que o parceiro possui
     * @return true para iniciar valendo 3 pontos, false para desistir e perder
     *         1 ponto
     */
    boolean aceitaMaoDeX(Carta[] cartasParceiro, SituacaoJogo s);

}
