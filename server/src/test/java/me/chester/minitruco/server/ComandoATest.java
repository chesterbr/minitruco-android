package me.chester.minitruco.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComandoATest {

    private static JogadorConectado j1, j2;

    @BeforeEach
    void setUp() {
        j1 = spy(new JogadorConectado(null));
        j2 = spy(new JogadorConectado(null));
        doNothing().when(j1).println(any());
        doNothing().when(j2).println(any());
    }

    @Test
    void testAbortaEncerraPartidaENotifica() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.iniciaPartida(j1);
        assertNotNull(s.getPartida());

        Comando.interpreta("A", j2);
        assertNull(s.getPartida());
        verify(j1).println(argThat((String msg) -> msg.matches("A 2 .+")));
        verify(j2).println(argThat((String msg) -> msg.matches("A 2 .+")));
    }

    @Test
    void testAbortaEmSalaMasForaDeJogoÉIgnorado() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        Comando.interpreta("A", j2);
        verify(j1, never()).println(argThat((String msg) -> msg.startsWith("A")));
        verify(j2, never()).println(argThat((String msg) -> msg.startsWith("A")));
    }

    @Test
    void testAbortaForaDeSalaÉIgonorado() {
        Comando.interpreta("A", j1);
        verify(j1, never()).println(argThat((String msg) -> msg.startsWith("A")));
    }
}
