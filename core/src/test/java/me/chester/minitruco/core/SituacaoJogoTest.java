package me.chester.minitruco.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SituacaoJogoTest {

    @Test
    void toObservationRetornaValoresMesmoComObjetoVazio() {
        SituacaoJogo sj = new SituacaoJogo();
        String observation = sj.toObservation();
        String[] valores = observation.split(" ");
        for (String v : valores) {
            assertThat(v, matchesPattern("-?\\d+"));
        }
    }

    @Test
    void quantidadeDeRangesBateComOTamanhoDaObservacao() {
        int tamanhoObservacao = (new SituacaoJogo()).toObservation().split(" ").length;
        assertEquals(tamanhoObservacao, SituacaoJogo.ranges.length);
    }

    @Test
    void toObservationRetornaMaoDoJogadorNasPrimeirasPosicoes() {
        SituacaoJogo sj = new SituacaoJogo();
        sj.manilha = 'A';
        sj.cartasJogador = new Carta[] {
            new Carta('A',Carta.NAIPE_COPAS),
            new Carta('2',Carta.NAIPE_ESPADAS)
        };
        String observation = sj.toObservation();
        String[] valores = observation.split(" ");
        assertEquals("13", valores[0]); // Valor da manilha de espadas
        assertEquals("9", valores[1]);  // Valor do 2
        assertEquals("-1", valores[2]); // Não tem 3a carta
    }
}
