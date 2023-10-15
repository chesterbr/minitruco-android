package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.util.Vector;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jogador controlado pelo celular ou pelo servidor.
 * <p>
 * A execução das jogadas é feita por <code>Estrategia</code>.
 * <p>
 * @see Estrategia
 */
public class JogadorBot extends Jogador implements Runnable {
    private final static Logger LOGGER = Logger.getLogger("JogadorBot");
    public static final String APELIDO_BOT = "bot";

    private boolean fingeQuePensa = true;

    public JogadorBot() {
        this(null, null);
    }

    public JogadorBot(ThreadFactory tf) {
        this(null, tf);
    }

    public JogadorBot(Estrategia e) { this(e, null); }

    public JogadorBot(Estrategia e, ThreadFactory tf) {
        if (e == null) {
            estrategia = random.nextBoolean() ? new EstrategiaSellani() : new EstrategiaGasparotto();
        } else {
            estrategia = e;
        }
        LOGGER.info("Estrategia: " + estrategia.getClass().getName());
        setNome(APELIDO_BOT);
        if (tf == null) {
            thread = new Thread(this);
        } else {
            thread = tf.newThread(this);
        }
        thread.start();
    }

    /**
     * Thread que processa as notificações recebidas pelo jogador (para não
     * travar a partida enquanto isso oacontece)
     */
    final Thread thread;

    /**
     * Estrategia que está controlando este jogador
     */
    private final Estrategia estrategia;

    /**
     * Situação atual da partida (para a estrategia)
     */
    final SituacaoJogo situacaoJogo = new SituacaoJogo();

    /**
     * Quantidade de jogadores cuja resposta estamos esperando para um pedido de
     * truco.
     */
    private int numRespostasAguardando = 0;

    /**
     * Sinaliza se os adversários aceitaram um pedido de truco
     */
    private boolean aceitaramTruco;

    /**
     * Indica que é a vez deste jogador (para que a thread execute a jogada)
     */
    private boolean minhaVez = false;

    /**
     * Indica se o jogador pode jogar uma carta fechada (sendo a vez dele)
     */
    private boolean podeFechada = false;

    public void setFingeQuePensa(boolean fingeQuePensa) {
        this.fingeQuePensa = fingeQuePensa;
    }

    public void vez(Jogador j, boolean podeFechada) {
        if (this.equals(j)) {
            LOGGER.log(Level.INFO, "Jogador " + this.getPosicao()
                    + " recebeu notificacao de vez");
            this.podeFechada = podeFechada;
            this.minhaVez = true;
            this.estouAguardandoRepostaAumento = false;
        }
    }

    // TODO ao invez de ser o próprio runnable, colocar num método e chamar no lambda
    // TODO quebrar um pouco esse método
    // TODO rever soluções provisórias do crash que era causado pela CPU
    //      tentar jogar carta da rodada anterior
    public void run() {

        LOGGER.log(Level.INFO, "JogadorBot " + this + " (.run) iniciado");
        while (partida == null || !partida.finalizada) {
            sleep(100);

            if (minhaVez && !estouAguardandoRepostaAumento) {
                LOGGER.log(Level.INFO, "Jogador " + this.getPosicao()
                        + " viu que e' sua vez");

                // Dá um tempinho, pra fingir que está "pensando"
                if (fingeQuePensa) {
                    sleep(random.nextInt(500));
                }

                // Atualiza a situação da partida (incluindo as cartas na mão)
                atualizaSituacaoJogo();
                situacaoJogo.podeFechada = podeFechada;

                // Solicita que o estrategia jogue
                int posCarta;
                try {
                    posCarta = estrategia.joga(situacaoJogo);
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, "Erro em joga", e);
                    posCarta = 0;
                }

                // TODO REVERTER, ISSO É SÓ PRA TESTAR
                if (posCarta == -1) { posCarta = 0; }

//                // Se a estratégia pediu truco, processa e desencana de jogar
//                // agora
//                if ((posCarta == -1) && (situacaoJogo.valorProximaAposta != 0)) {
//                    aceitaramTruco = false;
//                    numRespostasAguardando = 2;
//                    LOGGER.log(Level.INFO, "Jogador " + this.getPosicao()
//                            + " vai aumentar aposta");
//                    estouAguardandoRepostaAumento = true;
//                    partida.aumentaAposta(this);
//                    LOGGER.log(Level.INFO, "Jogador " + this.getPosicao()
//                            + " aguardando resposta");
//                    continue;
//                }
//
//                // Se a estratégia pediu truco fora de hora, ignora e joga a
//                // primeira carta
//                if (posCarta == -1) {
//                    LOGGER.log(Level.INFO, "Jogador" + this.getPosicao()
//                            + " pediu truco fora de hora");
//                    posCarta = 0;
//                }

                // Joga a carta selecionada e remove ela da mão
                boolean isFechada = posCarta >= 10;
                if (isFechada) {
                    LOGGER.log(Level.INFO, "Jogador" + this.getPosicao()
                            + " vai tentar jogar fechada");
                    posCarta -= 10;
                }

                Carta c;
                try {
                    c = cartasRestantes.elementAt(posCarta);
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Tentativa de resolver o out-of-bounds que surgiu na 2.3.x
                    // Eu não consigo reproduzir nem faço idéia de como diabos ele
                    // chega aqui com 0 elementos no array, mas vamos evitar o crash
                    // e ver se tudo se resolve sozinho; não deve afetar quem
                    // não tem o problema (como eu)
                    LOGGER.log(Level.INFO, "Out Of Bounds tentando recuperar a carta de cartasRestantes", e);
                    continue;
                }
                c.setFechada(isFechada && podeFechada);
                cartasRestantes.removeElement(c);
                if (!minhaVez) {
                    // Isso acontece MUITO raramente, mas trava o jogo; na dúvida,
                    // a gente seta o minhaVez para false no maoFechada() e
                    // evita esse problema aqui
                    // TODO: deixar rodando e ver se chega aqui, ou se setar pra false no maoFechada resolveu
                    LOGGER.log(Level.INFO, "Jogador " + this.getPosicao()
                        + "IA pedir para jogar " + c + ", mas acabou a mão/rodada");
                    continue;
                }
                LOGGER.log(Level.INFO, "Jogador " + this.getPosicao()
                        + " (" + this.estrategia + ") vai pedir para jogar " + c);
                partida.jogaCarta(this, c);
                minhaVez = false;
            }

            if (recebiPedidoDeAumento) {
                recebiPedidoDeAumento = false;
                atualizaSituacaoJogo();
                if (fingeQuePensa) {
                    sleep(1000 + random.nextInt(1000));
                } else {
                    // Se não estivermos fingindo que pensamos, vamos só dar
                    // uma pausa pequena pra randomizar quem responde primeiro
                    sleep(random.nextInt(50));
                }
                // O sync/if é só pra evitar resposta dupla entre 2 bots
                synchronized (partida) {
                    if (situacaoJogo.posJogadorPedindoAumento != 0) {
                        boolean resposta = false;
                        try {
                            resposta = estrategia.aceitaTruco(situacaoJogo);
                        } catch (Exception e) {
                            LOGGER.log(Level.INFO, "Erro em aceite-aumento", e);
                        }
                        partida.respondeAumento(this, resposta);
                    }
                }
            }

            if (recebiPedidoDeMaoDeX) {
                recebiPedidoDeMaoDeX = false;
                atualizaSituacaoJogo();
                if (fingeQuePensa) {
                    sleep(1000 + random.nextInt(1000));
                } else {
                    // Se não estivermos fingindo que pensamos, vamos só dar
                    // uma pausa pequena pra randomizar quem responde primeiro
                    sleep(random.nextInt(50));
                }
                boolean respostaMaoDeX = false;
                try {
                    // TODO REVERTER, ISSO É SÓ PRA TESTAR
                    respostaMaoDeX = true;
//                    respostaMaoDeX = estrategia.aceitaMaoDeX(
//                            cartasDoParceiroDaMaoDeX, situacaoJogo);
                } catch (Exception e) {
                    LOGGER.log(Level.INFO,
                            "Erro em aceite-mao-de-x no jogador" + this.getPosicao(),
                            e);
                    respostaMaoDeX = random.nextBoolean();
                }
                partida.decideMaoDeX(this, respostaMaoDeX);
            }

            if (estouAguardandoRepostaAumento && (numRespostasAguardando == 0)) {
                estouAguardandoRepostaAumento = false;
                // Se aceitaram, vamos seguir o jogo
                if (aceitaramTruco) {
                    atualizaSituacaoJogo();
                    situacaoJogo.valorProximaAposta = 0;
                    minhaVez = true;
                }
            }

        }
        LOGGER.log(Level.INFO, "JogadorBot " + this + " (.run) finalizado");

    }

    private boolean recebiPedidoDeAumento = false;
    private boolean estouAguardandoRepostaAumento = false;

    private boolean recebiPedidoDeMaoDeX = false;

    private Carta[] cartasDoParceiroDaMaoDeX;

    public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {
        if (j.getEquipe() == this.getEquipeAdversaria()) {
            recebiPedidoDeAumento = true;
        }
    }

    /**
     * Atualiza a situação do jogo (para as estratégias)
     */
    private void atualizaSituacaoJogo() {
        partida.atualizaSituacao(situacaoJogo, this);
        if (partida.isPlacarPermiteAumento()) {
            situacaoJogo.valorProximaAposta = valorProximaAposta;
        } else {
            situacaoJogo.valorProximaAposta = 0;
        }
        int numCartas = cartasRestantes.size();
        situacaoJogo.cartasJogador = new Carta[numCartas];
        for (int i = 0; i < numCartas; i++) {
            Carta c = cartasRestantes.elementAt(i);
            situacaoJogo.cartasJogador[i] = new Carta(c.getLetra(),
                    c.getNaipe());
        }
    }

    int valorProximaAposta;

    @Override
    public void aceitouAumentoAposta(Jogador j, int valor, int rndFrase) {

        // Se estou esperando resposta, contabiliza
        if (numRespostasAguardando > 0) {
            numRespostasAguardando = 0;
            aceitaramTruco = true;
        }

        if (j.getEquipe() == this.getEquipe()) {
            // Nós aceitamos um truco, então podemos aumentar
            // (i.e., se foi truco, podemos pedir 6, se for 6, podemos pedir 9,
            // etc.) até o limite de 12
            if (valor != 12) {
                valorProximaAposta = valor + 3;
            }
        } else {
            // Eles aceitaram um truco, temos que esperar eles pedirem
            valorProximaAposta = 0;
        }

    }

    @Override
    public void recusouAumentoAposta(Jogador j, int rndFrase) {

        // Se estivermos aguardando resposta, contabiliza (e deixa o adversário
        // perceber)
        if (numRespostasAguardando > 0) {
            numRespostasAguardando--;
            Thread.yield();
        }

    }

    public void rodadaFechada(int numMao, int resultado, Jogador jogadorQueTorna) {
        // Não faz nada
    }

    public void maoFechada(int[] pontosEquipe) {

        LOGGER.log(Level.INFO, "Jogador " + this.getPosicao()
        + " recebeu notificação de mão fechada; mudando minhaVez de " + minhaVez + "para false");

        // Cancela todas as jogadas em aguardo
        minhaVez = false;
        estouAguardandoRepostaAumento = false;
        recebiPedidoDeAumento = false;
    }

    public void jogoFechado(int numEquipeVencedora, int rndFrase) {
        estrategia.partidaFinalizada(numEquipeVencedora);
    }

    public void cartaJogada(Jogador j, Carta c) {
        // Não faz nada
    }

    public void inicioMao(Jogador jogadorQueAbre) {

        // Guarda as cartas que estão na mão do jogador
        cartasRestantes.removeAllElements();
        for (int i = 0; i <= 2; i++) {
            cartasRestantes.addElement(this.getCartas()[i]);
        }

        // Libera o jogador para pedir truco (se nao estivermos em mao de 11)
        valorProximaAposta = (partida.isPlacarPermiteAumento() ? 3 : 0);

    }

    /**
     * Cartas que ainda não foram jogadas
     */
    private final Vector<Carta> cartasRestantes = new Vector<>(3);

    public void inicioPartida(int placarEquipe1, int placarEquipe2) {
        // Por ora não faz nada
    }

    public void decidiuMaoDeX(Jogador j, boolean aceita, int rndFrase) {
        // Por ora não faz nada
    }

    public void informaMaoDeX(Carta[] cartasParceiro) {
        cartasDoParceiroDaMaoDeX = cartasParceiro;
        recebiPedidoDeMaoDeX = true;
    }

    public void jogoAbortado(int posicao, int rndFrase) {
        // Não precisa tratar
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            LOGGER.log(Level.INFO, "Interrupted during sleep", e);
        }
    }

}
