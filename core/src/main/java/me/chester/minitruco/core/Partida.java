package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Representa uma partida de truco.
 * <p>
 * As implementações desta classe irão cuidar de executar o jogo (no caso de
 * <code>PartidaLocal</code>) ou manter a comunicação com uma partida em execução
 * remota (<code>PartidaRemota</code>). Em qualquer caso, os objetos Jogador não
 * terão ciência de onde a partida está se passando.
 * <p>
 * Recebe <i>comandos</i> dos jogadores através de seus métodos (ex.:
 * <code>jogaCarta()</code>, <code>aumentaAposta()</code>) e envia <i>notificações</i>
 * a eles chamando métodos deles (Ex.: <code>inicioPartida()</code>, <code>cartaJogada()</code>).
 * <p>
 * A classe é um <code>Runnable</code> para permitir tanto a execução em Thread
 * (que "viverá" o tempo de uma partida completa) quanto a chamada direta ao
 * método <code>run()</code> (para rodar um teste, por exemplo).
 *
 * @see Jogador
 */
public abstract class Partida implements Runnable {

    /**
     * Referência para determinar a ordem das cartas no truco
     */
    protected static final String letrasOrdenadas = "4567QJKA23";

    /**
     * Modalidade do jogo (paulista, mineiro, baralho limpo, manilha velha...)
     */
    protected final Modo modo;

    /**
     * Rodada que estamos jogando (de 1 a 3).
     * <p>
     * (as implementações devem manter atualizado)
     */
    int numRodadaAtual;

    public Partida(Modo modo) {
        this.modo = modo;
    }

    /**
     * Calcula um valor relativo para a carta, considerando as manilhas em jogo
     * <p>
     * Este método está na superclasse porque, no início da rodada, toda a
     * informação necessária consiste na manilha e em sua regra, e essas já
     * foram transmitidas, evitando assim, dúzias de comandos.
     *
     * @param c
     *            Carta cujo valor desejamos
     */
    public static int getValorTruco(Carta c, char letraManilha) {

        if (c.isFechada()) {
            // Cartas fechadas sempre têm valor 0
            return 0;
        }

        if (letraManilha == SituacaoJogo.MANILHA_VELHA) {
            if (c.getLetra() == '7' && c.getNaipe() == Carta.NAIPE_OUROS) {
                return 11;
            } else if (c.getLetra() == 'A'
                    && c.getNaipe() == Carta.NAIPE_ESPADAS) {
                return 12;
            } else if (c.getLetra() == '7' && c.getNaipe() == Carta.NAIPE_COPAS) {
                return 13;
            } else if (c.getLetra() == '4' && c.getNaipe() == Carta.NAIPE_PAUS) {
                return 14;
            }
        }

        if (c.getLetra() == letraManilha) {
            // Valor de 11 a 14, conforme o naipe
            switch (c.getNaipe()) {
            case Carta.NAIPE_OUROS:
                return 11;
            case Carta.NAIPE_ESPADAS:
                return 12;
            case Carta.NAIPE_COPAS:
                return 13;
            case Carta.NAIPE_PAUS:
                return 14;
            default:
                return 0;
            }
        } else {
            // Valor de 1 a 10 conforme a letra
            return letrasOrdenadas.indexOf(c.getLetra()) + 1;
        }
    }

    /**
     * Jogadores adicionados a esta partida
     */
    protected final Jogador[] jogadores = new Jogador[4];

    /**
     * Número de jogadores adicionados até agora
     */
    protected int numJogadores = 0;

    /**
     * Guarda quais cartas foram jogadas em cada rodada.
     * <p>
     * (as implementações devem alimentar este array)
     */
    protected Carta[][] cartasJogadasPorRodada;

    public static String textoModo(String modo) {
        switch (modo) {
            case "P": return "Truco Paulista";
            case "M": return "Truco Mineiro";
            case "L": return "Baralho Limpo";
            case "V": return "Manilha Velha";
        }
        return null;
    }

    public Modo getModo() {
        return modo;
    }

    /**
     * Inicia o jogo.
     * <p>
     * Este método só vai retornar quando a partida for encerrada, então
     * não deve ser chamado em threads que não querem esperar (Ex.: UI Thread)
     */
    public abstract void run();

    /**
     * Informa à partida que o jogador quer descartar aquela carta.
     * <p>
     * Tem que ser a vez dele e não pode haver ninguém trucando.
     * <p>
     * A rotina não verifica se o jogador realmente possuía aquela carta -
     * assume-se que as instâncias de Jogador são honestas e se protegem de
     * clientes remotos desonestos
     *
     */
    public abstract void jogaCarta(Jogador j, Carta c);

    /**
     * Informa à partida a resposta daquele jogador a uma mão de 10/11
     *
     * @param j
     *            Jogador que está respondendo
     * @param aceita
     *            true se o jogador topa jogar, false se deixar para o parceiro
     *            decidir
     */
    public abstract void decideMaoDeX(Jogador j, boolean aceita);

    /**
     * Informa à partida que o jogador solicitou um aumento de aposta ("truco",
     * "seis", etc.).
     * <p>
     * Os jogadores são notificados, e a aposta será efetivamente aumentada se
     * um dos adversários responder positivamente.
     * <p>
     * Observe-se que a vez do jogador fica "suspensa", já que lançamentos de
     * cartas só são aceitos se não houver ninguém trucando. Como o jogador
     * atualmente só pode trucar na sua vez, isso não é problema.
     *
     * @param j
     *            Jogador que está solicitando o aumento
     */
    public abstract void aumentaAposta(Jogador j);

    /**
     * Informa à partida que o jogador respondeu a um pedido de aumento de aposta
     *
     * @param j
     *            Jogador que respondeu ao pedido
     * @param aceitou
     *            <code>true</code> se ele mandou descer, <code>false</code> se
     *            correu
     */
    public abstract void respondeAumento(Jogador j, boolean aceitou);

    /**
     * Retorna as cartas jogadas por cada jogador naquela rodada
     *
     * @param rodada
     *            número de 1 a 3
     * @return cartas jogadas naquela rodada (índice = posição do Jogador-1)
     */
    public Carta[] getCartasDaRodada(int rodada) {
        return cartasJogadasPorRodada[rodada - 1];
    }

    /**
     * Carta que determina a manilha (em partida que não usa manilha velha)
     */
    public Carta cartaDaMesa;

    /**
     * Atualiza um objeto que contém a situação da partida (exceto pelas cartas do
     * jogador)
     *
     * @param s
     *            objeto a atualizar
     * @param j
     *            Jogador que receberá a situação
     */
    public abstract void atualizaSituacao(SituacaoJogo s, Jogador j);

    protected int getValorTruco(Carta c) {
        return getValorTruco(c, this.getManilha());
    }

    /**
     * Adiciona um jogador a esta partida.
     * <p>
     * Ele será colocado na próxima posição disponível, e passa a receber
     * eventos da partida.
     *
     * @param jogador
     *            Jogador que será adicionado (bot, humano, etc)
     * @return true se adicionou o jogador, false se não conseguiu (ex.: mesa
     *         lotada)
     */
    public synchronized boolean adiciona(Jogador jogador) {

        // Se for jogador, só entra se a mesa ainda tiver vaga.
        if (numJogadores == 4) {
            return false;
        }

        // Adiciona na lista e notifica a todos (incluindo ele) de sua presença
        jogadores[numJogadores] = jogador;
        numJogadores++;
        jogador.partida = this;
        jogador.setPosicao(numJogadores);
        for (Jogador j : jogadores) {
            if (j != null) {
                j.entrouNoJogo(jogador, this);
            }
        }
        return true;

    }

    /**
     * Recupera um jogador inscrito
     *
     * @param posicao
     *            valor de 1 a 4
     * @return Objeto correspondente àquela posição
     */
    protected Jogador getJogador(int posicao) {
        return jogadores[posicao - 1];
    }

    private char manilha;

    /**
     * Pontos de cada equipe na partida.
     */
    protected final int[] pontosEquipe = { 0, 0 };

    /**
     * Indica que a partida foi finalizada (para evitar que os bots fiquem
     * "rodando em falso" caso a partida seja abortada
     */
    public boolean finalizada = false;

    /**
     * @return Letra correspondente à manilha, ou constante em caso de manilha
     *         fixa
     * @see SituacaoJogo#MANILHA_VELHA
     */
    public char getManilha() {
        return manilha;
    }

    /**
     * Determina a letra da manilha, baseado na carta virada (o "vira").
     * <p>
     * Deve ser chamado a cada inicialização de mão.
     *
     * @param c
     *            Carta virada. Ignorado se for partida com manilha velha
     */
    public void setManilha(Carta c) {

        cartaDaMesa = c;

        if (modo.isManilhaVelha()) {
            manilha = SituacaoJogo.MANILHA_VELHA;
            return;
        }

        int posManilha = letrasOrdenadas.indexOf(c.getLetra()) + 1;
        if (posManilha == letrasOrdenadas.length()) {
            posManilha = 0;
        }
        manilha = letrasOrdenadas.charAt(posManilha);

        // Detalhe: no baralho limpo, a manilha do vira 3 é a dama (e não o 4)
        if (modo.isBaralhoLimpo() && c.getLetra() == '3') {
            manilha = 'Q';
        }

    }

    /**
     * Informa se não estamos impedidos de disponibilizar aumento (por conta
     * de ser uma mão de 10/11, ou mesmo um empate acima do limite, ex.:
     * 11x11 no Truco Paulista)
     */
    public boolean isPlacarPermiteAumento() {
        int max = modo.pontuacaoParaMaoDeX();
        return pontosEquipe[0] < max && pontosEquipe[1] < max;
    }

    /**
     * @return true se esta partida não envolve nenhuma comunicação remota
     * (bluetooth, internet, etc.)
     */
    public boolean semJogadoresRemotos() {
        return false;
    }

    /**
     * Aborta a partida por iniciativa daquele jogador
     *
     * @param posicao
     *            posição (1 a 4) do jogador que abandonou o jogo
     */
     public abstract void abandona(int posicao);

    /**
     * Configuração que faz o jogador humano jogar automaticamente
     *
     * @return false (a não ser em PartidaLocal _se_ a opção for habilitada)
     */
    public boolean isJogoAutomatico() {
        return false;
    }

    /**
     * Retorna o nome usado para um determinado valor quando estamos pedindo aumento
     * de aposta. Por exemplo, 3 (no truco paulista) ou 4 (no truco mineiro)
     * se chamam "truco".
     * <p>
     * O principal uso é mapear valores para assets do strings.xml. Por exemplo, as
     * frases usadas no pedido de truco estão em "balao_aumento_truco", para pedir
     * seis estão em "balao_aumento_seis", etc.
     *
     * @param valor aumento solicitado. Pode ser 3, 6, 9, 12 no truco paulista,
     *              ou 4, 6, 10, 12 no truco mineiro.
     * @return "truco", "seis", "nove", etc.
     */
    public String nomeNoTruco(int valor) {
        switch (valor) {
            case 3:
            case 4:
                return "truco";
            case 6:
                return "seis";
            case 9:
                return "nove";
            case 10:
                return "dez";
            case 12:
                return "doze";
        }
        throw new IllegalArgumentException(valor + " não é um valor especial no truco");
    }

}
