package me.chester.minitruco.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ComandoRTest {

    private static Sala sala;
    private static JogadorConectado j1, j2, j3;

    @BeforeEach
    void setUp() {
        sala = new Sala(true, "P");
        j1 = spy(new JogadorConectado(null));
        j2 = spy(new JogadorConectado(null));
        j3 = spy(new JogadorConectado(null));
        j1.setNome("j1");
        j2.setNome("j2");
        j3.setNome("j3");
        sala.adiciona(j1);
        sala.adiciona(j2);
        sala.adiciona(j3);
        Mockito.reset(j1,j2, j3);
        doNothing().when(j1).println(any());
        doNothing().when(j2).println(any());
        doNothing().when(j3).println(any());
    }

    @Test
    void testTrocaParceirosDoGerente() {
        Comando.interpreta("R T", j1);
        verify(j1).println("I j1|j3|bot|j2 P 1 PUB");
        verify(j2).println("I j1|j3|bot|j2 P 4 PUB");
        verify(j3).println("I j1|j3|bot|j2 P 2 PUB");
    }

    @Test
    void testInverteAdversariosDoGerente() {
        Comando.interpreta("R I", j1);
        verify(j1).println("I j1|bot|j3|j2 P 1 PUB");
        verify(j2).println("I j1|bot|j3|j2 P 4 PUB");
        verify(j3).println("I j1|bot|j3|j2 P 3 PUB");
    }

    @Test
    void testIgnoraOutrosUsuarios() {
        Comando.interpreta("R T", j2);
        Comando.interpreta("R I", j2);
        verify(j1, never()).println(any());
        verify(j2, never()).println(any());
        verify(j3, never()).println(any());
    }

    @Test
    void testIgnoraSeNaoEstaEmUmaSala() {
        sala.remove(j1);
        Comando.interpreta("R T", j1);
        Comando.interpreta("R I", j1);
        verify(j1, never()).println(any());
        verify(j2, never()).println(any());
        verify(j3, never()).println(any());
    }

    @Test
    void testIgnoraSeJogoEmAndamento() {
        sala.iniciaPartida(j1);
        Comando.interpreta("R T", j1);
        Comando.interpreta("R I", j1);
        verify(j1, never()).println(startsWith("I "));
        verify(j2, never()).println(startsWith("I "));
        verify(j3, never()).println(startsWith("I "));
    }

    @Test
    void testIgnoraArgumentosInvalidos() {
        Comando.interpreta("R", j1);
        Comando.interpreta("R ", j1);
        Comando.interpreta("R X", j1);
        Comando.interpreta("R R R", j1);
        Comando.interpreta("R WTF", j1);
        verify(j1, never()).println(any());
        verify(j2, never()).println(any());
        verify(j3, never()).println(any());
    }

}
