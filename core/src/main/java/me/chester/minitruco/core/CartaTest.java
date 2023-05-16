package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartaTest {

    @Test
    @DisplayName("Representa adequadamente como String")
    void testToString() {
        Carta c = new Carta('K', Carta.NAIPE_ESPADAS);
        assertEquals("Ke", c.toString());
    }
}
