package me.chester.minitruco.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ComandoNTest {

    private static JogadorConectado jogadorConectado;

    @BeforeAll
    static void setUp() {
        jogadorConectado = mock(JogadorConectado.class);
    }

    void verificaResposta(String linha, String resposta) {
        Mockito.reset(jogadorConectado);
        Comando.interpreta(linha, jogadorConectado);
        verify(jogadorConectado).println(resposta);
    }

    @Test
    void testNomesInvalidos() {
        verificaResposta("N", "X NI");
        verificaResposta("N ", "X NI");
        verificaResposta("N !@#$", "X NI");
    }
}
