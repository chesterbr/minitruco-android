package me.chester.minitruco.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ComandoETest {

    private static JogadorConectado j1, j2, j3, j4, jAnon;

    @BeforeEach
    void setUp() {
        Sala.limpaSalas();
        j1 = spy(new JogadorConectado(null));
        j2 = spy(new JogadorConectado(null));
        j3 = spy(new JogadorConectado(null));
        j4 = spy(new JogadorConectado(null));
        jAnon = spy(new JogadorConectado(null));
        j1.setNome("j1");
        j2.setNome("j2");
        j3.setNome("j3");
        j4.setNome("j4");
        doNothing().when(j1).println(any());
        doNothing().when(j2).println(any());
        doNothing().when(j3).println(any());
        doNothing().when(j4).println(any());
        doNothing().when(jAnon).println(any());
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

    @Test
    void testSalaPrivada() {
        ArgumentCaptor<String> notificacaoCaptor = ArgumentCaptor.forClass(String.class);
        Comando.interpreta("E PRI P", j1);
        verify(j1).println(notificacaoCaptor.capture());

        String notificacao = notificacaoCaptor.getValue();
        assertThat(notificacao, matchesPattern("I j1\\|bot\\|bot\\|bot P 1 PRI-[0-9]+"));
        String salaComPrefixo = notificacao.split(" ")[4];

        Comando.interpreta("E " + salaComPrefixo, j2);
        verify(j1).println("I j1|j2|bot|bot P 1 " + salaComPrefixo);
        verify(j2).println("I j1|j2|bot|bot P 2 " + salaComPrefixo);

        verify(j1, times(2)).println(any());
        verify(j2, times(1)).println(any());
    }

    @Test
    void testArgumentosInvalidosSaoIgnoradas() {
        Comando.interpreta("E", j1);
        Comando.interpreta("E ", j1);
        Comando.interpreta("E XYZ", j1);
        Comando.interpreta("E XYZ A", j1);
        Comando.interpreta("E PUB !", j1);
        verify(j1, never()).println(any());
    }

    @Test
    void testSalaInvalidaNotificaErro() {
        Comando.interpreta("E PRI-SALA404", j1);
        verify(j1).println("X SI");
    }

    @Test
    void testSalaInexistenteNotificaErro() {
        Comando.interpreta("E PRI-12345", j1);
        verify(j1).println("X SI");
    }

    @Test
    void testSalaLotadaNotificaErro() {
        ArgumentCaptor<String> notificacaoCaptor = ArgumentCaptor.forClass(String.class);
        Comando.interpreta("E PRI P", j1);
        verify(j1).println(notificacaoCaptor.capture());

        String notificacao = notificacaoCaptor.getValue();
        assertThat(notificacao, matchesPattern("I j1\\|bot\\|bot\\|bot P 1 PRI-[0-9]+"));
        String salaComPrefixo = notificacao.split(" ")[4];

        Comando.interpreta("E " + salaComPrefixo, j2);
        Comando.interpreta("E " + salaComPrefixo, j3);
        Comando.interpreta("E " + salaComPrefixo, j4);
        verify(j4).println("I j1|j2|j3|j4 P 4 " + salaComPrefixo);

        Comando.interpreta("E " + salaComPrefixo, jAnon);
        verify(j1, never()).println("X SI");
        verify(j2, never()).println("X SI");
        verify(j3, never()).println("X SI");
        verify(j4, never()).println("X SI");
        verify(jAnon).println("X SI");
        verify(jAnon, times(1)).println(any());
    }

    @Test
    void testTiraDaSalaAtualSeJaEstiverEmUma() {
        Comando.interpreta("E PUB M", j1);
        verify(j1).println("I j1|bot|bot|bot M 1 PUB");
        Comando.interpreta("E PUB M", j2);
        verify(j1).println("I j1|j2|bot|bot M 1 PUB");
        verify(j2).println("I j1|j2|bot|bot M 2 PUB");
        Comando.interpreta("E PUB M", j3);
        verify(j1).println("I j1|j2|j3|bot M 1 PUB");
        verify(j2).println("I j1|j2|j3|bot M 2 PUB");
        verify(j3).println("I j1|j2|j3|bot M 3 PUB");

        Comando.interpreta("E PUB P", j1);
        verify(j1).println("S");
        verify(j1).println("I j1|bot|bot|bot P 1 PUB");
        verify(j2).println("I j2|j3|bot|bot M 1 PUB");
        verify(j3).println("I j2|j3|bot|bot M 2 PUB");

        verify(j1, times(5)).println(any());
        verify(j2, times(3)).println(any());
        verify(j3, times(2)).println(any());
    }

    @Test
    void testUsaNomeDefaultSeNaoForInformado() {
        ArgumentCaptor<String> notificacaoCaptor = ArgumentCaptor.forClass(String.class);
        Comando.interpreta("E PUB P", jAnon);
        verify(jAnon).println(notificacaoCaptor.capture());
        String notificacao = notificacaoCaptor.getValue();
        assertThat(notificacao, matchesPattern("I sem_nome_.+\\|bot\\|bot\\|bot P 1 PUB"));
    }
}
