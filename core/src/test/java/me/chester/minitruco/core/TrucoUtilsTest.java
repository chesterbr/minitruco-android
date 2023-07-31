package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrucoUtilsTest {

    private String[] nomes;

    @BeforeEach
    void setUp() {
        nomes = new String[] { "j1", "j2", "j3", "j4" };
    }

    @Test
    void testMontaNotificacaoI() {
        assertEquals(
            "I j1|j2|j3|j4 P " + TrucoUtils.POSICAO_PLACEHOLDER,
            TrucoUtils.montaNotificacaoI(nomes, "P")
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
                TrucoUtils.montaNotificacaoI(jogadores, "P"),
                TrucoUtils.montaNotificacaoI(nomes, "P")
        );
    }
}
