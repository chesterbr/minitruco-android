package me.chester.minitruco.core;

/**
 * Modo de jogo igual ao Paulista, mas a partida termina em 1 ponto.
 * <p>
 * APENAS PARA TESTES
 */
public class ModoFinalizaEm1 extends ModoPaulista {

    public int pontuacaoParaMaoDeX() {
        return 0;
    }
}
