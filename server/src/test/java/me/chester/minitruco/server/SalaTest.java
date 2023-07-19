package me.chester.minitruco.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SalaTest {

    JogadorConectado j1, j2, j3, j4, j5, j6, j7, j8, j9, j10;

    @BeforeEach
    void setUp() {
        Sala.limpaSalas();

        j1 = new JogadorConectado(null);
        j2 = new JogadorConectado(null);
        j3 = new JogadorConectado(null);
        j4 = new JogadorConectado(null);
        j5 = new JogadorConectado(null);
        j6 = new JogadorConectado(null);
        j7 = new JogadorConectado(null);
        j8 = new JogadorConectado(null);
        j9 = new JogadorConectado(null);
        j10 = new JogadorConectado(null);
    }

    @Test
    void testColocaEmSalaPublicaRetornaSalaDeDestino() {
        Sala s = Sala.colocaEmSalaPublica(j1, "P");
        assertEquals(s, j1.getSala());
    }

    @Test
    void testColocaEmSalaPublicaEncaixaJogadores() {
        // Jogadores que pedem o mesmo modo devem ir para a mesma sala
        Sala s1 = Sala.colocaEmSalaPublica(j1, "P");
        Sala.colocaEmSalaPublica(j2, "P");
        Sala.colocaEmSalaPublica(j3, "P");
        Sala.colocaEmSalaPublica(j4, "P");
        assertEquals(s1, j1.getSala());
        assertEquals(s1, j2.getSala());
        assertEquals(s1, j3.getSala());
        assertEquals(s1, j4.getSala());

        // Se não houver mais sala disponível, cria-se uma nova
        Sala s2 = Sala.colocaEmSalaPublica(j5, "P");
        Sala.colocaEmSalaPublica(j6, "P");
        assertNotEquals(s1, s2);
        assertEquals(s2, j5.getSala());
        assertEquals(s2, j6.getSala());

        // Se abrir uma vaga, a sala original é reaproveitada, ou seja
        // uma nova sala só é criada quando esgotarem as vagas em todas
        // as salas existentes daquele modo
        s1.remove(j4);
        // Vão ser distribuídos entre s1 e s2
        Sala.colocaEmSalaPublica(j7, "P");
        Sala.colocaEmSalaPublica(j8, "P");
        Sala.colocaEmSalaPublica(j9, "P");
        // Vai numa nova sala
        Sala.colocaEmSalaPublica(j10, "P");

        assertTrue(j7.getSala() == s1 || j7.getSala() == s2);
        assertTrue(j8.getSala() == s1 || j8.getSala() == s2);
        assertTrue(j9.getSala() == s1 || j9.getSala() == s2);
        assertFalse(j10.getSala() == s1 || j10.getSala() == s2);
    }

    @Test
    void testColocaEmSalaPublicaSeparaPorModo() {
        Sala.colocaEmSalaPublica(j1, "P");
        Sala.colocaEmSalaPublica(j2, "M");
        Sala.colocaEmSalaPublica(j3, "L");
        Sala.colocaEmSalaPublica(j4, "P");

        assertEquals(j1.getSala(), j4.getSala());
        assertNotEquals(j1.getSala(), j2.getSala());
        assertNotEquals(j1.getSala(), j3.getSala());
    }
}
