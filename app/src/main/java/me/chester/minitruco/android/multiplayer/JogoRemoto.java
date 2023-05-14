package me.chester.minitruco.android.multiplayer;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */


import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.core.Baralho;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.SituacaoJogo;

/**
 * Representa, no cliente, o <code>Jogo</code> que está executando no servidor.
 * <p>
 * Ela recebe do ClienteActivity as notificações
 * que ele não entende, repassando ao JogadorHumano (como um JogoLocal faria).
 * <p>
 * Quando a pessoa faz alguma ação que chama métodos métodos de ação (jogaCarta(),
 * aumentaAposta(), etc.), esta classe gera o comando apropriado para o servidor
 * e envia (também através do ClienteActivity).
 * <p>
 * Desta forma, ela não se envolve com a conexão em si (que é reaproveitada entre
 * um jogo e outro), e pode ser usada tanto em Bluetooth quanto em Internet (através
 * do descendente apropriado de ClienteActivity)
 */
public class JogoRemoto extends Jogo {

    private final static Logger LOGGER = Logger.getLogger("JogoRemoto");
    private final ClienteMultiplayer cliente;
    private JogadorHumano jogadorHumano;
    /**
     * Esse baralho é apenas para sortear cartas quando alguém joga uma fechada
     * (as cartas, mesmo fechadas, têm que ser únicas)
     */
    private Baralho baralho;
    private int numRodadaAtual;

    /**
     * Cria um novo proxy de jogo remoto associado a um cliente.
     *
     * Este jogo vai conter o jogadorHumano passado (na posição especificada)
     * e instâncias de JogadorDummy nas outras posições (as ações deles serão todas baseadas
     * em notificações recebidas por esta classe).
     */
    public JogoRemoto(ClienteMultiplayer cliente, JogadorHumano jogadorHumano, int posJogador, String modo) {
        super(modo);
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
     * Retorna o jogador humano que está no jogo
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
                    cartas[i] = new Carta(tokens[i]);
                    baralho.tiraDoBaralho(cartas[i]);
                }
                if (!isManilhaVelha()) {
                    cartaDaMesa = new Carta(tokens[3]);
                    baralho.tiraDoBaralho(cartaDaMesa);
                }
                setManilha(cartaDaMesa);
                getJogadorHumano().setCartas(cartas);
                getJogadorHumano().inicioMao();
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
                    for (int i = 0; i < cartasHumano.length; i++) {
                        if (cartasHumano[i].toString().equals(tokens[1])) {
                            c = cartasHumano[i];
                            break;
                        }
                    }
                    // Se solicitou carta fechada, muda o status
                    if (tokens.length > 2 && tokens[2].equals("T")) {
                        c.setFechada(true);
                    }
                } else {
                    if (tokens.length > 1) {
                        // Cria a carta jogada pela CPU
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
                // Alguém aceitou mão de ferro, informa
                getJogadorHumano().decidiuMaoDeFerro(
                        getJogador(Integer.parseInt(tokens[0])),
                        tokens[1].equals("T"),
                        Integer.parseInt(tokens[2]));
                break;
            case 'F':
                // Mão de ferro. Recupera as cartas do parceiro e informa o jogador
                Carta[] cartasMaoDeFerro = new Carta[3];
                for (int i = 0; i <= 2; i++) {
                    cartasMaoDeFerro[i] = new Carta(tokens[i]);
                }
                getJogadorHumano().informaMaoDeFerro(cartasMaoDeFerro);
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
                // Fim de jogo
                getJogadorHumano().jogoFechado(
                        Integer.parseInt(tokens[0]),
                        Integer.parseInt(tokens[1]));

                break;
            case 'A':
                // Jogo abortado por alguém
                getJogadorHumano().jogoAbortado(
                        Integer.parseInt(tokens[0]),
                        Integer.parseInt(tokens[1]));
                break;
        }
    }

    /**
     * Não implementado em jogo remoto (apenas o JogadorCPU usa isso, e ele
     * não participa desses jogos).
     */
    public void atualizaSituacao(SituacaoJogo s, Jogador j) {
        // não faz nada
    }

    public boolean isManilhaVelha() { return modoStr.equals("M"); }

    public void run() {
        // Notifica o jogador humano que a partida começou
        getJogadorHumano().inicioPartida(0, 0);
    }

    public void jogaCarta(Jogador j, Carta c) {
        cliente.enviaLinha("J " + c + (c.isFechada() ? " T" : ""));
    }

    public void decideMaoDeFerro(Jogador j, boolean aceita) {
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
    public void abortaJogo(int posicao) {
        // TODO eu acho que o if aqui é redundante (no geral, só o Jogador 1
        //.     faria isso mesmo); conferir em outros lugares
        if (posicao == 1) {
            cliente.enviaLinha("A");
        }
    }

}
