package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class BaralhoTest {

    @Test
    void testBaralhoAleatorio() {
        Baralho b1 = new Baralho(false);
        List cartas1 = new ArrayList();
        for (int i = 0; i < 40; i++) {
            cartas1.add(b1.sorteiaCarta());
        }

        Baralho b2 = new Baralho(false);
        List cartas2 = new ArrayList();
        for (int i = 0; i < 40; i++) {
            cartas2.add(b2.sorteiaCarta());
        }
        assertNotEquals(cartas1, cartas2);
    }


    @Test
    void testBaralhoNaoAleatorio() {
        Baralho b1 = new Baralho(false, 123);
        List cartas1 = new ArrayList();
        for (int i = 0; i < 40; i++) {
            cartas1.add(b1.sorteiaCarta());
        }

        Baralho b2 = new Baralho(false, 123);
        List cartas2 = new ArrayList();
        for (int i = 0; i < 40; i++) {
            cartas2.add(b2.sorteiaCarta());
        }
        assertEquals(cartas1, cartas2);
    }

}
