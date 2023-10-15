package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ThreadFactory;

class JogadorBotTest {

    @Test
    void testNotificaFimDeJogoParaEstrategia() {
        Estrategia e = mock(Estrategia.class);
        JogadorBot bot = new JogadorBot(e);
        bot.jogoFechado(1, 999);
        verify(e).partidaFinalizada(1);
    }

    // TODO limpar esse código
    // TODO testar todas as cartas (só testa a primeira)
    // TODO testar cartas jogadas (talvez acumular múltiplas situaçãoJogo e deixar a partida ir até o fim?)
    // esse teste faz pensar que talvez SituacaoJogo devesse se auto-preencher
    // (mas esse tipo de "jogo que joga sozinho" vai ser útil; creio que
    // a idéia aqui seja encapsular e otimizar esse formato para que testes possam
    // observar diferentes aspectos e resultados)
    @Test
    @Timeout(5)
    @DisplayName("Sempre passa cópias das cartas para as estratégias")
    void testVazamentoCarta() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        final SituacaoJogo[] situacaoJogo = new SituacaoJogo[1];
        Estrategia e = new Estrategia() {
            @Override
            public int joga(SituacaoJogo s) {
                situacaoJogo[0] = s;
                return 0;
            }

            @Override
            public boolean aceitaTruco(SituacaoJogo s) {
                return false;
            }

            @Override
            public boolean aceitaMaoDeX(Carta[] cartasParceiro, SituacaoJogo s) {
                return false;
            }

            @Override
            public void partidaFinalizada(int numEquipeVencedora) {

            }
        };
        Partida j = new PartidaLocal(false, false, "P");
        JogadorBot bot = new JogadorBot(e, null);
        j.adiciona(bot);
        j.adiciona(new JogadorBot());
        j.adiciona(new JogadorBot());
        j.adiciona(new JogadorBot());
        new Thread(j).start();
        while (situacaoJogo[0] == null) {
            Thread.sleep(250);
        }
        assertEquals(bot.getCartas()[0], situacaoJogo[0].cartasJogador[0]);
        assertFalse(bot.getCartas()[0] == situacaoJogo[0].cartasJogador[0]);

    }

    @Test
    void testAceitaThreadFactory() {
        ThreadFactory tf = spy(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        });
        JogadorBot bot = new JogadorBot(tf);
        verify(tf).newThread(bot);
    }
}
