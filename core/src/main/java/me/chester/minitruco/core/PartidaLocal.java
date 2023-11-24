package me.chester.minitruco.core;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Executa o jogo efetivamente.
 * <p>
 * A classe mantém o estado do jogo e toma todas as decisões.
 */
public class PartidaLocal extends Partida {

    private final static Logger LOGGER = Logger.getLogger("PartidaLocal");

    private final static Random rand = new Random();

    /**
     * Cria uma nova partida.
     * <p>
     * Ela só inicia quando forem adicionados os quatro jogadores.
     *
     * @param humanoDecide   Se verdadeira e a partida não tiver clientes remotos, um bot
     *                       parceiro de humano não pode aceitar aumento ou mão de 10/11
     * @param jogoAutomatico Se verdadeira, o humano "joga sozinho" (teste de stress)
     * @param modoStr        String de 1 caractere que determina
     *                       se o truco é paulista, mineiro, etc.
     */
    public PartidaLocal(boolean humanoDecide, boolean jogoAutomatico, String modoStr) {
        super(Modo.fromString(modoStr));
        this.baralho = new Baralho(modo.isBaralhoLimpo());
        this.humanoDecide = humanoDecide;
        this.jogoAutomatico = jogoAutomatico;
    }

    /**
     * Usa um baralho não-aleatório (para testes)
     *
     * @param seed
     */
    public void setSeedBaralho(long seed) {
        this.baralho = new Baralho(modo.isBaralhoLimpo(), seed);
    }

    /**
     * Baralho que será usado durante essa partida
     */
    private Baralho baralho;

    /**
     * Resultados de cada rodada (1 para vitória da equipe 1/3, 2 para vitória
     * da equipe 2/4 e 3 para empate)
     */
    private final int[] resultadoRodada = new int[3];

    /**
     * Valor atual da mão (1, 3, 6, 9 ou 12)
     */
    private int valorMao;

    /**
     * Jogador que está pedindo aumento de aposta (pedindo truco, 6, 9 ou 12).
     * Se for null, ninguém está pedindo
     */
    private Jogador jogadorPedindoAumento;

    /**
     * Status das respsotas para um pedido de aumento de aposta para cada
     * jogador.
     * <p>
     * false signfica que não respondeu ainda, true que respondeu recusando
     */
    private final boolean[] recusouAumento = new boolean[4];

    /**
     * Posição (1 a 4) do jogador da vez
     */
    private int posJogadorDaVez;

    /**
     * Jogador que abriu a rodada
     */
    private Jogador jogadorAbriuRodada;

    /**
     * Jogador que abriu a mão
     */
    private Jogador jogadorAbriuMao;

    /**
     * Indica, para cada jogador, se estamos aguardando a resposta dele
     * de aceite/recusa da mão de 10/11
     */
    private final boolean[] aguardandoRespostaMaoDeX = new boolean[4];

    /**
     * Sinaliza para o loop principal que alguém jogou uma carta
     */
    boolean alguemJogou = false;

    /**
     * Se alguemJogou = true, é o alguém que jogou
     */
    private Jogador jogadorQueJogou;

    /**
     * Se alguemJogou = true, é a carta jogada
     */
    private Carta cartaJogada;

    private final boolean humanoDecide;
    private final boolean jogoAutomatico;

    private boolean performanceMaxima = false;

    /*
     * (non-Javadoc)
     *
     * @see mt.JogoGenerico#run()
     */
    public void run() {

        // Avisa os jogadores que a partida vai começar
        LOGGER.log(Level.INFO, "Partida (.run) iniciada");
        for (Jogador interessado : jogadores) {
            interessado.inicioPartida(pontosEquipe[0], pontosEquipe[1]);
        }

        // Inicia a primeira rodada, usando o jogador na posição 1, e processa
        // as jogadas até alguém ganhar ou a partida ser abortada (o que pode
        // ocorrer em paralelo, daí os múltiplos checks a finalizada)
        iniciaMao(getJogador(1));
        while (pontosEquipe[0] < 12 && pontosEquipe[1] < 12 && !finalizada) {
            while ((!alguemJogou) && (!finalizada)) {
                sleep();
            }
            if (!finalizada) {
                LOGGER.log(Level.INFO, "alguém jogou!");
                processaJogada();
                alguemJogou = false;
            }
        }
        LOGGER.log(Level.INFO, "Partida (.run) finalizada");
    }

    /**
     * Inicia uma mão (i.e., uma distribuição de cartas)
     *
     * @param jogadorQueAbre
     *            Jogador que abre a rodada
     */
    private void iniciaMao(Jogador jogadorQueAbre) {

        // Embaralha as cartas e reinicia a mesa
        baralho.embaralha();
        cartasJogadasPorRodada = new Carta[3][4];

        // Distribui as cartas de cada jogador
        for (int j = 1; j <= 4; j++) {
            Jogador jogador = getJogador(j);
            Carta[] cartas = new Carta[3];
            for (int i = 0; i <= 2; i++) {
                cartas[i] = baralho.sorteiaCarta();
            }
            jogador.setCartas(cartas);
        }

        // Vira a carta da mesa, determinando a manilha
        cartaDaMesa = baralho.sorteiaCarta();
        setManilha(cartaDaMesa);

        // Inicializa a mão
        valorMao = modo.valorInicialDaMao();

        jogadorPedindoAumento = null;
        numRodadaAtual = 1;
        jogadorAbriuMao = jogadorAbriuRodada = jogadorQueAbre;

        LOGGER.log(Level.INFO, "Abrindo mao com j" + jogadorQueAbre.getPosicao()
                + ",manilha=" + getManilha());

        // Abre a primeira rodada, informando a carta da mesa e quem vai abrir
        posJogadorDaVez = jogadorQueAbre.getPosicao();
        for (int i = 3; i >= 0; i--) {
            jogadores[i].inicioMao(jogadorQueAbre);
        }

        if (pontosEquipe[0] == modo.pontuacaoParaMaoDeX()
                ^ pontosEquipe[1] == modo.pontuacaoParaMaoDeX()) {
            if (pontosEquipe[0] == modo.pontuacaoParaMaoDeX()) {
                setEquipeAguardandoMaoDeX(1);
                getJogador(1).informaMaoDeX(getJogador(3).getCartas());
                getJogador(3).informaMaoDeX(getJogador(1).getCartas());
            } else {
                setEquipeAguardandoMaoDeX(2);
                getJogador(2).informaMaoDeX(getJogador(4).getCartas());
                getJogador(4).informaMaoDeX(getJogador(2).getCartas());
            }
        } else {
            // Se for uma mão normal, passa a vez para o jogador que abre
            setEquipeAguardandoMaoDeX(0);
            notificaVez();
        }

    }

    /**
     * Processa uma jogada e passa a vez para o próximo jogador (ou finaliza a
     * rodoada/mão/partida), notificando os jogadores apropriadamente
     *
     */
    private void processaJogada() {

        Jogador j = this.jogadorQueJogou;
        Carta c = this.cartaJogada;

        LOGGER.log(Level.INFO, "processaJogada: j" + j.getPosicao() + " joga " + c +
                "; jogadorPedindoAumento:" + (jogadorPedindoAumento == null ? "null" : jogadorPedindoAumento.getPosicao()) +
                "; isAguardandoRespostaMaoDeX:" + isAguardandoRespostaMaoDeX() +
                "; jogadorDaVez: "+getJogadorDaVez().getPosicao());

        // Se a partida acabou, a mesa não estiver completa, já houver alguém
        // trucando, estivermos aguardando ok da mão de 10/11 ou não for a vez do
        // cara, recusa
        if (finalizada || numJogadores < 4 || jogadorPedindoAumento != null
                || (isAguardandoRespostaMaoDeX())
                || !j.equals(getJogadorDaVez())) {
            return;
        }

        // Verifica se a carta já não foi jogada anteriormente (normalmente não
        // deve acontecer - mesmo caso do check anterior)
        for (int i = 0; i <= 2; i++) {
            for (int k = 0; k <= 3; k++) {
                if (c.equals(cartasJogadasPorRodada[i][k])) {
                    LOGGER.log(Level.INFO, "carta jogada anteriormente: "+ c + "," + i + "," + k);
                    renotificaVezBot();
                    return;
                }
            }
        }

        // Verifica se a carta realmente pertence ao jogador
        Carta cartaNaMaoDoJogador = null;
        for (int i = 0; i <= 2; i++) {
            if (j.getCartas()[i].equals(c)) {
                cartaNaMaoDoJogador = c;
            }
        }
        if (cartaNaMaoDoJogador == null) {
            LOGGER.log(Level.INFO, "j" + j.getPosicao() + " tentou jogar " + c +
                    " mas esta carta não está na mão dele");
            renotificaVezBot();
            return;
        }

        // Garante que a regra para carta fechada seja respeitada
        if (!isPodeFechada()) {
            c.setFechada(false);
        }

        LOGGER.log(Level.INFO, "J" + j.getPosicao() + " joga " + c);

        // Dá a carta como jogada, notificando os jogadores
        cartasJogadasPorRodada[numRodadaAtual - 1][j.getPosicao() - 1] = c;
        for (Jogador interessado : jogadores) {
            interessado.cartaJogada(j, c);
        }

        // Passa a vez para o próximo jogador
        posJogadorDaVez++;
        if (posJogadorDaVez == 5) {
            posJogadorDaVez = 1;
        }
        if (posJogadorDaVez == jogadorAbriuRodada.getPosicao()) {

            // Completou a volta da rodada - acha o valor da maior carta da mesa
            Carta[] cartas = getCartasDaRodada(numRodadaAtual);
            int valorMaximo = 0;
            for (int i = 0; i <= 3; i++) {
                valorMaximo = Math.max(valorMaximo, getValorTruco(cartas[i]));
            }

            // Determina a equipe vencedora (1/2= equipe 1 ou 2; 3=empate) e o
            // jogador que vai "tornar", i.e., abrir a próxima rodada
            setResultadoRodada(numRodadaAtual, 0);
            Jogador jogadorQueTorna = null;
            for (int i = 0; i <= 3; i++) {
                if (getValorTruco(cartas[i]) == valorMaximo) {
                    if (jogadorQueTorna == null) {
                        jogadorQueTorna = getJogador(i + 1);
                    }
                    if (i == 0 || i == 2) {
                        setResultadoRodada(numRodadaAtual,
                                getResultadoRodada(numRodadaAtual) | 1);
                    } else {
                        setResultadoRodada(numRodadaAtual,
                                getResultadoRodada(numRodadaAtual) | 2);
                    }
                }
            }

            LOGGER.log(Level.INFO, "Rodada fechou. Resultado: "
                    + getResultadoRodada(numRodadaAtual));

            // Se houve vencedor, passa a vez para o jogador que fechou a
            // vitória, senão deixa quem abriu a mão anterior abrir a próxima
            if (getResultadoRodada(numRodadaAtual) != 3) {
                posJogadorDaVez = jogadorQueTorna.getPosicao();
            } else {
                jogadorQueTorna = getJogadorDaVez();
            }

            // Notifica os interessados que a mão foi feita
            for (Jogador interessado : jogadores) {
                interessado.rodadaFechada(numRodadaAtual,
                        getResultadoRodada(numRodadaAtual), jogadorQueTorna);
            }

            // Verifica se já temos vencedor na rodada
            int resultadoRodada = 0;
            if (numRodadaAtual == 2) {
                if (getResultadoRodada(1) == 3 && getResultadoRodada(2) != 3) {
                    // Empate na 1a. mão, quem fez a 2a. leva
                    resultadoRodada = getResultadoRodada(2);
                } else if (getResultadoRodada(1) != 3
                        && getResultadoRodada(2) == 3) {
                    // Empate na 2a. mão, quem fez a 1a. leva
                    resultadoRodada = getResultadoRodada(1);
                } else if (getResultadoRodada(1) == getResultadoRodada(2)
                        && getResultadoRodada(1) != 3) {
                    // Quem faz as duas primeiras leva
                    resultadoRodada = getResultadoRodada(2);
                }
            } else if (numRodadaAtual == 3) {
                if (getResultadoRodada(3) != 3) {
                    // Quem faz a 3a. leva
                    resultadoRodada = getResultadoRodada(3);
                } else {
                    // Se a 3a. empatou, a 1a. decide
                    resultadoRodada = getResultadoRodada(1);
                }
            }

            // Se já tivermos vencedor (ou empate final), notifica e abre uma
            // nova mao, senão segue a vida na mão seguinte
            if (resultadoRodada != 0) {
                // Soma os pontos (se não deu emptate)
                if (resultadoRodada != 3) {
                    pontosEquipe[resultadoRodada - 1] += valorMao;
                }
                fechaMao();
            } else {
                numRodadaAtual++;
                jogadorAbriuRodada = jogadorQueTorna;
                notificaVez();
            }
        } else {
            notificaVez();
        }

    }

    /**
     * Conclui a mão atual, e, se a partida não acabou, inicia uma nova.
     *
     */
    private void fechaMao() {

        LOGGER.log(Level.INFO, "Mao fechou. Placar: " + pontosEquipe[0] + " a "
                + pontosEquipe[1]);

        // Notifica os interessados que a rodada acabou, e, se for o caso, que o
        // partida acabou também
        // A notificação é feita em ordem reversa para que um JogadorBluetooth não tenha
        // que esperar pelas animacões de um JogadorHumano
        int rndFrase = Math.abs(rand.nextInt());
        for (int i = 3; i >= 0; i--) {
            Jogador interessado = jogadores[i];
            interessado.maoFechada(pontosEquipe);
            if (pontosEquipe[0] > modo.pontuacaoParaMaoDeX()) {
                interessado.jogoFechado(1, rndFrase);
                finalizada = true;
            } else if (pontosEquipe[1] > modo.pontuacaoParaMaoDeX()) {
                interessado.jogoFechado(2, rndFrase);
                finalizada = true;
            }
        }

        // Se ainda estivermos em jogo, incia a nova mao
        if (pontosEquipe[0] <= modo.pontuacaoParaMaoDeX()
                && pontosEquipe[1] <= modo.pontuacaoParaMaoDeX()) {
            int posAbre = jogadorAbriuMao.getPosicao() + 1;
            if (posAbre == 5)
                posAbre = 1;
            iniciaMao(getJogador(posAbre));
        }

    }

    // /// NOTIFICAÇÕES RECEBIDAS DOS JOGADORES

    /*
     * (non-Javadoc)
     *
     * @see mt.JogoGenerico#jogaCarta(mt.Jogador, mt.Carta)
     */
    public synchronized void jogaCarta(Jogador j, Carta c) {

        // Se alguém tiver jogado e ainda não foi processado, segura a onda
        while (alguemJogou) {
            sleep();
        }

        this.jogadorQueJogou = j;
        this.cartaJogada = c;
        this.alguemJogou = true;

    }

    /*
     * (non-Javadoc)
     *
     * @see mt.JogoGenerico#decideMaoDeX(mt.Jogador, boolean)
     */
    public synchronized void decideMaoDeX(Jogador j, boolean aceita) {

        // Só entra se estivermos jogando e se estivermos agurardando resposta
        // daquele jogador para a pergunta (isso é importante para evitar duplo
        // início)
        if (finalizada || !aguardandoRespostaMaoDeX[j.getPosicao() - 1])
            return;

        LOGGER.log(Level.INFO, "J" + j.getPosicao() + (aceita ? "" : " nao")
                + " quer jogar mao de 11 ");

        // Se for um bot parceiro de humano numa partida 100% local, trata como recusa
        // (quem decide mão de 10/11 é o humano) e nem notifica (silenciando o balão)
        if (isIgnoraDecisao(j)) {
            aceita = false;
        } else {
            // Avisa os outros jogadores da decisão
            int rndFrase = Math.abs(rand.nextInt());
            for (Jogador interessado : jogadores) {
                interessado.decidiuMaoDeX(j, aceita, rndFrase);
            }
        }

        aguardandoRespostaMaoDeX[j.getPosicao() - 1] = false;

        if (aceita) {
            // Se aceitou, desencana da resposta do parceiro e pode tocar o
            // jogo, valendo o valor da mão de 10/11
            aguardandoRespostaMaoDeX[j.getParceiro() - 1] = false;
            valorMao = modo.valorDaMaoDeX();
            notificaVez();
        } else {
            // Se recusou (e o parceiro também), a equipe adversária ganha
            // a pontuação da mão comum
            if (!aguardandoRespostaMaoDeX[j.getParceiro() - 1]) {
                pontosEquipe[j.getEquipeAdversaria() - 1] += modo
                        .valorInicialDaMao();
                fechaMao();
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see mt.JogoGenerico#aumentaAposta(mt.Jogador)
     */
    public void aumentaAposta(Jogador j) {

        // Se a partida estiver finalizada, a mesa não estiver completa, já houver
        // alguém trucando, estivermos aguardando a mão de 10/11 ou não for a vez
        // do cara, recusa
        if ((finalizada) || (numJogadores < 4)
                || (jogadorPedindoAumento != null)
                || isAguardandoRespostaMaoDeX() || !j.equals(getJogadorDaVez())) {
            return;
        }

        LOGGER.log(Level.INFO, "Jogador  " + j.getPosicao() + " pede aumento");

        // Atualiza o status
        this.jogadorPedindoAumento = j;
        for (int i = 0; i <= 3; i++)
            recusouAumento[i] = false;

        // Notifica todos os jogadores
        int valor = modo.valorSeHouverAumento(valorMao);
        int rndFrase = Math.abs(rand.nextInt());
        for (Jogador interessado : jogadores) {
            interessado.pediuAumentoAposta(j, valor, rndFrase);
        }
        LOGGER.log(Level.INFO, "Jogadores notificados do aumento");
    }

    /*
     * (non-Javadoc)
     *
     * @see mt.JogoGenerico#respondeAumento(mt.Jogador, boolean)
     */
    public synchronized void respondeAumento(Jogador j, boolean aceitou) {
        // Apenas os adversários de quem trucou respondem
        if (jogadorPedindoAumento == null
                || jogadorPedindoAumento.getEquipeAdversaria() != j.getEquipe()) {
            return;
        }

        LOGGER.log(Level.INFO, "Jogador  " + j.getPosicao()
                + (aceitou ? "aceitou" : "recusou"));

        int posParceiro = (j.getPosicao() + 1) % 4 + 1;
        // Se, numa partida 100% local (só o humano e bots)
        // o bot parceiro do humano aceita, trata como recusa
        // (mas notifica humano do aceite)
        boolean ignorarAceite = isIgnoraDecisao(j) && aceitou;
        int rndFrase = Math.abs(rand.nextInt());
        if (aceitou && !ignorarAceite) {
            // Se o jogador aceitou, seta o novo valor, notifica a galera e tira
            // a partida da situtação de truco
            valorMao = modo.valorSeHouverAumento(valorMao);
            jogadorPedindoAumento = null;
            for (Jogador interessado : jogadores) {
                interessado.aceitouAumentoAposta(j, valorMao, rndFrase);
            }
        } else {
            // Primeiro notifica todos os jogadores da recusa
            // (se for um aceite ignorado, diz pro humano que aceitou, só pra ele saber o que seria feito)
            for (Jogador interessado : jogadores) {
                if (aceitou && ignorarAceite && (interessado == jogadores[posParceiro - 1])) {
                    interessado.aceitouAumentoAposta(j, valorMao, rndFrase);
                } else {
                    interessado.recusouAumentoAposta(j, rndFrase);
                }
            }
            if (recusouAumento[posParceiro - 1]) {
                // Se o parceiro também recusou, derrota da dupla
                pontosEquipe[jogadorPedindoAumento.getEquipe() - 1] += valorMao;
                fechaMao();
            } else {
                // Sinaliza a recusa, deixando a decisão na mão do parceiro
                recusouAumento[j.getPosicao() - 1] = true;
            }
        }
    }

    @Override
    public boolean semJogadoresRemotos() {
        for (int i = 0; i < 3; i ++)
            if (!(jogadores[i] instanceof JogadorHumano || jogadores[i] instanceof JogadorBot)) {
                return false;
            }
        return true;
    }

    @Override
    public void abandona(int posicao) {
        finalizada = true;
        int rndFrase = Math.abs(rand.nextInt());
        for (Jogador j : jogadores) {
            if (j != null) {
                j.jogoAbortado(posicao, rndFrase);
            }
        }
    }

    /**
     * Determina se o jogador em questão deve ter sua decisão (aceite de aumento ou mão 11) ignorada.
     *
     * @param jogador jogador que acabou de tomar uma decisão
     * @return true se o jogador for um bot cujo parceiro é humano em um partida 100% local
     */
    public boolean isIgnoraDecisao(Jogador jogador) {
        int posParceiro = (jogador.getPosicao() + 1) % 4 + 1;
        return  humanoDecide &&
                semJogadoresRemotos() &&
                (jogador instanceof JogadorBot) &&
                (jogadores[posParceiro - 1] instanceof JogadorHumano);
    }

    /**
     * Determina qual a equipe que está aguardando mão de 10/11
     *
     * @param i
     *            1 ou 2 para a respectiva equipe, 0 para ninguém aguardando mão
     *            de 11 (partida normal)
     */
    private void setEquipeAguardandoMaoDeX(int i) {
        aguardandoRespostaMaoDeX[0] = aguardandoRespostaMaoDeX[2] = (i == 1);
        aguardandoRespostaMaoDeX[1] = aguardandoRespostaMaoDeX[3] = (i == 2);
    }

    private int getResultadoRodada(int mao) {
        return resultadoRodada[mao - 1];
    }

    private void setResultadoRodada(int mao, int valor) {
        resultadoRodada[mao - 1] = valor;
    }

    /**
     * Informa aos jogadores participantes que é a vez de um deles.
     */
    private void notificaVez() {

        // Esses dados têm que ser coletados *antes* de chamar as Threads.
        // Motivo: se uma delas resolver jogar, a informação para as outras pode
        // ficar destaualizada.
        Jogador j = getJogadorDaVez();
        boolean pf = isPodeFechada();

        for (Jogador interessado : jogadores) {
            interessado.vez(j, pf);
        }

    }

    /**
     * Se o jogador da vez for bot, re-notifica ele que é sua vez.
     * <p>
     * Isso é usado para casos em que o bot joga uma jogada inválida (ex.: porque
     * jogou depois que a mão fechou), evitando que ela fique "travada"
     */
    private void renotificaVezBot() {
        Jogador j = getJogadorDaVez();
        boolean pf = isPodeFechada();

        if (j instanceof JogadorBot) {
            j.vez(j, pf);
        }
    }

    /**
     * Informa se o jogador da vez pode jogar carta fechada (se mudar a regra,
     * basta alterar aqui).
     * <p>
     * Regra atual: só vale carta fechada se não for a 1a. rodada e se o
     * parceiro não tiver jogado fechada também
     *
     */
    private boolean isPodeFechada() {
        Carta cartaParceiro = cartasJogadasPorRodada[numRodadaAtual - 1][getJogadorDaVez()
                .getParceiro() - 1];
        return (numRodadaAtual > 1 && (cartaParceiro == null || !cartaParceiro
                .isFechada()));
    }

    /**
     * Recupera o jogador cuja vez é a atual
     *
     */
    private Jogador getJogadorDaVez() {
        return getJogador(posJogadorDaVez);
    }

    /*
     * (non-Javadoc)
     *
     * @see mt.JogoGenerico#atualizaSituacao(mt.SituacaoJogo, mt.Jogador)
     */
    public void atualizaSituacao(SituacaoJogo s, Jogador j) {
        s.baralhoSujo = !modo.isBaralhoLimpo();
        s.manilha = getManilha();
        s.numRodadaAtual = this.numRodadaAtual;
        s.posJogador = j.getPosicao();
        s.posJogadorQueAbriuRodada = this.jogadorAbriuRodada.getPosicao();
        if (this.jogadorPedindoAumento != null)
            s.posJogadorPedindoAumento = this.jogadorPedindoAumento
                    .getPosicao();
        s.valorMao = this.valorMao;

        System.arraycopy(this.pontosEquipe, 0, s.pontosEquipe, 0, 2);
        System.arraycopy(this.resultadoRodada, 0, s.resultadoRodada, 0, 3);

        for (int i = 0; i <= 2; i++)
            for (int k = 0; k <= 3; k++) {
                Carta c = cartasJogadasPorRodada[i][k];
                if (c == null) {
                    s.cartasJogadas[i][k] = null;
                } else if (s.cartasJogadas[i][k] == null) {
                    s.cartasJogadas[i][k] = new Carta(c.getLetra(),
                            c.getNaipe());
                } else {
                    s.cartasJogadas[i][k].setLetra(c.getLetra());
                    s.cartasJogadas[i][k].setNaipe(c.getNaipe());
                }
                // Se for uma carta fechada, limpa letra/naipe na cópia (pra
                // evitar que uma estratégia maligna tente espiar uma carta
                // fechada)
                if (c != null && c.isFechada()) {
                    s.cartasJogadas[i][k].setFechada(true);
                    s.cartasJogadas[i][k].setLetra(Carta.LETRA_NENHUMA);
                    s.cartasJogadas[i][k].setNaipe(Carta.NAIPE_NENHUM);
                }
            }

    }

    /**
     * Verifica se estamos aguardando resposta para mão de 10/11
     *
     * @return true se falta alguém responder, false caso contrário
     */
    private boolean isAguardandoRespostaMaoDeX() {
        for (int i = 0; i <= 3; i++) {
            if (aguardandoRespostaMaoDeX[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Configura boost de performance (para treinar AI)
     *
     * @param performanceMaxima se true, a classe opera na performance máxima
     *                          (a um custo de CPU). Default é false.
     */
    public void setPerformanceMaxima(boolean performanceMaxima) {
        this.performanceMaxima = performanceMaxima;
    }

    private void sleep() {
        try {
            Thread.sleep(performanceMaxima ? 1 : 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isJogoAutomatico() {
        return jogoAutomatico;
    }
}
