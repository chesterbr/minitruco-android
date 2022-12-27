package me.chester.minitruco.core;


import java.util.Random;

/*
 * Copyright © 2005-2012 Carlos Duarte do Nascimento "Chester" <cd@pobox.com>
 * Todos os direitos reservados.
 *
 * A redistribuição e o uso nas formas binária e código fonte, com ou sem
 * modificações, são permitidos contanto que as condições abaixo sejam
 * cumpridas:
 *
 * - Redistribuições do código fonte devem conter o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente.
 *
 * - Redistribuições na forma binária devem reproduzir o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente na documentação e/ou materiais fornecidos com
 *   a distribuição.
 *
 * - Nem o nome do Chester, nem o nome dos contribuidores podem ser
 *   utilizados para endossar ou promover produtos derivados deste
 *   software sem autorização prévia específica por escrito.
 *
 * ESTE SOFTWARE É FORNECIDO PELOS DETENTORES DE DIREITOS AUTORAIS E
 * CONTRIBUIDORES "COMO ESTÁ", ISENTO DE GARANTIAS EXPRESSAS OU TÁCITAS,
 * INCLUINDO, SEM LIMITAÇÃO, QUAISQUER GARANTIAS IMPLÍCITAS DE
 * COMERCIABILIDADE OU DE ADEQUAÇÃO A FINALIDADES ESPECÍFICAS. EM NENHUMA
 * HIPÓTESE OS TITULARES DE DIREITOS AUTORAIS E CONTRIBUIDORES SERÃO
 * RESPONSÁVEIS POR QUAISQUER DANOS, DIRETOS, INDIRETOS, INCIDENTAIS,
 * ESPECIAIS, EXEMPLARES OU CONSEQUENTES, (INCLUINDO, SEM LIMITAÇÃO,
 * FORNECIMENTO DE BENS OU SERVIÇOS SUBSTITUTOS, PERDA DE USO OU DADOS,
 * LUCROS CESSANTES, OU INTERRUPÇÃO DE ATIVIDADES), CAUSADOS POR QUAISQUER
 * MOTIVOS E SOB QUALQUER TEORIA DE RESPONSABILIDADE, SEJA RESPONSABILIDADE
 * CONTRATUAL, RESTRITA, ILÍCITO CIVIL, OU QUALQUER OUTRA, COMO DECORRÊNCIA
 * DE USO DESTE SOFTWARE, MESMO QUE HOUVESSEM SIDO AVISADOS DA
 * POSSIBILIDADE DE TAIS DANOS.
 *
 */

/**
 * Base para os diversos tipos de jogador que podem participar de um jogo.
 * <p>
 * Todo Jogador recebe notificações do jogo (por implementar Interessado)
 * <p>
 * A subclasse determina se o jogador é o usuário do celular, um jogador virtual
 * ou um jogador de outro celular conectado remotamente.
 */
public abstract class Jogador {

    protected static Random random = new Random();
    /**
     * Estratégias suportadas pelos jogadores automático (CPU e Bot)
     */
    static Estrategia[] ESTRATEGIAS = {new EstrategiaGasparotto(),
            new EstrategiaSellani()};
    /**
     * Lista de opções de estratégia para comboboxes (tem os nomes e a última
     * opção é a de sorteio
     */
    static String[] opcoesEstrategia = new String[ESTRATEGIAS.length + 1];

    static {
        // Preenche a lista de opções usando o array de estratégias
        // (o último elemento é preenchido depois de carregar o idioma,
        // pois é a frase "sortear estratégia")
        for (int i = 0; i < ESTRATEGIAS.length; i++)
            opcoesEstrategia[i] = ESTRATEGIAS[i].getNomeEstrategia();
    }

    /**
     * Jogo que está sendo jogado por este jogador
     */
    protected Jogo jogo;
    private int posicao = 0;
    private Carta[] cartas;
    private String nome = "unnamed";

    /**
     * Instancia uma estratégia (para uso em jogadores que precisam disso, como
     * o <code>JogadorBot</code> ou o <code>JogadorCPU</code>).
     *
     * @param nomeEstrategia Nome da estratégia (ex.: "Willian"). Se nenhuma estratégia se
     *                       identificar por aquele nome, sorteia uma aleatória
     * @return nova instância da estratégia
     */
    public static Estrategia criaEstrategiaPeloNome(String nomeEstrategia) {
        // Procura uma classe de estratégia com aquele nome
        int numEstrategia = -1;
        for (int i = 0; i < ESTRATEGIAS.length; i++) {
            if (ESTRATEGIAS[i].getNomeEstrategia().equals(nomeEstrategia)) {
                numEstrategia = i;
                break;
            }
        }
        // Se não houver nenhuma, sorteia
        if (numEstrategia == -1) {
            numEstrategia = random.nextInt(ESTRATEGIAS.length);
        }

        // Cria uma nova instância
        try {
            return ESTRATEGIAS[numEstrategia].getClass()
                    .newInstance();
        } catch (InstantiationException e) {
            throw new Error(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new Error(e.getMessage());
        }
    }

    /**
     * Processa o evento de entrada no jogo (guardando o jogo)
     */
    public void entrouNoJogo(Jogador i, Jogo j) {
        if (i.equals(this)) {
            this.jogo = j;
        }
    }

    /**
     * Nome do jogador (em jogos multiplayer)
     */
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Recupera a posição do jogador no jogo
     *
     * @return número de 1 a 4 (não necessariamente a posição dele na mesa)
     */
    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    /**
     * Recupera a equipe em que este jogador está (assumindo que ele já esteja
     * aceito em um jogo)
     *
     * @return 1 ou 2
     */
    public int getEquipe() {
        return 1 + ((1 + posicao) % 2);
    }

    /**
     * Recupera a posição do parceiro
     *
     * @return número de 1 a 4
     */
    public int getParceiro() {
        return 1 + ((posicao + 1) % 4);
    }

    public int getEquipeAdversaria() {
        return 1 + (posicao % 2);
    }

    public Carta[] getCartas() {
        return cartas;
    }

    public void setCartas(Carta[] cartas) {
        this.cartas = cartas;
    }

    /**
     * Informa que uma carta foi jogada na mesa.
     *
     * @param j Jogador que jogou a carta
     * @param c Carta jogada
     */
    public abstract void cartaJogada(Jogador j, Carta c);

    /**
     * Informa ao jogador que uma nova mão está iniciando.
     * <p>
     * Ao receber esta mensagem, as cartas do jogador já foram atribuídas via
     * setCartas(), e a carta virada já está disponível via getCarta().
     */
    public abstract void inicioMao();

    /**
     * Informa que uma partida começou. Não é obrigatório tratar - até porque o
     * inicioMao será chamado logo em seguida.
     */
    public abstract void inicioPartida(int placarEquipe1, int placarEquipe2);

    /**
     * Informa que é a vez de um jogador jogar.
     *
     * @param j           Jogador cuja vez chegou
     * @param podeFechada true se o jogador pode jogar carta fechada, false se não pod
     */
    public abstract void vez(Jogador j, boolean podeFechada);

    /**
     * Informa que um jogador pediu aumento de aposta (truco, seis, etc.).
     *
     * @param j     Jogador que pediu o aumento
     * @param valor Quanto a mão passará a valar se algum adversário aceitar
     */
    public abstract void pediuAumentoAposta(Jogador j, int valor);

    /**
     * Informa que o jogador aceitou um pedido de aumento de aposta.
     *
     * @param j     Jogador que aceitou o aumento
     * @param valor Quanto a mão está valendo agora
     */
    public abstract void aceitouAumentoAposta(Jogador j, int valor);

    /**
     * Informa que o jogador recusou um pedido de aumento de aposta.
     * <p>
     * Obs.: isso não impede que o outro jogador da dupla aceite o pedido, é
     * apenas para notificação visual. Se o segundo jogdor recusar o pedido, a
     * mensagem de derrota da dupla será enviada logo em seguida.
     *
     * @param j Jogador que recusou o pedido.
     */
    public abstract void recusouAumentoAposta(Jogador j);

    /**
     * Informa o jogador que a rodada foi fechada
     *
     * @param numRodada       1 a 3, rodada que foi fechada
     * @param resultado       1 se a equipe 1+3 venceu, 2 se a equipe 2+4 venceu, 3 se
     *                        empatou
     * @param jogadorQueTorna jogador que venceu a rodada (e que irá "tornar"), ou null se
     *                        for empate
     */
    public abstract void rodadaFechada(int numRodada, int resultado,
                                       Jogador jogadorQueTorna);

    /**
     * Informa que a mão foi concluída
     *
     * @param pontosEquipe Array com os pontos da equipe 1 e 2 (índices 0 e 1)
     */
    public abstract void maoFechada(int[] pontosEquipe);

    /**
     * Informa que o jogo foi concluído
     *
     * @param numEquipeVencedora Equipe que ganhou o jogo (1 ou 2)
     */
    public abstract void jogoFechado(int numEquipeVencedora);

    /**
     * Informa que um jogador fez sua escolha de topar ou não uma rodada quando
     * sua equipe tinha 11 pontos
     *
     * @param j      Jogador que fez a escolha
     * @param aceita true se o jogador topou, false se recusou
     */
    public abstract void decidiuMao11(Jogador j, boolean aceita);

    /**
     * Informa que o jogador é beneficiário de uma "mão de 11", e, portanto,
     * deve decidir se aceita ou não esta rodada (se aceitar vale 3 pontos, se
     * ambos recusarem perde 1)
     *
     * @param cartasParceiro Cartas do parceiro
     * @see Jogo#decideMao11(Jogador, boolean)
     */
    public abstract void informaMao11(Carta[] cartasParceiro);

    /**
     * Informa que o jogo foi abandonado por alguma causa externa (ex.: um
     * jogador desistiu)
     *
     * @param posicao Posição do jogador que abortou
     */
    public abstract void jogoAbortado(int posicao);

}
