package me.chester.minitruco.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.chester.minitruco.core.JogadorBot;

class ComandoATest {

    private static JogadorConectado j1, j2, j3;

    @BeforeEach
    void setUp() {
        j1 = spy(new JogadorConectado(null));
        j2 = spy(new JogadorConectado(null));
        j3 = spy(new JogadorConectado(null));
        doNothing().when(j1).println(any());
        doNothing().when(j2).println(any());
        doNothing().when(j3).println(any());
    }

    @Test
    void testAbortaSalaPrivadaComPartidaEncerraENotificaSemAtualizarSala() {
        Sala s = new Sala(false, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.iniciaPartida(j1);
        assertNotNull(s.getPartida());

        Comando.interpreta("A", j2);
        assertNull(s.getPartida());
        verify(j1).println(argThat((String msg) -> msg.matches("A 2 .+")));
        verify(j2).println(argThat((String msg) -> msg.matches("A 2 .+")));
        verify(j1, never()).println(argThat((String msg) -> msg.startsWith("I ")));
    }

    @Test
    void testAbortaSalaPublicaComPartidaNaoNotificaSaidaNemAtualizarSala() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.iniciaPartida(j1);
        assertNotNull(s.getPartida());

        Comando.interpreta("A", j2);
        verify(j1, never()).println(argThat((String msg) -> msg.startsWith("I ")));
        verify(j1, never()).println(argThat((String msg) -> msg.startsWith("A ")));
    }

    @Test
    void testAbortaSalaPublicaComPartidaInformaTrocaParaOutrosJogadores() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.adiciona(j3);
        j2.setNome("joselito");
        s.iniciaPartida(j1);

        Comando.interpreta("A", j2);
        verify(j1).println(argThat((String msg) -> msg.matches("! T .*joselito.*")));
        verify(j3).println(argThat((String msg) -> msg.matches("! T .*joselito.*")));
    }

    @Test
    void testAbortaSalaPublicaTrocaUsuarioPorBot() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.iniciaPartida(j1);
        assertNotNull(s.getPartida());

        Comando.interpreta("A", j2);
        assertNotNull(s.getPartida());
        // TODO o que eu quero fazer na *sala*?
//        assertEquals(JogadorBot.class, s.getJogador(2).getClass());
        assertEquals(JogadorBot.class, s.getPartida().getJogador(2).getClass());
    }


    @Test
    void testAbortaEmSalaMasForaDeJogoAtualizaSalaParaOutrosJogadores() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        reset();
        Comando.interpreta("A", j2);
        verify(j1, never()).println(argThat((String msg) -> msg.startsWith("A")));
        verify(j2, never()).println(argThat((String msg) -> msg.startsWith("A")));
        verify(j1).println(argThat((String msg) -> msg.startsWith("I ")));
    }

    @Test
    void testAbortaForaDeSalaÉIgonorado() {
        Comando.interpreta("A", j1);
        verify(j1, never()).println(argThat((String msg) -> msg.startsWith("A")));
    }
}
