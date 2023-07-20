package me.chester.minitruco.android.multiplayer;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */


import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.android.SalaActivity;
import me.chester.minitruco.android.multiplayer.bluetooth.ClienteBluetoothActivity;
import me.chester.minitruco.core.Baralho;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Modo;
import me.chester.minitruco.core.Partida;
import me.chester.minitruco.core.SituacaoJogo;

/**
 * Representa, no cliente (Bluetooth ou Internet), a <code>PartidaLocal</code> que está executando no servidor.
 * <p>
 * Ela tem duas responsabilidades:
 * <p>
 * - Transformar as notificações específicas do jogo em andamento recebidas
 *   do<code>ClienteActivity</code> em chamadas correspondentes no jogador
 *   (que, se for o <code>JogadorHumano</code>, vai representar na UI)
 * <p>
 * - Transformar as chamadas feitas pelo <code>JogadorHumano</code> (em resposta
 *   a ações executadas na UI) em comandos, enviando-os à <code>ClientActivity</code>
 * <p>
 * Desta forma, ela não se envolve com a conexão em si (que tem tempo de vida
 * maior que um partida) e pode ser usada tanto em Bluetooth quanto em Internet
 * (através do descendente apropriado de <code>ClienteActivity</code>).
 */
public class PartidaRemota extends Partida {

    private final static Logger LOGGER = Logger.getLogger("PartidaRemota");
    private final SalaActivity cliente;
    private JogadorHumano jogadorHumano;
    /**
     * Esse baralho é apenas para sortear cartas quando alguém joga uma fechada
     * (as cartas, mesmo fechadas, têm que ser únicas)
     */
    private Baralho baralho;
    private int numRodadaAtual;

    /**
     * Cria um novo proxy da partida que está rodando num servidor).
     * <p>
     * As posições diferentes da posição do jogador humano (que é o único
     * que realmente precisa ser notificado e escutado, pois é quem reproduz
     * e coleta os eventos de UI) são preenchidas com <code>JogadorDummy</code>.
     *
     * @param cliente
     *          Faz a comunicação com a camada físico (Bluetooth, Internet)
     * @param jogadorHumano
     *          Faz a comunicação com a UI
     * @param posJogador
     *          posição (1 a 4) na qual o jogadorHumano se encontra, do ponto
     *          de vista do servidor (na tela, o jogadorHumano sempre
     *          aparece na posição 1/inferior)
     * @param modoStr
     *          String de 1 caractere recebida pelo servidor que determina
     *          se o truco é paulista, mineiro, etc.
     */
    public PartidaRemota(SalaActivity cliente, JogadorHumano jogadorHumano, int posJogador, String modoStr) {
        super(Modo.fromString(modoStr));
        this.cliente = cliente;

        // Adiciona o jogador na posição correta
        // (preenchendo as outras com dummies)
        for (int i = 1; i <= 4; i++) {
            if (i == posJogador) {
                adiciona(jogadorHumano);
            } else {
                adiciona(new JogadorDummy());
            }
        }
    }

    /**
     * Retorna o jogador humano que está na partida
     *
     * @return objeto que representa o humano
     */
    public JogadorHumano getJogadorHumano() {
        if (jogadorHumano == null)
            for (int i = 1; i <= 4; i++)
                if (getJogador(i) instanceof JogadorHumano)
                    jogadorHumano = (JogadorHumano) getJogador(i);
        return jogadorHumano;

    }

    /**
     * Processa uma notificação "in-game", gerando o evento apropriado no
     * jogador humano
     *
     * @param tipoNotificacao caractere identificador
     * @param parametros      dependem do caractere
     */
    public void processaNotificacao(char tipoNotificacao, String parametros) {

        // Uso geral
        String[] tokens = parametros.split(" ");
        Jogador j;

        switch (tipoNotificacao) {
            case 'P':
                // Início de partida
                pontosEquipe[0] = pontosEquipe[1] = 0;
                getJogadorHumano().maoFechada(pontosEquipe);
                break;
            case 'M':
                // Início da mão
                numRodadaAtual = 1;
                cartasJogadasPorRodada = new Carta[3][4];
                baralho = new Baralho(modo.isBaralhoLimpo());
                // Gera as cartas e notifica
                Carta[] cartas = new Carta[3];
                for (int i = 0; i <= 2; i++) {
                    cartas[i] = new Carta(tokens[i + 1]);
                    baralho.tiraDoBaralho(cartas[i]);
                }
                if (!modo.isManilhaVelha()) {
                    cartaDaMesa = new Carta(tokens[4]);
                    baralho.tiraDoBaralho(cartaDaMesa);
                }
                setManilha(cartaDaMesa);
                getJogadorHumano().setCartas(cartas);
                getJogadorHumano().inicioMao(getJogador(Integer.parseInt(tokens[0])));
                break;
            case 'J':
                // Recupera o jogador que jogou a carta
                int posicao = Integer.parseInt(tokens[0]);
                j = getJogador(posicao);
                // Recupera a carta jogada (isso depende do jogaodr ser local ou
                // remoto, e de a carta ser aberta ou fechada)
                Carta c;
                LOGGER.log(Level.INFO, "posicoes: " + getJogadorHumano().getPosicao()
                        + "," + posicao);
                if (getJogadorHumano().getPosicao() == posicao) {
                    // Recupera a carta jogada pelo humano
                    c = null;
                    Carta[] cartasHumano = getJogadorHumano().getCartas();
                    for (Carta carta : cartasHumano) {
                        if (carta.toString().equals(tokens[1])) {
                            c = carta;
                            break;
                        }
                    }
                    // Se solicitou carta fechada, muda o status
                    if (tokens.length > 2 && tokens[2].equals("T")) {
                        c.setFechada(true);
                    }
                } else {
                    if (tokens.length > 1) {
                        // Cria a carta jogada pelo bot
                        c = new Carta(tokens[1]);
                        baralho.tiraDoBaralho(c);
                    } else {
                        // Carta fechada, cria uma qualquer e seta o status
                        c = baralho.sorteiaCarta();
                        c.setFechada(true);
                    }
                }
                // Guarda a carta no array de cartas jogadas, para consulta
                cartasJogadasPorRodada[numRodadaAtual - 1][posicao - 1] = c;
                // Avisa o jogador humano que a jogada foi feita
                getJogadorHumano().cartaJogada(j, c);
                break;
            case 'V':
                // Informa o jogador humano que é a vez de alguém
                getJogadorHumano().vez(getJogador(Integer.parseInt(tokens[0])),
                        tokens[1].equals("T"));
                break;
            case 'T':
                getJogadorHumano().pediuAumentoAposta(
                        getJogador(Integer.parseInt(tokens[0])),
                        Integer.parseInt(tokens[1]),
                        Integer.parseInt(tokens[2]));
                break;
            case 'D':
                getJogadorHumano().aceitouAumentoAposta(
                        getJogador(Integer.parseInt(tokens[0])),
                        Integer.parseInt(tokens[1]),
                        Integer.parseInt(tokens[2]));
                break;
            case 'C':
                getJogadorHumano().recusouAumentoAposta(
                        getJogador(Integer.parseInt(tokens[0])),
                        Integer.parseInt(tokens[1]));
                break;
            case 'H':
                // Alguém aceitou mão de 10/11, informa
                getJogadorHumano().decidiuMaoDeX(
                        getJogador(Integer.parseInt(tokens[0])),
                        tokens[1].equals("T"),
                        Integer.parseInt(tokens[2]));
                break;
            case 'F':
                // mão de 10/11. Recupera as cartas do parceiro e informa o jogador
                Carta[] cartasMaoDeX = new Carta[3];
                for (int i = 0; i <= 2; i++) {
                    cartasMaoDeX[i] = new Carta(tokens[i]);
                }
                getJogadorHumano().informaMaoDeX(cartasMaoDeX);
                break;
            case 'R':
                // Fim de rodada, recupera o resultado e o jogador que torna
                int resultado = Integer.parseInt(tokens[0]);
                j = getJogador(Integer.parseInt(tokens[1]));
                getJogadorHumano().rodadaFechada(numRodadaAtual, resultado, j);
                numRodadaAtual++;
                break;
            case 'O':
                // Fim de mão, recupera os placares
                pontosEquipe[0] = Integer.parseInt(tokens[0]);
                pontosEquipe[1] = Integer.parseInt(tokens[1]);
                getJogadorHumano().maoFechada(pontosEquipe);
                break;
            case 'G':
                // Fim de partida
                finalizada = true;
                getJogadorHumano().jogoFechado(
                        Integer.parseInt(tokens[0]),
                        Integer.parseInt(tokens[1]));

                break;
            case 'A':
                // Partida abortada por alguém
                getJogadorHumano().jogoAbortado(
                        Integer.parseInt(tokens[0]),
                        Integer.parseInt(tokens[1]));
                break;
        }
    }

    /**
     * Não implementado em partida remota (apenas o JogadorBot usa isso, e ele
     * não participa desses jogos).
     */
    public void atualizaSituacao(SituacaoJogo s, Jogador j) {
        // não faz nada
    }

    public void run() {
        // Notifica o jogador humano que a partida começou
        getJogadorHumano().inicioPartida(0, 0);
    }

    public void jogaCarta(Jogador j, Carta c) {
        cliente.enviaLinha("J " + c + (c.isFechada() ? " T" : ""));
    }

    public void decideMaoDeX(Jogador j, boolean aceita) {
        cliente.enviaLinha("H " + (aceita ? "T" : "F"));
    }

    public void aumentaAposta(Jogador j) {
        if (j.equals(getJogadorHumano()))
            cliente.enviaLinha("T");
    }

    public void respondeAumento(Jogador j, boolean aceitou) {
        if (j.equals(getJogadorHumano())) {
            if (aceitou)
                cliente.enviaLinha("D");
            else
                cliente.enviaLinha("C");
        }
    }

    public void enviaMensagem(Jogador j, String s) {
    }

    @Override
    public void abandona(int posicao) {
        // O if é necessário porque o caller pode nem saber quem pediu pra abandonar
        if (posicao == 1) {
            cliente.enviaLinha("A");
        }
    }

    private boolean humanoGerente;

    public void setHumanoGerente(boolean humanoGerente) {
        this.humanoGerente = humanoGerente;
    }

    @Override
    public boolean isHumanoGerente() {
        if (cliente instanceof ClienteBluetoothActivity) {
            // Cliente Bluetooth nunca é o gerente
            return false;
        } else {
            // O servidor diz se o cliente internet é o gerente
            return humanoGerente;
        }
    }

}
