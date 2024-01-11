package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static java.lang.Thread.sleep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class PartidaLocalTest {


    @Test
    @Timeout(30)
    void testTrocaUsuarioPorBotSubstituiEContinuaJogoNaVezDoUsuario() throws InterruptedException {
        Jogador jogadorTrocadoNaVez = new JogadorDeTeste() {
            @Override
            public void vez(Jogador j, boolean podeFechada) {
                if (j == this) {
                    PartidaLocal p = (PartidaLocal) partida;
                    int posicao = getPosicao();
                    assertNotEquals(JogadorBot.class, p.getJogador(posicao).getClass());
                    p.trocaUsuarioPorBot(this);
                    assertEquals(JogadorBot.class, p.getJogador(posicao).getClass());
                    ((JogadorBot)p.getJogador(posicao)).setFingeQuePensa(false);
                }
            }
        };
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(jogadorTrocadoNaVez);
        partida.adiciona(new JogadorDeTeste());
        (new Thread(partida)).start();
        while (!partida.finalizada) {
            sleep(100);
        }
    }

}
