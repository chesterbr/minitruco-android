package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartaTest {

    @Test
    @DisplayName("Pode ser instanciada por letra/naipe ou string")
    void testCreate() {
        Carta carta7p = new Carta("7p");
        assertEquals('7', carta7p.getLetra());
        assertEquals(Carta.NAIPE_PAUS, carta7p.getNaipe());

        Carta carta3o = new Carta('3', Carta.NAIPE_OUROS);
        assertEquals('3', carta3o.getLetra());
        assertEquals(Carta.NAIPE_OUROS, carta3o.getNaipe());
    }

    @Test
    @DisplayName("Representa adequadamente como String")
    void testToString() {
        Carta c = new Carta('K', Carta.NAIPE_ESPADAS);
        assertEquals("Ke", c.toString());
    }

    @Test
    @DisplayName("É igual a outra carta se tiverem a mesma letra e naipe")
    void testEquals() {
        Carta cartaKe = new Carta("Ke");
        assertEquals(new Carta("Ke"), cartaKe);
        assertNotEquals(new Carta("Kc"), cartaKe);
        assertNotEquals(new Carta("Ae"), cartaKe);
    }

    // TODO getValorTruco deveria suportar manilha velha
    //      (mas tem que ver as consequencias para estrategias)
}
