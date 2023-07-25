package me.chester.minitruco.core;

import java.util.Random;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Base para os diversos tipos de jogador que podem participar de um partida.
 * <p>
 * Independente de ser o usuário do celular (JogadorHumano), um jogador virtual
 * (JogadorBot) ou qualquer outro tipo, o jogador recebe notificações do Partida
 * e envia comandos a ele de forma assíncrona.
 */
public abstract class Jogador {

    // Variáveis / Métodos úteis

    protected static final Random random = new Random();

    private int posicao = 0;

    private Carta[] cartas;

    /**
     * Partida da qual este jogador está participando no momento
     */
    protected Partida partida;

    /**
     * Garante que o nome do jogador tenha tamanho e caracteres seguros.
     *
     * @param nome Nome a ser sanitizado
     * @return nome apenas com alfanuméricos acentuados, underscores e <= 25
     *         caracteres; se não houverem caracteres válidos, nome default.
     */
    public static String sanitizaNome(String nome) {
        return (nome == null ? "" : nome)
            .replaceAll("^bot$", "")
            .replaceAll("[-_ \r\n]"," ")
            .trim()
            .replaceAll("[^a-zA-Z0-9À-ÿ ]", "")
            .trim()
            .replaceAll(" +","_")
            .replaceAll("^(.{0,25}).*$", "$1")
            .replaceAll("_$","")
            .replaceAll("^[-_ ]*$", "sem_nome_"+(1 + random.nextInt(999)));
    }

    /**
     * Processa o evento de entrada na partida (guardando a partida)
     */
    public void entrouNoJogo(Jogador j, Partida p) {
        if (j.equals(this)) {
            this.partida = p;
        }
    }

    private String nome = "unnamed";

    /**
     * @return Nome do jogador (em jogos multiplayer)
     */
    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return super.toString()+"["+nome+"]";
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Recupera a posição do jogador na partida
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
     * aceito em um partida)
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

    public void setCartas(Carta[] cartas) {
        this.cartas = cartas;
    }

    public Carta[] getCartas() {
        return cartas;
    }

    /**
     * Informa que uma carta foi jogada na mesa.
     *
     * @param j
     *            Jogador que jogou a carta
     * @param c
     *            Carta jogada
     */
    public abstract void cartaJogada(Jogador j, Carta c);

    /**
     * Informa ao jogador que uma nova mão está iniciando.
     * <p>
     * Ao receber esta mensagem, as cartas do jogador já foram atribuídas via
     * setCartas(), e a carta virada já está disponível via getCarta().
     */
    public abstract void inicioMao(Jogador jogadorQueAbre);

    /**
     * Informa que uma partida começou. Não é obrigatório tratar - até porque o
     * inicioMao será chamado logo em seguida.
     */
    public abstract void inicioPartida(int placarEquipe1, int placarEquipe2);

    /**
     * Informa que é a vez de um jogador jogar.
     *
     * @param j
     *            Jogador cuja vez chegou
     * @param podeFechada
     *            true se o jogador pode jogar carta fechada, false se não pod
     */
    public abstract void vez(Jogador j, boolean podeFechada);

    /**
     * Informa que um jogador pediu aumento de aposta (truco, seis, etc.).
     *
     * @param j
     *            Jogador que pediu o aumento
     * @param valor
     *            Quanto a mão passará a valar se algum adversário aceitar
     * @param rndFrase
     *              Número "grande" que identifica a frase do strings.xml dita
     *              pelo jogador (índice_da_frase = rndFrase % frases.length())
     */
    public abstract void pediuAumentoAposta(Jogador j, int valor, int rndFrase);

    /**
     * Informa que o jogador aceitou um pedido de aumento de aposta.
     *
     * @param j
     *            Jogador que aceitou o aumento
     * @param valor
     *            Quanto a mão está valendo agora
     * @param rndFrase
     *              Número "grande" que identifica a frase do strings.xml dita
     *              pelo jogador (índice_da_frase = rndFrase % frases.length())
     */
    public abstract void aceitouAumentoAposta(Jogador j, int valor, int rndFrase);

    /**
     * Informa que o jogador recusou um pedido de aumento de aposta.
     * <p>
     * Obs.: isso não impede que o outro jogador da dupla aceite o pedido, é
     * apenas para notificação visual. Se o segundo jogdor recusar o pedido, a
     * mensagem de derrota da dupla será enviada logo em seguida.
     *
     * @param j
     *            Jogador que recusou o pedido.
     * @param rndFrase
     *              Número "grande" que identifica a frase do strings.xml dita
     *              pelo jogador (índice_da_frase = rndFrase % frases.length())
     */
    public abstract void recusouAumentoAposta(Jogador j, int rndFrase);

    /**
     * Informa o jogador que a rodada foi fechada
     *
     * @param numRodada
     *            1 a 3, rodada que foi fechada
     * @param resultado
     *            1 se a equipe 1+3 venceu, 2 se a equipe 2+4 venceu, 3 se
     *            empatou
     * @param jogadorQueTorna
     *            jogador que venceu a rodada (e que irá "tornar"), ou null se
     *            for empate
     */
    public abstract void rodadaFechada(int numRodada, int resultado,
            Jogador jogadorQueTorna);

    /**
     * Informa que a mão foi concluída
     *
     * @param pontosEquipe
     *            Array com os pontos da equipe 1 e 2 (índices 0 e 1)
     *
     */
    public abstract void maoFechada(int[] pontosEquipe);

    /**
     * Informa que a partida foi concluído
     *
     * @param numEquipeVencedora
     *            Equipe que ganhou a partida (1 ou 2)
     * @param rndFrase
     *              Número "grande" que identifica a frase do strings.xml dita
     *              pelo jogador (índice_da_frase = rndFrase % frases.length())
     */
    public abstract void jogoFechado(int numEquipeVencedora, int rndFrase);

    /**
     * Informa que um jogador fez sua escolha de topar ou não
     * a mão de 10/11
     *
     * @param j        Jogador que fez a escolha
     * @param aceita   true se o jogador topou, false se recusou
     * @param rndFrase
     *              Número "grande" que identifica a frase do strings.xml dita
     *              pelo jogador (índice_da_frase = rndFrase % frases.length())
     */
    public abstract void decidiuMaoDeX(Jogador j, boolean aceita, int rndFrase);

    /**
     * Informa que o jogador é beneficiário de uma mão de 10/11, e, portanto,
     * deve decidir se aceita ou não esta rodada (se aceitar vale o valor do truco,
     * se ambos recusarem perde o modo normal)
     *
     * @param cartasParceiro Cartas do parceiro
     */
    public abstract void informaMaoDeX(Carta[] cartasParceiro);

    /**
     * Informa que a partida foi abandonado por alguma causa externa (ex.: um
     * jogador desistiu)
     *
     * @param posicao  Posição do jogador que abortou
     * @param rndFrase
     *              Número "grande" que identifica a frase do strings.xml dita
     *              pelo jogador (índice_da_frase = rndFrase % frases.length())
     */
    public abstract void jogoAbortado(int posicao, int rndFrase);

}
