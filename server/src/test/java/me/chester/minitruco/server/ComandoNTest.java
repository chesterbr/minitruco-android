package me.chester.minitruco.server;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
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
        if (resposta == "<nome default>") {
            String regex = "^N sem_nome_\\d{1,4}$";
            verify(jogadorConectado).println(matches(regex));
        } else {
            verify(jogadorConectado).println(eq(resposta));
        }
    }

    @Test
    void testNomesInvalidosViramNomeDefault() {
        verificaResposta("N", "<nome default>");
        verificaResposta("N ", "<nome default>");
        verificaResposta("N !@#$", "<nome default>");
    }

    @Test
    void testSanitizaNome() {
        verificaResposta("N joão do pulo 23!", "N joão_do_pulo_23");
    }

    @Test
    void testAtribuiNome() {
        Comando.interpreta("N joselito", jogadorConectado);
        verify(jogadorConectado).setNome("joselito");
    }
}
