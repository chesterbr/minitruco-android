package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static java.lang.Thread.sleep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class PartidaLocalTest {


    @Test
    @Timeout(20)
    void testTrocaPorBotNaVez() throws InterruptedException {
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        partida.pontosEquipe[0] = 7;
        partida.pontosEquipe[1] = 7;
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste() {
            @Override
            public void vez(Jogador j, boolean podeFechada) {
                if (j == this) {
                    PartidaLocal p = (PartidaLocal) partida;
                    int posicao = getPosicao();
                    p.trocaPorBot(this);
                    ((JogadorBot) p.getJogador(posicao)).setFingeQuePensa(false);
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
    @Timeout(20)
    void testTrocaPorBotForaDaVez() throws InterruptedException {
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        partida.pontosEquipe[0] = 7;
        partida.pontosEquipe[1] = 7;
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste());
        partida.adiciona(new JogadorDeTeste() {
            @Override
            public void vez(Jogador j, boolean podeFechada) {
                if (j != this) {
                    PartidaLocal p = (PartidaLocal) partida;
                    int posicao = getPosicao();
                    p.trocaPorBot(this);
                    ((JogadorBot) p.getJogador(posicao)).setFingeQuePensa(false);
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
        partida.pontosEquipe[0] = 7;
        partida.pontosEquipe[1] = 7;
        for (int i = 1; i <= 4; i++) {
            partida.adiciona(new JogadorDeTeste() {
                @Override
                public void vez(Jogador j, boolean podeFechada) {
                    if (j == this) {
                        PartidaLocal p = (PartidaLocal) partida;
                        int posicao = getPosicao();
                        p.trocaPorBot(this);
                        ((JogadorBot) p.getJogador(posicao)).setFingeQuePensa(false);
                    }
                }
            });
        }
        (new Thread(partida)).start();
        while (!partida.finalizada) {
            sleep(100);
        }
        assertEquals(JogadorBot.class, partida.getJogador(1).getClass());
        assertEquals(JogadorBot.class, partida.getJogador(2).getClass());
        assertEquals(JogadorBot.class, partida.getJogador(3).getClass());
        assertEquals(JogadorBot.class, partida.getJogador(4).getClass());
    }

    @Test
    @Timeout(20)
    void testTrocaPorBotAguardandoMaoDeX() throws InterruptedException {
        class JogadorDeTesteQueRecusaMaoDe11 extends JogadorDeTeste {
            public void informaMaoDeX(Carta[] cartasParceiro) {
                partida.decideMaoDeX(this, false);
            }
        }
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        // Partida vai começar em 11 x 0 para a dupla 1 (jogadores 1 e 3)
        partida.pontosEquipe[0] = 11;
        // Jogador 1 vai recusar a mão de 11
        partida.adiciona(new JogadorDeTesteQueRecusaMaoDe11());
        partida.adiciona(new JogadorDeTesteQueRecusaMaoDe11());
        // Jogador 3 vai ser trocado enquanto estiver pensando na mão de 11
        partida.adiciona(new JogadorDeTeste() {
            @Override
            public void informaMaoDeX(Carta[] cartasParceiro) {
                PartidaLocal p = (PartidaLocal) partida;
                int posicao = getPosicao();
                p.trocaPorBot(this);
                ((JogadorBot) p.getJogador(posicao)).setFingeQuePensa(false);
            }
        });
        partida.adiciona(new JogadorDeTesteQueRecusaMaoDe11());
        (new Thread(partida)).start();
        while (!partida.finalizada) {
            sleep(100);
        }
        assertEquals(JogadorDeTesteQueRecusaMaoDe11.class, partida.getJogador(1).getClass());
        assertEquals(JogadorDeTesteQueRecusaMaoDe11.class, partida.getJogador(2).getClass());
        assertEquals(JogadorBot.class, partida.getJogador(3).getClass());
        assertEquals(JogadorDeTesteQueRecusaMaoDe11.class, partida.getJogador(4).getClass());
    }


    @Test
    @Timeout(20)
    void testTrocaPorBotAguardandoAumento() throws InterruptedException {
        class JogadorDeTesteQueRecusaAumento extends JogadorDeTeste {
            public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {
                if (j.getEquipe() == this.getEquipeAdversaria()) {
                    partida.respondeAumento(this, false);
                }
            }
        }
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        partida.pontosEquipe[0] = 7;
        partida.pontosEquipe[1] = 7;
        partida.adiciona(new JogadorDeTesteQueRecusaAumento());
        // Jogador 2 vai pedir truco enquanto o 3 não for trocado
        // (e o 1 sempre vai recusar, deixando a decisão na mão do 3)
        partida.adiciona(new JogadorDeTeste() {
            @Override
            public void vez(Jogador j, boolean podeFechada) {
                if (j == this && partida.getJogador(3).getClass() != JogadorBot.class) {
                    new Thread(() -> {
                        partida.aumentaAposta(j);
                    }).start();

                } else {
                    super.vez(j, podeFechada);
                }
            }

            @Override
            public void aceitouAumentoAposta(Jogador j, int valor, int rndFrase) {
                if (((PartidaLocal) partida).getJogadorDaVez() == this) {
                    super.vez(this, false);
                }
            }
        });
        // Jogador 3 vai ser trocado enquanto estiver pensando no pedido de truco
        partida.adiciona(new JogadorDeTeste() {
            @Override
            public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {
                if (j.getEquipe() == this.getEquipeAdversaria()) {
                    PartidaLocal p = (PartidaLocal) partida;
                    int posicao = getPosicao();
                    p.trocaPorBot(this);
                    ((JogadorBot) p.getJogador(posicao)).setFingeQuePensa(false);
                }
            }
        });
        partida.adiciona(new JogadorDeTesteQueRecusaAumento());
        (new Thread(partida)).start();
        while (!partida.finalizada) {
            sleep(100);
        }
        assertEquals(JogadorBot.class, partida.getJogador(3).getClass());
    }

    // TODO: testar trocaPorBot() quando a partida estiver aguardando o jogador responder aumento
    //       (provavelmente vai falhar, aí vai ter que ter uma repetição da notificação, igual teve quando era a vez)
}
