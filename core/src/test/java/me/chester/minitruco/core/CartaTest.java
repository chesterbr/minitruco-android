package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    @DisplayName("Atribui valor correto quando em jogo com manilha nova")
    void testGetValorTrucoManilhaNova() {
        char manilha = '6';
        assertEquals(1, new Carta("4e").getValorTruco(manilha));
        assertEquals(2, new Carta("5e").getValorTruco(manilha));
        assertNotEquals(3, new Carta("6e").getValorTruco(manilha));
        assertEquals(4, new Carta("7c").getValorTruco(manilha));
        assertEquals(5, new Carta("Qp").getValorTruco(manilha));
        assertEquals(6, new Carta("Jo").getValorTruco(manilha));
        assertEquals(7, new Carta("Ke").getValorTruco(manilha));
        assertEquals(8, new Carta("Ac").getValorTruco(manilha));
        assertEquals(9, new Carta("2e").getValorTruco(manilha));
        assertEquals(10, new Carta("3e").getValorTruco(manilha));
        assertEquals(11, new Carta("6o").getValorTruco(manilha));
        assertEquals(12, new Carta("6e").getValorTruco(manilha));
        assertEquals(13, new Carta("6c").getValorTruco(manilha));
        assertEquals(14, new Carta("6p").getValorTruco(manilha));

        manilha = 'J';
        assertEquals(1, new Carta("4p").getValorTruco(manilha));
        assertEquals(2, new Carta("5c").getValorTruco(manilha));
        assertEquals(3, new Carta("6e").getValorTruco(manilha));
        assertEquals(4, new Carta("7o").getValorTruco(manilha));
        assertEquals(5, new Carta("Qe").getValorTruco(manilha));
        assertNotEquals(6, new Carta("Jo").getValorTruco(manilha));
        assertEquals(7, new Carta("Ko").getValorTruco(manilha));
        assertEquals(8, new Carta("Ae").getValorTruco(manilha));
        assertEquals(9, new Carta("2c").getValorTruco(manilha));
        assertEquals(10, new Carta("3e").getValorTruco(manilha));
        assertEquals(11, new Carta("Jo").getValorTruco(manilha));
        assertEquals(12, new Carta("Je").getValorTruco(manilha));
        assertEquals(13, new Carta("Jc").getValorTruco(manilha));
        assertEquals(14, new Carta("Jp").getValorTruco(manilha));
    }

    @Test
    @DisplayName("Atribui valor correto quando em jogo com manilha velha")
    void testGetValorTrucoManilhaVelha() {
        char manilha = SituacaoJogo.MANILHA_VELHA;
        assertEquals(1, new Carta("4e").getValorTruco(manilha));
        assertEquals(2, new Carta("5c").getValorTruco(manilha));
        assertEquals(3, new Carta("6p").getValorTruco(manilha));
        assertEquals(4, new Carta("7e").getValorTruco(manilha));
        assertEquals(5, new Carta("Qo").getValorTruco(manilha));
        assertEquals(6, new Carta("Je").getValorTruco(manilha));
        assertEquals(7, new Carta("Ke").getValorTruco(manilha));
        assertEquals(8, new Carta("Ap").getValorTruco(manilha));
        assertEquals(9, new Carta("2e").getValorTruco(manilha));
        assertEquals(10, new Carta("3e").getValorTruco(manilha));
        assertEquals(11, new Carta("7o").getValorTruco(manilha));
        assertEquals(12, new Carta("Ae").getValorTruco(manilha));
        assertEquals(13, new Carta("7c").getValorTruco(manilha));
        assertEquals(14, new Carta("4p").getValorTruco(manilha));
    }

    @Test
    @DisplayName("Cartas inicializam abertas")
    void testFechada() {
        assertFalse(new Carta("Ae").isFechada());
    }

    @Test
    @DisplayName("Cartas valem 0 quando fechadas")
    void testValorCartaFechada() {
        Carta c = new Carta("Ae");
        c.setFechada(true);
        assertEquals(0, c.getValorTruco('A'));
        c.setFechada(false);
        assertNotEquals(0, c.getValorTruco('A'));
    }
}
