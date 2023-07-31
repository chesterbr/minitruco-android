package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

class TrucoUtilsTest {

    private String[] nomes;

    @BeforeEach
    void setUp() {
        nomes = new String[] { "j1", "j2", "j3", "j4" };
    }

    @Test
    void testMontaNotificacaoI() {
        // Sim, esse teste assume o valor do placeholder; isso é proposital
        // para pedir cuidado ao mexer nesse valor
        assertEquals(
            "I j1|j2|j3|j4 P $POSICAO PRI-123",
            TrucoUtils.montaNotificacaoI(nomes, "P", "PRI-123")
        );
        Collections.reverse(Arrays.asList(nomes));
        nomes[2] = null;
        nomes[3] = "";
        assertEquals(
            "I j4|j3|bot|bot M $POSICAO BLT",
            TrucoUtils.montaNotificacaoI(nomes, "M", "BLT")
        );

    }

    @Test
    void testMontaNotificacaoIAceitaJogadoresOuSeusNomesIgualmente() {
        Jogador[] jogadores = new Jogador[4];
        for (int i = 0; i < 4; i++) {
            jogadores[i] = mock(Jogador.class);
            when(jogadores[i].getNome()).thenReturn(nomes[i]);
        }
        assertEquals(
                TrucoUtils.montaNotificacaoI(jogadores, "M", "BLT"),
                TrucoUtils.montaNotificacaoI(nomes, "M", "BLT")
        );
    }
}
