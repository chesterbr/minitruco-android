package me.chester.minitruco.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;

import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.Partida;

class SalaTest {

    JogadorConectado j1, j2, j3, j4, j5, j6, j7, j8, j9, j10;
    JogadorConectado jj1, jj2, jj3, jj4;

    private void assertPosicoes(Sala s,
                                Jogador p1, Jogador p2, Jogador p3, Jogador p4) {
        Jogador[] esperados = new Jogador[] {p1, p2, p3, p4};
        Jogador[] reais = new Jogador[] {
            s.getJogador(1),
            s.getJogador(2),
            s.getJogador(3),
            s.getJogador(4)
        };
        assertArrayEquals(esperados, reais);
    }

    private Sala criaSalaCheiaComJj1Gerente() {
        Sala sala = new Sala(true, "P");
        sala.adiciona(jj1);
        sala.adiciona(jj2);
        sala.adiciona(jj3);
        sala.adiciona(jj4);
        assertPosicoes(sala, jj1, jj2, jj3, jj4);
        assertEquals(jj1, sala.getGerente());
        return sala;
    }

    @BeforeEach
    void setUp() {
        Sala.limpaSalas();

        j1 = spy(new JogadorConectado(mock(Socket.class)));
        j2 = spy(new JogadorConectado(mock(Socket.class)));
        j3 = spy(new JogadorConectado(mock(Socket.class)));
        j4 = spy(new JogadorConectado(mock(Socket.class)));
        j5 = new JogadorConectado(mock(Socket.class));
        j6 = new JogadorConectado(mock(Socket.class));
        j7 = new JogadorConectado(mock(Socket.class));
        j8 = new JogadorConectado(mock(Socket.class));
        j9 = new JogadorConectado(mock(Socket.class));
        j10 = new JogadorConectado(mock(Socket.class));
        j1.setNome("j1");
        j2.setNome("j2");
        j3.setNome("j3");
        j4.setNome("j4");

        jj1 = spy(new JogadorConectado(null));
        jj2 = spy(new JogadorConectado(null));
        jj3 = spy(new JogadorConectado(null));
        jj4 = spy(new JogadorConectado(null));
        jj1.setNome("jj1");
        jj2.setNome("jj2");
        jj3.setNome("jj3");
        jj4.setNome("jj4");

        // Mocks para jogadores que podem estar em partida iniciada/encerada
        doNothing().when(j1).println(any());
        doNothing().when(j2).println(any());
        doNothing().when(j3).println(any());
        doNothing().when(j4).println(any());
        doNothing().when(jj1).println(any());
        doNothing().when(jj2).println(any());
        doNothing().when(jj3).println(any());
        doNothing().when(jj4).println(any());
    }

    @Test
    void testGerenteÉSempreOUsuarioNaPosicao1() {
        Sala s = new Sala(true, "P");
        assertNull(s.getGerente());
        s.adiciona(j1);
        assertEquals(j1, s.getGerente());
        s.adiciona(j2);
        assertEquals(j1, s.getGerente());
        s.adiciona(j3);
        assertEquals(j1, s.getGerente());
        s.adiciona(j4);
        assertEquals(j1, s.getGerente());
        s.remove(j1);
        assertEquals(j2, s.getGerente());
        s.remove(j3);
        assertEquals(j2, s.getGerente());
        s.adiciona(j3);
        assertEquals(j2, s.getGerente());
        s.remove(j2);
        assertEquals(j3, s.getGerente());
        s.remove(j3);
        assertEquals(j4, s.getGerente());
        s.remove(j4);
        assertNull(s.getGerente());
    }

    @Test
    void testRemoveMantemPosicao1PreenchidaSemAlterarPosicoesRelativas() {
        Sala s = criaSalaCheiaComJj1Gerente();
        assertPosicoes(s, jj1, jj2, jj3, jj4);
        s.remove(jj3);
        assertPosicoes(s, jj1, jj2, null, jj4);
        s.remove(jj1);
        assertPosicoes(s, jj2, null, jj4, null);
        s.adiciona(jj1);
        s.adiciona(jj3);
        assertPosicoes(s, jj2, jj1, jj4, jj3);
        s.remove(jj2);
        assertPosicoes(s, jj1, jj4, jj3, null);
    }

    @Test
    void testRemoveUltimoJogador() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.remove(jj1);
        s.remove(jj2);
        s.remove(jj3);
        s.remove(jj4);
        assertPosicoes(s, null, null, null, null);
    }

    @Test
    void testColocaEmSalaPublicaRetornaSalaDeDestino() {
        Sala s = Sala.colocaEmSalaPublica(j1, "P");
        assertEquals(s, j1.getSala());
    }

    @Test
    void testColocaEmSalaPublicaEncaixaJogadores() {
        // Jogadores que pedem o mesmo modo devem ir para a mesma sala
        Sala s1 = Sala.colocaEmSalaPublica(j1, "P");
        Sala.colocaEmSalaPublica(j2, "P");
        Sala.colocaEmSalaPublica(j3, "P");
        Sala.colocaEmSalaPublica(j4, "P");
        assertEquals(s1, j1.getSala());
        assertEquals(s1, j2.getSala());
        assertEquals(s1, j3.getSala());
        assertEquals(s1, j4.getSala());

        // Se não houver mais sala disponível, cria-se uma nova
        Sala s2 = Sala.colocaEmSalaPublica(j5, "P");
        Sala.colocaEmSalaPublica(j6, "P");
        assertNotEquals(s1, s2);
        assertEquals(s2, j5.getSala());
        assertEquals(s2, j6.getSala());

        // Se abrir uma vaga, a sala original é reaproveitada, ou seja
        // uma nova sala só é criada quando esgotarem as vagas em todas
        // as salas existentes daquele modo
        s1.remove(j4);
        // Vão ser distribuídos entre s1 e s2
        Sala.colocaEmSalaPublica(j7, "P");
        Sala.colocaEmSalaPublica(j8, "P");
        Sala.colocaEmSalaPublica(j9, "P");
        // Vai numa nova sala
        Sala.colocaEmSalaPublica(j10, "P");

        assertTrue(j7.getSala() == s1 || j7.getSala() == s2);
        assertTrue(j8.getSala() == s1 || j8.getSala() == s2);
        assertTrue(j9.getSala() == s1 || j9.getSala() == s2);
        assertFalse(j10.getSala() == s1 || j10.getSala() == s2);
    }

    @Test
    void testColocaEmSalaPublicaSeparaPorModo() {
        Sala.colocaEmSalaPublica(j1, "P");
        Sala.colocaEmSalaPublica(j2, "M");
        Sala.colocaEmSalaPublica(j3, "L");
        Sala.colocaEmSalaPublica(j4, "P");

        assertEquals(j1.getSala(), j4.getSala());
        assertNotEquals(j1.getSala(), j2.getSala());
        assertNotEquals(j1.getSala(), j3.getSala());
    }

    @Test
    void testNaoColocaJogadorEmSalaComJogoEmAndamento() {
        Sala s1 = Sala.colocaEmSalaPublica(j1, "P");
        s1.iniciaPartida(j1);
        Sala s2 = Sala.colocaEmSalaPublica(j2, "P");
        assertNotEquals(s1, s2);
    }

    @Test
    void testMandaInfoParaTodos() {
        j1.setNome("john");
        j2.setNome("paul");
        j3.setNome("george");
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.adiciona(j3);
        s.mandaInfoParaTodos();
        verify(j1).println("I john|paul|george|bot P 1 PUB");
        verify(j2).println("I john|paul|george|bot P 2 PUB");
        verify(j3).println("I john|paul|george|bot P 3 PUB");
    }

    @Test
    void testSalaInicializaSemPartida() {
        Sala s = new Sala(true, "P");
        assertNull(s.getPartida());
    }

    @Test
    void testIniciaPartida() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);

        s.iniciaPartida(j1);
    }

   @Test
   void testSalaNaoIniciaPartidaSozinha() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        s.adiciona(j3);
        s.adiciona(j4);
        assertNull(s.getPartida());
    }

    @Test
    void testSalaNaoIniciaPartidaSeJogadorNaoEstiverNela() {
        Sala s = new Sala(true, "P");
        s.iniciaPartida(j1);
        assertNull(s.getPartida());
    }

    @Test
    void testSalaNaoIniciaPartidaSeJogadorNaoForGerente() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);

        assertEquals(s.getGerente(), j1);
        s.iniciaPartida(j2);
        assertNull(s.getPartida());
    }

    @Test
    void testSalaNaoIniciaPartidaSeServidorEstiverSendoDesligado() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);
        try {
            JogadorConectado.servidorSendoDesligado = true;
            s.iniciaPartida(j1);
        } finally {
            // Evita side effect em outros testes
            JogadorConectado.servidorSendoDesligado = false;
        }
        assertNull(s.getPartida());
    }

    @Test
    void testSalaNaoIniciaPartidaSeJaHouverUmaEmAndamento() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);

        s.iniciaPartida(j1);
        Partida p = s.getPartida();
        assertEquals(p, s.getPartida());

        s.iniciaPartida(j1);
        assertEquals(p, s.getPartida());
    }

    @Test
    void testSalaPodeIniciarNovaPartidaQuandoAPrimeiraFinaliza() {
        Sala s = new Sala(true, "P");
        s.adiciona(j1);
        s.adiciona(j2);

        s.iniciaPartida(j1);
        Partida p = s.getPartida();

        p.abandona(1);
        assertNull(s.getPartida());
        s.iniciaPartida(j1);
        assertNotNull(s.getPartida());
        assertNotEquals(p, s.getPartida());
    }

    @Test
    void testTrocaParceiroSimplesRetornaTrueETroca() {
        Sala s = criaSalaCheiaComJj1Gerente();
        assertPosicoes(s, jj1, jj2, jj3, jj4);
        assertTrue(s.trocaParceiro(jj1));
        assertPosicoes(s, jj1, jj3, jj4, jj2);
    }

    @Test
    void testTrocaParceiroRetornaFalseEIgnoraSeNaoForGerente() {
        Sala s = criaSalaCheiaComJj1Gerente();
        assertPosicoes(s, jj1, jj2, jj3, jj4);
        assertFalse(s.trocaParceiro(jj2));
        assertPosicoes(s, jj1, jj2, jj3, jj4);
    }

    @Test
    void testTrocaParceiroRetornaFalseEIgnoraSeEstiverJogando() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.iniciaPartida(jj1);
        assertPosicoes(s, jj1, jj2, jj3, jj4);
        assertFalse(s.trocaParceiro(jj1));
        assertPosicoes(s, jj1, jj2, jj3, jj4);
    }

    @Test
    void testTrocaParceiroNaoAlteraQuemÉOGerente() {
        Sala s = criaSalaCheiaComJj1Gerente();
        assertEquals(jj1, s.getGerente());
        s.trocaParceiro(jj1);
        assertEquals(jj1, s.getGerente());
    }

    @Test
    void testTrocaParceiroEmSalaComVaga() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.remove(jj3);
        assertPosicoes(s, jj1, jj2, null, jj4);
        s.trocaParceiro(jj1);
        assertPosicoes(s, jj1, null, jj4, jj2);
        s.trocaParceiro(jj1);
        assertPosicoes(s, jj1, jj4, jj2, null);
        s.trocaParceiro(jj1);
        assertPosicoes(s, jj1, jj2, null, jj4);
    }

    @Test
    void testTrocaParceiroEmSalaComDuasVagas() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.remove(jj2);
        s.remove(jj4);
        assertPosicoes(s, jj1, null, jj3, null);
        s.trocaParceiro(jj1);
        assertPosicoes(s, jj1, jj3, null, null);
        s.trocaParceiro(jj1);
        assertPosicoes(s, jj1, null, null, jj3);
        s.trocaParceiro(jj1);
        assertPosicoes(s, jj1, null, jj3, null);
    }

    @Test
    void testInverteAdversariosSimplesRetornaTrueEInverte() {
        Sala s = criaSalaCheiaComJj1Gerente();
        assertPosicoes(s, jj1, jj2, jj3, jj4);
        assertTrue(s.inverteAdversarios(jj1));
        assertPosicoes(s, jj1, jj4, jj3, jj2);
        assertTrue(s.inverteAdversarios(jj1));
        assertPosicoes(s, jj1, jj2, jj3, jj4);
    }

    @Test
    void testInverteAdversariosRetornaFalseEIgnoraSeNaoForGerente() {
        Sala s = criaSalaCheiaComJj1Gerente();
        assertPosicoes(s, jj1, jj2, jj3, jj4);
        assertFalse(s.inverteAdversarios(jj2));
        assertPosicoes(s, jj1, jj2, jj3, jj4);
    }

    @Test
    void testInverteAdversariosRetornaFalseEIgnoraSeEstiverJogando() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.iniciaPartida(jj1);
        assertPosicoes(s, jj1, jj2, jj3, jj4);
        assertFalse(s.inverteAdversarios(jj1));
        assertPosicoes(s, jj1, jj2, jj3, jj4);
    }

    @Test
    void testSalaPrivadaGeraCodigoNumericoDeCincoDigitos() {
        Sala s = new Sala(false, "P");
        assertThat(s.codigo, matchesPattern("[0-9]{5}"));
    }

    String sorted(String s) {
        return Arrays.stream(s.split("")).sorted().collect(Collectors.joining());
    }

    @Test
    void testModosAguardandoJogadores() {
        Sala.colocaEmSalaPublica(j1, "P");
        Sala.colocaEmSalaPublica(j2, "P");
        Sala.colocaEmSalaPublica(j3, "M");
        Sala.colocaEmSalaPublica(j4, "L");
        Sala.colocaEmSalaPublica(j5, "P");
        Sala.colocaEmSalaPublica(j6, "P");
        Sala.colocaEmSalaPublica(j7, "M");
        Sala.colocaEmSalaPublica(j8, "L");
        assertEquals("LM", sorted(Sala.modosAguardandoJogadores()));         // P está cheia; V está vazia

        Sala.colocaEmSalaPublica(j9, "P");
        assertEquals("LMP", sorted(Sala.modosAguardandoJogadores()));        // Abriu uma nova sala P para o j9

        j9.getSala().remove(j9);
        assertEquals("LM", sorted(Sala.modosAguardandoJogadores()));        // A sala fechou automaticamente com 1 jogador

        Sala.colocaEmSalaPublica(j10, "L");
        Sala.colocaEmSalaPublica(jj1, "L");
        assertEquals("M", sorted(Sala.modosAguardandoJogadores()));        // Completou a sala L
    }

    @Test
    void testIsPublica() {
        Sala s = new Sala(true, "P");
        assertTrue(s.isPublica());
        s = new Sala(false, "P");
        assertFalse(s.isPublica());
    }

    @Test
    void testTrocaPorBotsFazTrocaCompleta() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.iniciaPartida(jj1);
        s.trocaPorBot(jj2);
        assertEquals(JogadorBot.class, s.getJogador(2).getClass());
        assertEquals(s.getJogador(2), s.getPartida().getJogador(2));
        assertNull(jj2.getSala());
    }

    @Test
    void testTrocaPorBotsSoFuncionaComPartidaEmAndamento() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.trocaPorBot(jj2); // Ainda não iniciou
        s.iniciaPartida(jj1);
        s.trocaPorBot(jj3); // Em andamento
        s.getPartida().abandona(1);
        s.trocaPorBot(jj4); // Terminada
        assertEquals(jj2, s.getJogador(2));
        assertNotEquals(jj3, s.getJogador(3));
        assertEquals(jj4, s.getJogador(4));
    }

    @Test
    void testRemoveBotsTiraOsBots() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.iniciaPartida(jj1);
        s.trocaPorBot(jj2);
        assertNotNull(s.getJogador(2));
        s.removeBots();
        assertNotNull(s.getJogador(1));
        assertNull(s.getJogador(2));
        assertNotNull(s.getJogador(3));
        assertNotNull(s.getJogador(4));
    }

    @Test
    void testRemoveBotsNaoLiberaSalaEnquantoNinguemSair() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.iniciaPartida(jj1);
        s.trocaPorBot(jj2);
        s.removeBots();

        Sala.colocaEmSalaPublica(j1, "P");
        assertNotEquals(s, j1.getSala());
    }

    @Test
    void testRemoveBotsLiberaSalaQuandoElaEncerraOJogoAtual() {
        Sala s = criaSalaCheiaComJj1Gerente();
        s.iniciaPartida(jj1);
        s.trocaPorBot(jj2);
        s.removeBots();
        s.liberaJogo();

        Sala.colocaEmSalaPublica(j1, "P");
        assertEquals(s, j1.getSala());
    }
}
