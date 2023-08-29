package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static me.chester.minitruco.core.TrucoUtils.POSICAO_PLACEHOLDER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

class TrucoUtilsTest {

    private String[] nomes;

    @BeforeEach
    void setUp() {
        nomes = new String[]{"j1", "j2", "j3", "j4"};
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

    @Test
    void testNomeParaDisplayEmSalaPrivadaOuBluetooth() {
        for (String tipoSala : new String[]{"PRI", "BLT"}) {
            String n = TrucoUtils.montaNotificacaoI(nomes, "M", tipoSala);
            String n1 = n.replace(POSICAO_PLACEHOLDER, "1");
            String n2 = n.replace(POSICAO_PLACEHOLDER, "2");
            String n3 = n.replace(POSICAO_PLACEHOLDER, "3");
            String n4 = n.replace(POSICAO_PLACEHOLDER, "4");

            // Jogador na posição 1 (gerente)
            assertEquals("<b>j1 (você) (gerente)</b>", TrucoUtils.nomeHtmlParaDisplay(n1, 1));
            assertEquals("j2", TrucoUtils.nomeHtmlParaDisplay(n1, 2));
            assertEquals("j3", TrucoUtils.nomeHtmlParaDisplay(n1, 3));
            assertEquals("j4", TrucoUtils.nomeHtmlParaDisplay(n1, 4));

            // Jogador na posição 2
            assertEquals("j2 (você)", TrucoUtils.nomeHtmlParaDisplay(n2, 1));
            assertEquals("j3", TrucoUtils.nomeHtmlParaDisplay(n2, 2));
            assertEquals("j4", TrucoUtils.nomeHtmlParaDisplay(n2, 3));
            assertEquals("<b>j1 (gerente)</b>", TrucoUtils.nomeHtmlParaDisplay(n2, 4));

            // Jogador na posição 3
            assertEquals("j3 (você)", TrucoUtils.nomeHtmlParaDisplay(n3, 1));
            assertEquals("j4", TrucoUtils.nomeHtmlParaDisplay(n3, 2));
            assertEquals("<b>j1 (gerente)</b>", TrucoUtils.nomeHtmlParaDisplay(n3, 3));
            assertEquals("j2", TrucoUtils.nomeHtmlParaDisplay(n3, 4));

            // Jogador na posição 4
            assertEquals("j4 (você)", TrucoUtils.nomeHtmlParaDisplay(n4, 1));
            assertEquals("<b>j1 (gerente)</b>", TrucoUtils.nomeHtmlParaDisplay(n4, 2));
            assertEquals("j2", TrucoUtils.nomeHtmlParaDisplay(n4, 3));
            assertEquals("j3", TrucoUtils.nomeHtmlParaDisplay(n4, 4));
        }
    }

    @Test
    void testNomeParaDisplayEmSalaPublica() {
        String n = TrucoUtils.montaNotificacaoI(nomes, "M", "PUB");
        String n1 = n.replace(POSICAO_PLACEHOLDER, "1");
        String n2 = n.replace(POSICAO_PLACEHOLDER, "2");
        String n3 = n.replace(POSICAO_PLACEHOLDER, "3");
        String n4 = n.replace(POSICAO_PLACEHOLDER, "4");

        // Jogador na posição 1
        assertEquals("j1 (você)", TrucoUtils.nomeHtmlParaDisplay(n1, 1));
        assertEquals("j2", TrucoUtils.nomeHtmlParaDisplay(n1, 2));
        assertEquals("j3", TrucoUtils.nomeHtmlParaDisplay(n1, 3));
        assertEquals("j4", TrucoUtils.nomeHtmlParaDisplay(n1, 4));

        // Jogador na posição 2
        assertEquals("j2 (você)", TrucoUtils.nomeHtmlParaDisplay(n2, 1));
        assertEquals("j3", TrucoUtils.nomeHtmlParaDisplay(n2, 2));
        assertEquals("j4", TrucoUtils.nomeHtmlParaDisplay(n2, 3));
        assertEquals("j1", TrucoUtils.nomeHtmlParaDisplay(n2, 4));

        // Jogador na posição 3
        assertEquals("j3 (você)", TrucoUtils.nomeHtmlParaDisplay(n3, 1));
        assertEquals("j4", TrucoUtils.nomeHtmlParaDisplay(n3, 2));
        assertEquals("j1", TrucoUtils.nomeHtmlParaDisplay(n3, 3));
        assertEquals("j2", TrucoUtils.nomeHtmlParaDisplay(n3, 4));


        assertEquals("j4 (você)", TrucoUtils.nomeHtmlParaDisplay(n4, 1));
        assertEquals("j1", TrucoUtils.nomeHtmlParaDisplay(n4, 2));
        assertEquals("j2", TrucoUtils.nomeHtmlParaDisplay(n4, 3));
        assertEquals("j3", TrucoUtils.nomeHtmlParaDisplay(n4, 4));
    }

    @Test
    void testNomeHtmlParaDispayConverteEspacos() {
        String n = TrucoUtils.montaNotificacaoI(new String[]{
            "John_Lennon",
            "bot",
            "George_Harrison",
            "Ringo_Starr"
        }, "M", "PUB").replace(POSICAO_PLACEHOLDER, "1");;
        assertEquals("John Lennon (você)", TrucoUtils.nomeHtmlParaDisplay(n, 1));
        assertEquals("bot", TrucoUtils.nomeHtmlParaDisplay(n, 2));


    }
}
