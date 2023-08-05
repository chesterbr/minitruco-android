package me.chester.minitruco.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComandoETest {

    private static JogadorConectado j1, j2, j3;

    @BeforeEach
    void setUp() {
        Sala.limpaSalas();
        j1 = spy(new JogadorConectado(null));
        j2 = spy(new JogadorConectado(null));
        j3 = spy(new JogadorConectado(null));
        j1.setNome("j1");
        j2.setNome("j2");
        j3.setNome("j3");
        doNothing().when(j1).println(any());
        doNothing().when(j2).println(any());
        doNothing().when(j3).println(any());
    }

    @Test
    void testProcuraSalaPublicaFazMatchDeRegra() {
        Comando.interpreta("E PUB P", j1);
        verify(j1).println("I j1|bot|bot|bot P 1 PUB");

        Comando.interpreta("E PUB P", j2);
        verify(j1).println("I j1|j2|bot|bot P 1 PUB");
        verify(j2).println("I j1|j2|bot|bot P 2 PUB");

        Comando.interpreta("E PUB M", j3);
        verify(j3).println("I j3|bot|bot|bot M 1 PUB");

        verify(j1, times(2)).println(any());
        verify(j2, times(1)).println(any());
    }

    @Test
    void testCriaSalaPublicaIgnoraSalaExistente() {
        Comando.interpreta("E PUB P", j1);
        Comando.interpreta("E NPU P", j2);
        Comando.interpreta("E NPU P", j3);

        verify(j1).println("I j1|bot|bot|bot P 1 PUB");
        verify(j2).println("I j2|bot|bot|bot P 1 PUB");
        verify(j3).println("I j3|bot|bot|bot P 1 PUB");

        verify(j1, times(1)).println(any());
        verify(j2, times(1)).println(any());
        verify(j3, times(1)).println(any());
    }

    // TODO: X NO faz sentido? Seria melhor dar um nome default
    // TODO: X JE faz sentido? Seria melhor jogador sair da sala atual e entrar na nova
    // TODO: X AI faz sentido? Seria melhor ignorar comandos e argumentos inválidos (e logar?)
}
