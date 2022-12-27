package me.chester.minitruco.core;

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
 * Base para as estratégias "plugáveis" que um jogador CPU pode utilizar.
 * <p>
 * Uma estratégia é uma classe que implementa os métodos <code>joga</code>,
 * <code>aceitaTruco</code> e <code>aceitaMao11</code>, que são chamados pelo
 * jogo quando for a vez do jogador, quando pedirem aumento para a dupla dele e
 * quando a dupla for decidir uma mão de 11, respectivamente.
 * <p>
 * Eses métodos recebem uma "fotografia" do jogo (SituacaoJogo) no momento em
 * que a ação deles é demandada. Esta fotografia inclui todo o histórico da
 * mão/rodada, placares, etc. Se for desejado guardar estado, o tempo de vida de
 * uma estratégia é o mesmo de <code>Jogo</code>, ou seja, o estado (não-
 * <code>static</code>) persistirá ao longo de uma partida, mas não entre
 * partidas.
 * <p>
 * Para que a estratégia apareça no jogo, adicione uma instância dela ao array
 * ESTRATEGIAS da classe Jogador.
 * <p>
 * Se você criar uma nova estratégia, pode contribui-la para o jogo (desde que
 * concorde em licenciá-la sob os termos acima, baseados na licença "new BSD").
 * Você será creditado e manterá seus direitos autorais. Basta fazer um fork e
 * pull request no github ou entrar em contato com o Chester no cd@pobox.com.
 *
 * @see Jogador#ESTRATEGIAS
 */
public interface Estrategia {

    /**
     * Retorna o nome "copmpleto" da Estrategia
     */
    String getNomeEstrategia();

    /**
     * Retorna informações de copyright e afins
     */
    String getInfoEstrategia();

    /**
     * Executa uma jogada.
     * <p>
     * Observe que, ao pedir aumento, o sistema irá interagir com a outra dupla.
     * Se a partida seguir, o método será chamado novamente para efetivar a real
     * jogada.
     * <p>
     * A estratégia é responsável por checar se o valor da próxima aposta é
     * diferente de 0 e só pedir aumento nesta situação.
     * <p>
     *
     * @param s Situação do jogo no momento
     * @return posição da carta na mão a jogar (em letrasCartasJogador), ou -1
     * para pedir truco
     */
    int joga(SituacaoJogo s);

    /**
     * Decide se aceita um pedido de aumento.
     * <p>
     * O valor do aumento pode ser determinado verificando o valor atual da
     * partida (que ainda não foi aumentado)
     *
     * @param s Situação do jogo no momento
     * @return true para aceitar, false para desistir
     */
    boolean aceitaTruco(SituacaoJogo s);

    /**
     * Decide se aceita iniciar uma "mão de 11"
     *
     * @param cartasParceiro cartas que o parceiro possui
     * @return true para iniciar valendo 3 pontos, false para desistir e perder
     * 1 ponto
     */
    boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s);

    /**
     * Notifica que uma partida está começando.
     */
    void inicioPartida();

    /**
     * Notifica que uma mão está começando
     */
    void inicioMao();

    /**
     * Informa que um jogador pediu aumento de aposta (truco, seis, etc.).
     *
     * @param posJogador Jogador que pediu o aumento
     * @param valor      Quanto a rodada passará a valar se algum adversário aceitar
     */
    void pediuAumentoAposta(int posJogador, int valor);

    /**
     * Informa que o jogador aceitou um pedido de aumento de aposta.
     *
     * @param posJogador Jogador que aceitou o aumento
     * @param valor      Quanto a rodada está valendo agora
     */
    void aceitouAumentoAposta(int posJogador, int valor);

    /**
     * Informa que o jogador recusou um pedido de aumento de aposta.
     * <p>
     * Obs.: isso não impede que o outro jogador da dupla aceite o pedido, é
     * apenas para notificação visual. Se o segundo jogdor recusar o pedido, a
     * mensagem de derrota da dupla será enviada logo em seguida.
     *
     * @param posJogador Jogador que recusou o pedido.
     */
    void recusouAumentoAposta(int posJogador);

}
