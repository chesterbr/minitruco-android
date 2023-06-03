package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;

class JogadorBotTest {

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
        };
        Partida j = new PartidaLocal(false, false, "P");
        // TODO Isso é gambiarra porque eu tirei o construtor que recebe
        // estratégia :facepalm:
        Field estrategiaDoBot = JogadorBot.class.getDeclaredField("estrategia");
        estrategiaDoBot.setAccessible(true);
        JogadorBot bot = new JogadorBot();
        estrategiaDoBot.set(bot, e);
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
}
