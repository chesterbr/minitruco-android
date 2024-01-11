package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static java.lang.Thread.sleep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class PartidaLocalTest {


    @Test
    @Timeout(30)
    void testTrocaUsuarioPorBotSubstituiEContinuaJogoNaVezDoUsuario() throws InterruptedException {
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste() {
            @Override
            public void vez(Jogador j, boolean podeFechada) {
                if (j == this) {
                    PartidaLocal p = (PartidaLocal) partida;
                    int posicao = getPosicao();
                    p.trocaUsuarioPorBot(this);
                    ((JogadorBot)p.getJogador(posicao)).setFingeQuePensa(false);
                }
            }
        });
        partida.adiciona(new JogadorDeTeste());
        (new Thread(partida)).start();
        while (!partida.finalizada) {
            sleep(100);
        }
        assertEquals(JogadorDeTeste.class, partida.getJogador(1).getClass());
        assertEquals(JogadorDeTeste.class, partida.getJogador(2).getClass());
        assertEquals(JogadorBot.class, partida.getJogador(3).getClass());
        assertEquals(JogadorDeTeste.class, partida.getJogador(4).getClass());
    }

    @Test
    @Timeout(30)
    void testTrocaUsuarioPorBotSubstituiEContinuaJogoForaDaVezDoUsuario() throws InterruptedException {
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste() {
            @Override
            public void vez(Jogador j, boolean podeFechada) {
                if (j != this) {
                    PartidaLocal p = (PartidaLocal) partida;
                    int posicao = getPosicao();
                    p.trocaUsuarioPorBot(this);
                    ((JogadorBot)p.getJogador(posicao)).setFingeQuePensa(false);
                }
            }
        });
        partida.adiciona(new JogadorDeTeste());
        (new Thread(partida)).start();
        while (!partida.finalizada) {
            sleep(100);
        }
        assertEquals(JogadorDeTeste.class, partida.getJogador(1).getClass());
        assertEquals(JogadorDeTeste.class, partida.getJogador(2).getClass());
        assertEquals(JogadorBot.class, partida.getJogador(3).getClass());
        assertEquals(JogadorDeTeste.class, partida.getJogador(4).getClass());
    }


    @Test
    @Timeout(30)
    void testTrocaTodosPorBots() throws InterruptedException {
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste() {
            @Override
            public void vez(Jogador j, boolean podeFechada) {
                PartidaLocal p = (PartidaLocal) partida;
                int posicao = getPosicao();
                p.trocaUsuarioPorBot(this);
                ((JogadorBot)p.getJogador(posicao)).setFingeQuePensa(false);
            }
        });
        partida.adiciona(new JogadorDeTeste());
        (new Thread(partida)).start();
        while (!partida.finalizada) {
            sleep(100);
        }
        assertEquals(JogadorBot.class, partida.getJogador(1).getClass());
        assertEquals(JogadorBot.class, partida.getJogador(2).getClass());
        assertEquals(JogadorBot.class, partida.getJogador(3).getClass());
        assertEquals(JogadorBot.class, partida.getJogador(4).getClass());
    }
}
