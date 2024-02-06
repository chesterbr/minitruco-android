package me.chester.minitruco.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.util.Random;

import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.PartidaLocal;

class ComandoATest {

    private static JogadorConectado j1, j2, j3, j4;

    @BeforeEach
    void setUp() {
        j1 = spy(new JogadorConectado(null));
        j2 = spy(new JogadorConectado(mock(Socket.class)));
        j3 = spy(new JogadorConectado(mock(Socket.class)));
        j4 = spy(new JogadorConectado(null));
        doNothing().when(j1).println(any());
        doNothing().when(j2).println(any());
        doNothing().when(j3).println(any());
        doNothing().when(j4).println(any());
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
    void testAbortaSalaPublicaComPartidaTrocaUsuarioPorBot() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.iniciaPartida(j1);
        assertNotNull(s.getPartida());

        Comando.interpreta("A", j2);
        assertNotNull(s.getPartida());
        assertEquals(JogadorBot.class, s.getPartida().getJogador(2).getClass());
    }

    @Test
    void testAbortaSalaPublicaComPartidaDesconectaUsuarioTrocado() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.iniciaPartida(j1);

        Comando.interpreta("A", j2);
        assertNull(j2.getSala());
        verify(j2).desconecta();
    }

    @Test
    void testAbortaEmSalaMasForaDeJogoAtualizaSalaParaOutrosJogadores() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
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

    @Test
    void testAbortaEmSalaPublicaComPartidaEmAndamentoTrocaUsuarioPorBotNaSalaENaPartida() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.iniciaPartida(j1);

        Comando.interpreta("A", j2);
        assertEquals(JogadorBot.class, s.getJogador(2).getClass());
        assertEquals(s.getJogador(2), s.getPartida().getJogador(2));
    }

    @Test
    void testAbortaEmSalaPrivadaComPartidaEmAndamentoNaoTrocaPorBotEEncerraPartida() {
        Sala s = new Sala(false, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.iniciaPartida(j1);

        Comando.interpreta("A", j2);
        assertEquals(JogadorConectado.class, s.getJogador(2).getClass());
        assertNull(s.getPartida());
    }

    @Test
    void testAbortaEmSalaPublicaSemPartidaEmAndamentoRemoveBotsDaSalaMasMantemOutrosJogadores() throws InterruptedException {
        Sala s = new Sala(true, "1");
        s.adiciona(j1);
        s.adiciona(j2);
        s.adiciona(j3);
        s.adiciona(j4);
        s.iniciaPartida(j1);
        Comando.interpreta("A", j3); // Partida em andamento; J3 é trocado por bot e desconectado
        PartidaLocal p = s.getPartida();
        assertNotNull(s.getJogador(3));

        while (!p.finalizada) {
            Jogador jogadorDaVez = p.getJogadorDaVez();
            if (jogadorDaVez instanceof JogadorConectado) {
                p.jogaCarta(jogadorDaVez, jogadorDaVez.getCartas()[new Random().nextInt(3)]);
            }
            Thread.sleep(100);
        }

        Comando.interpreta("A", j2); // Partida finalizada, J2 não sai da sala
        assertEquals(j1, s.getJogador(1));
        assertEquals(j2, s.getJogador(2)); // Jogador que abortou
        assertNull(s.getJogador(3)); // Jogador que foi trocado por bot
        assertEquals(j4, s.getJogador(4));
    }
}
