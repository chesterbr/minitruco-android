package me.chester.minitruco.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ComandoNTest {

    private static JogadorConectado jogadorConectado;

    @BeforeAll
    static void setUp() {
        jogadorConectado = mock(JogadorConectado.class);
    }

    /**
     * Verifica se a resposta do servidor é a esperada.
     *
     * @param linha    linha de comando enviada pelo cliente
     * @param resposta resposta esperada do servidor. Se for "<nome default>",
     *                 verifica se o servidor atribiu um nome default
     *                 (sem_nome_NNN, onde NNN é um número de 1 a 999)
     */
    void verificaResposta(String linha, String resposta) {
        Mockito.reset(jogadorConectado);
        Comando.interpreta(linha, jogadorConectado);
        if (resposta.equals("<nome default>")) {
            String regex = "^N sem_nome_\\d{1,3}$";
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

    @Test
    void testIgnoraTentativaDeTrocarONomeDentroDaSala() {
        JogadorConectado j = mock(JogadorConectado.class);
        when(j.getSala()).thenReturn(mock(Sala.class));
        Comando.interpreta("N joselito", j);
        verify(j, never()).setNome(any());
        verify(j, never()).println("N joselito");
    }
}
