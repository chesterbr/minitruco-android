package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PartidaLocalTest {

    @Test
    void testBaralhoAleatorioVersusNaoAleatorio() {
        PartidaLocal p1 = new PartidaLocal(false, false, "P");
        p1.setSeedBaralho(111);
        for (int i = 0; i < 4; i++) {
            p1.adiciona(new JogadorBot());
        }
        p1.finalizada = true;
        p1.run();
        List cartas1 = new ArrayList();
        for (int indice_jogador = 0; indice_jogador < 4; indice_jogador++) {
            for (int indice_carta = 0; indice_carta < 3; indice_carta++) {
                cartas1.add(p1.jogadores[indice_jogador].getCartas()[indice_carta]);
            }
        }

        PartidaLocal p2 = new PartidaLocal(false, false, "P");
        p2.setSeedBaralho(111);
        for (int i = 0; i < 4; i++) {
            p2.adiciona(new JogadorBot());
        }
        p2.finalizada = true;
        p2.run();
        List cartas2 = new ArrayList();
        for (int indice_jogador = 0; indice_jogador < 4; indice_jogador++) {
            for (int indice_carta = 0; indice_carta < 3; indice_carta++) {
                cartas2.add(p2.jogadores[indice_jogador].getCartas()[indice_carta]);
            }
        }
        assertEquals(cartas1, cartas2);

        PartidaLocal p3 = new PartidaLocal(false, false, "P");
        for (int i = 0; i < 4; i++) {
            p3.adiciona(new JogadorBot());
        }
        p3.finalizada = true;
        p3.run();
        List cartas3 = new ArrayList();
        for (int indice_jogador = 0; indice_jogador < 4; indice_jogador++) {
            for (int indice_carta = 0; indice_carta < 3; indice_carta++) {
                cartas3.add(p3.jogadores[indice_jogador].getCartas()[indice_carta]);
            }
        }
        assertNotEquals(cartas1, cartas3);
    }

}
