package me.chester.minitruco.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Socket;

import me.chester.minitruco.core.Partida;

class ComandoQTest {

    private static JogadorConectado j1, j2, j3, j4;

    @BeforeEach
    void setUp() {
        Sala.limpaSalas();
        j1 = spy(new JogadorConectado(mock(Socket.class)));
        j2 = spy(new JogadorConectado(mock(Socket.class)));
        j3 = spy(new JogadorConectado(mock(Socket.class)));
        j4 = spy(new JogadorConectado(mock(Socket.class)));
        doNothing().when(j1).println(any());
        doNothing().when(j2).println(any());
        doNothing().when(j3).println(any());
        doNothing().when(j4).println(any());
    }

    private Sala criaSalaPublicaCheia() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.adiciona(j3);
        s.adiciona(j4);
        return s;
    }

    @Test
    void testGerenteIniciaSalaPublicaCheiaSemBots() {
        Sala s = criaSalaPublicaCheia();

        Comando.interpreta("Q", j1);

        assertNotNull(s.getPartida());
        for (int i = 1; i <= 4; i++) {
            assertEquals(JogadorConectado.class, s.getJogador(i).getClass());
        }
        assertEquals("", Sala.modosAguardandoJogadores());
    }

    @Test
    void testNaoGerenteNaoIniciaSalaPublicaCheia() {
        Sala s = criaSalaPublicaCheia();

        Comando.interpreta("Q", j2);
        Comando.interpreta("Q", j3);
        Comando.interpreta("Q", j4);

        assertNull(s.getPartida());
    }

    @Test
    void testQDeTodosOsClientesAposInicioNaoReiniciaPartida() {
        // O app manda Q de todos os jogadores ao receber o início da
        // partida (e o app antigo, quando era gerente, mandava o seu
        // alguns segundos após a mesa encher)
        Sala s = criaSalaPublicaCheia();

        Comando.interpreta("Q", j1);
        Partida p = s.getPartida();
        assertNotNull(p);

        Comando.interpreta("Q", j2);
        Comando.interpreta("Q", j3);
        Comando.interpreta("Q", j4);
        Comando.interpreta("Q", j1);

        assertSame(p, s.getPartida());
    }

    @Test
    void testQDoGerenteComMesaCheiaIniciaIndependenteDoMomento() throws InterruptedException {
        // Cliente antigo manda o Q depois de uns segundos, o novo, quando
        // a pessoa clica; para o servidor é a mesma coisa
        Sala s = criaSalaPublicaCheia();

        Thread.sleep(300);
        assertNull(s.getPartida());
        Comando.interpreta("Q", j1);

        assertNotNull(s.getPartida());
    }
}
