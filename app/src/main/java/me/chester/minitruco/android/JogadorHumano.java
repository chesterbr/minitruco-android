package me.chester.minitruco.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.PreferenceManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Partida;
import me.chester.minitruco.core.PartidaLocal;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Jogador que controla o celular.
 * <p>
 * Esta classe trabalha em conjunto com uma <code>TrucoActivity</code> e uma
 * <code>MesaView</code>, que mostram a partida ao usuário, capturam seu input e
 * executam as jogadas.
 */
public class JogadorHumano extends me.chester.minitruco.core.JogadorHumano {

    private final static Logger LOGGER = Logger.getLogger("JogadorHumano");

    private final TrucoActivity activity;

    private final MesaView mesa;

    int valorProximaAposta;

    public JogadorHumano(TrucoActivity activity, MesaView mesa) {
        this.activity = activity;
        this.mesa = mesa;
    }

    @Override
    public void cartaJogada(Jogador j, Carta c) {
        mesa.escondePergunta();
        mesa.setPosicaoVez(0);
        mesa.escondeBotaoAumento();
        mesa.escondeBotaoAbertaFechada();
        mesa.descarta(c, posicaoNaTela(j));
        LOGGER.log(Level.INFO, "Jogador na posicao de tela " + posicaoNaTela(j)
                + " jogou " + c);
    }

    @Override
    public void decidiuMaoDeX(Jogador j, boolean aceita, int rndFrase) {
        if (posicaoNaTela(j) == 3 && aceita) {
            mesa.escondePergunta();
        }
        if (aceita) {
            activity.setValorMao(partida.getModo().valorDaMaoDeX());
        }
        mesa.diz(aceita ? "mao_de_x_sim" : "mao_de_x_nao", posicaoNaTela(j), 1500, rndFrase);
    }

    @Override
    public void entrouNoJogo(Jogador j, Partida p) {

    }

    @Override
    public void informaMaoDeX(Carta[] cartasParceiro) {
        mesa.maoDeX(cartasParceiro);
    }

    @Override
    public void inicioMao(Jogador jogadorQueAbre) {
        valorProximaAposta = 3;
        for (int rodada = 1; rodada <= 3; rodada++) {
            activity.setResultadoRodada(rodada, 0);
        }
        LOGGER.log(Level.INFO, "distribuindo a mão");
        mesa.distribuiMao();
        activity.setValorMao(partida.getModo().valorInicialDaMao());
        mesa.setPosicaoVez(posicaoNaTela(jogadorQueAbre));
        activity.tiraDestaqueDoPlacar();
    }

    @Override
    public void inicioPartida(int placarEquipe1, int placarEquipe2) {
        incrementaEstatistica("statPartidas");
        activity.placar[0] = placarEquipe1;
        activity.placar[1] = placarEquipe2;
        activity.atualizaPlacar(placarEquipe1, placarEquipe2);
    }

    @Override
    public void jogoAbortado(int posicao, int rndFrase) {
        if (posicao != 0 && mesa != null) {
            mesa.diz("abortou", convertePosicaoJogadorParaPosicaoTela(posicao),
                    1000, rndFrase);
            mesa.aguardaFimAnimacoes();
        }
        if (activity != null) {
            activity.jogoAbortado = true;
            activity.finish();
        }
    }

    @Override
    public void jogoFechado(int numEquipeVencedora, int rndFrase) {
        boolean ganhei = (numEquipeVencedora == this.getEquipe());
        incrementaEstatistica(ganhei ? "statVitorias" : "statDerrotas");
        mesa.diz(ganhei ? "vitoria" : "derrota", 1, 1000, rndFrase);
        mesa.aguardaFimAnimacoes();
        activity.jogoFechado(numEquipeVencedora);
    }

    @Override
    public void maoFechada(int[] pontosEquipe) {
        int pontosNos = pontosEquipe[getEquipe() - 1];
        int pontosRivais = pontosEquipe[getEquipeAdversaria() - 1];
        activity.atualizaPlacar(pontosNos, pontosRivais);
        activity.setValorMao(0);
        mesa.escondeBotaoAumento();
        mesa.escondeBotaoAbertaFechada();
        mesa.setPosicaoVez(0);
        mesa.recolheMao();
    }

    @Override
    public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {
        LOGGER.log(Level.INFO, "pedindo para mostrar pergunta aumento");
        mesa.pedeAumento(posicaoNaTela(j), valor, rndFrase);
    }

    @Override
    public void aceitouAumentoAposta(Jogador j, int valor, int rndFrase) {
        if (j.getEquipe() == this.getEquipe()) {
            // Numa partida sem bluetooth/etc, o bot não aumenta, só
            // sinaliza a intenção de aumentar
            if (partida instanceof PartidaLocal && ((PartidaLocal) partida).isIgnoraDecisao(j)) {
                mesa.diz("aumento_quero", posicaoNaTela(j), 1500, rndFrase);
                return;
            }
            // Nós aceitamos um truco, então podemos pedir aumento (se o valor atual ainda permitir)
            valorProximaAposta = partida.getModo().valorSeHouverAumento(valor);
        } else {
            // Eles aceitaram um truco, temos que esperar eles pedirem
            valorProximaAposta = 0;
        }
        mesa.escondePergunta();
        mesa.diz("aumento_sim", posicaoNaTela(j), 1500, rndFrase);
        mesa.aceitouAumentoAposta();
        activity.setValorMao(valor);
    }

    @Override
    public void recusouAumentoAposta(Jogador j, int rndFrase) {
        mesa.diz("aumento_nao", posicaoNaTela(j), 1300, rndFrase);
    }

    @Override
    public void rodadaFechada(int numRodada, int resultado,
            Jogador jogadorQueTorna) {
        if (getEquipe() == 2) {
            // Se o humano nao é equipe 1 e não for empate, troca o resultado
            if (resultado == 1) {
                resultado = 2;
            } else if (resultado == 2) {
                resultado = 1;
            }
        }
        mesa.escondePergunta();
        mesa.setPosicaoVez(0);
        mesa.atualizaResultadoRodada(numRodada, resultado, jogadorQueTorna);
    }

    @Override
    public void vez(Jogador j, boolean podeFechada) {
        LOGGER.log(Level.INFO, "vez do jogador " + posicaoNaTela(j));
        mesa.escondeBotaoAumento();
        mesa.escondeBotaoAbertaFechada();
        if (j.equals(this)) {
            if ((valorProximaAposta > 0) && partida.isPlacarPermiteAumento()) {
                mesa.mostraBotaoAumento(valorProximaAposta);
            }
            if (podeFechada) {
                mesa.mostraBotaoAbertaFechada();
            }
        }
        mesa.vez(j.equals(this));
        mesa.setPosicaoVez(posicaoNaTela(j));
    }

    /**
     * Soma um a uma estatística (no. de partidas jogadas, no. de vitórias,
     * etc.)
     *
     * @param chave
     *            identificador da estatística (ex.: "statPartidas" para número
     *            de partidas jogadas)
     */
    private void incrementaEstatistica(String chave) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(activity);
        int partidas = preferences.getInt(chave, 0);
        Editor editor = preferences.edit();
        editor.putInt(chave, ++partidas);
        editor.apply();
    }

    /**
     * Retorna a posição do jogador na tela.
     * <p>
     * Num partida local, o 1 é o humano *e* a posição inferior da tela. Em jogos
     * remotos, o jogador 1 pode não ser o inferior, e esta função calcula a
     * posição que aquele jogador ocupa na tela sob o ponto de vista local.
     * <p>
     *
     * @return 1 para a posição inferior, 2 para a direita, 3 para cima, 4 para
     *         esquerda
     */
    private int posicaoNaTela(Jogador j) {
        int pos = j.getPosicao() - this.getPosicao() + 1;
        if (pos < 1) {
            pos = pos + 4;
        }
        return pos;
    }

    private int convertePosicaoJogadorParaPosicaoTela(int posicaoJogador) {
        int pos = posicaoJogador - this.getPosicao() + 1;
        if (pos < 1) {
            pos = pos + 4;
        }
        return pos;
    }

}
