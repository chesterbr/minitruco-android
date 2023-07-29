package me.chester.minitruco.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.regex.Pattern;

class ComandoBTest {

    JogadorConectado jogador;
    String buildVelho, buildEsperado, buildMaisNovo;

    @BeforeEach
    void setUp() {
        jogador = mock(JogadorConectado.class);
        int versaoMinima = MiniTrucoServer.BUILD_MINIMO_CLIENTE;
        buildVelho = String.valueOf(versaoMinima - 1);
        buildEsperado = String.valueOf(versaoMinima);
        buildMaisNovo = String.valueOf(versaoMinima + 1);
        assertEquals(5, buildVelho.length());
        assertEquals(5, buildEsperado.length());
        assertEquals(5, buildMaisNovo.length());
    }

    @Test
    void testBuildUsadoPeloComandoBTemCincoDigitos() {
        assertEquals(5, String.valueOf(MiniTrucoServer.BUILD_MINIMO_CLIENTE).length());
    }

    @Test
    void testComandoBIgnoraArgumentosInvalidos() {
        Comando.interpreta("B", jogador);
        Comando.interpreta("B ", jogador);
        Comando.interpreta("B foobar", jogador);
        Comando.interpreta("B 1.0.0", jogador);
        verify(jogador, never()).println(any());
    }

    @Test
    void testComandoBNaoFazNadaSeBuildForMaiorOuIgualAoMinimo() {
        Comando.interpreta("B " + buildEsperado, jogador);
        Comando.interpreta("B " + buildMaisNovo, jogador);
        verify(jogador, never()).println(any());
    }

    @Test
    void testComandoBMandaToastEDesconectaSeBuildForAntigo() {
        Comando.interpreta("B " + buildVelho, jogador);
        InOrder inOrder = Mockito.inOrder(jogador);
        inOrder.verify(jogador).println(matches(Pattern.compile(
            "^\\! T .*atualize o jogo", Pattern.CASE_INSENSITIVE)));
        verify(jogador).desconecta();
    }

}
