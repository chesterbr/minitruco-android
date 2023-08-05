package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import static me.chester.minitruco.core.TrucoUtils.POSICAO_PLACEHOLDER;
import static me.chester.minitruco.core.TrucoUtils.montaNotificacaoI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.Partida;
import me.chester.minitruco.core.PartidaLocal;

/**
 * Representa uma sala, onde ocorre um partida
 */
public class Sala {

    /**
     * Salas privadas (a chave é o código da sala)
     */
    private static final Map<String, Sala> salasPrivadas = new HashMap<>();

    /**
     * Salas públicas que ainda tem espaço para pelo menos um jogaor
     */
    private static final Set<Sala> salasPublicasDisponiveis = new HashSet<>();

    private static final Set<Sala> salasPublicasLotadas = new HashSet<>();

    /**
     * Limpa todas as salas, interrompendo as partidas e removendo os jogadores.
     * <p>
     * Usada mais para evitar efeitos colaterais entre testes.
     */
    public static void limpaSalas() {
        Set<Sala> todasAsSalas = new HashSet<>();
        todasAsSalas.addAll(salasPrivadas.values());
        todasAsSalas.addAll(salasPublicasDisponiveis);
        todasAsSalas.addAll(salasPublicasLotadas);
        for(Sala sala : todasAsSalas) {
            Partida partida = sala.getPartida();
            if (partida != null) {
                partida.abandona(0);
            }
            for (Jogador j : sala.jogadores.clone()) {
                if (j instanceof JogadorConectado) {
                    sala.remove((JogadorConectado) j);
                }
            }
        }
    }

    /**
     * Código usado para os amigos acharem a sala privada; null se for uma sala pública
     */
    String codigo;

    final String modo;

    /**
     * Jogadores presentes na sala; as posições 1 a 4 são os índices 0 a 3,
     * e a posição 1 (índice 0) é o gerente da sala.
     */
    private Jogador[] jogadores = new Jogador[4];

    /**
     * Partida que está rodando nessa sala (se houver)
     */
    private PartidaLocal partida = null;

    /**
     * Cria uma nova sala, pública ou privada
     */
    public Sala(boolean publica, String modo) {
        if (publica) {
            salasPublicasDisponiveis.add(this);
        } else {
            String codigo = UUID.randomUUID().toString().substring(0, 5);
            this.codigo = codigo;
            salasPrivadas.put(codigo, this);
        }
        this.modo = modo;
    }

    /**
     * Coloca o jogador em uma sala pública que tenha aquele modo de partida
     * criando uma caso estejam todas lotadas
     */
    public static synchronized Sala colocaEmSalaPublica(JogadorConectado j, String modo) {
        Sala sala = salasPublicasDisponiveis.stream().filter(s ->
            s.modo.equals(modo) && (s.getPartida() == null)
        ).findFirst().orElse(null);
        if (sala == null) {
            sala = new Sala(true, modo);
        }
        sala.adiciona(j);
        return sala;
    }

    /**
     * Coloca o jogador em uma sala privada pré-existente
     * @param codigo o código recebido de quem criou a sala
     * @return false caso a sala não tenha sido encontrada ou esteja lotada
     */
    public static synchronized boolean colocaEmSalaPrivada(JogadorConectado j, String codigo) {
        Sala sala = salasPrivadas.get(codigo);
        return (sala != null && sala.adiciona(j));
    }

    /**
     * Adiciona um jogador na primeira posição disponível da sala,
     * garantindo os links bidirecionais e, se necessário,
     * trocando entre a lista das lotadas e das disponíveis.
     *
     * @param j Jogador a adicionar
     * @return true se tudo correr bem, false se a sala estiver lotada ou o
     * jogador já estiver em outra sala
     */
    public synchronized boolean adiciona(JogadorConectado j) {
        // Se o jogador já está numa sala, não permite
        if (j.getSala() != null) {
            return false;
        }
        // Procura um lugarzinho na sala. Se achar, adiciona
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] == null) {
                // Link sala->jogador
                jogadores[i] = j;
                // Link jogador->sala
                j.setSala(this);
                atualizaColecoesDeSalas();
                return true;
            }
        }
        return false;
    }

    /**
     * Recupera o gerente da sala (o jogador que pode trocar/inverter posições
     * e iniciar a partida)
     *
     * @return `JogadorConectado` na posição 1, ou null se a sala estiver vazia
     */
    public synchronized Jogador getGerente() {
        return jogadores[0];
    }

    /**
     * Conta quantas pessoas tem na sala
     *
     * @return Número de Pessoas
     */
    public synchronized int getNumPessoas() {
        int numPessoas = 0;
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] != null) {
                numPessoas++;
            }
        }
        return numPessoas;
    }

    /**
     * Remove um jogador da sala, rompendo os links bidirecionais e atualizando
     * as listas de salas.
     * <p>
     * Se houver um partida em andamento, interrompe a mesma.
     * <p>
     * A sala é rotacionada de forma que o jogador mais antigo (e, portanto,
     * o gerente) esteja na posição 1.
     *
     * @param j Jogador a remover
     * @return true se removeu, false se ele não estava lá
     */
    public synchronized boolean remove(JogadorConectado j) {
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] == j) {
                // Finaliza partida em andamento, se houver.
                if (partida != null) {
                    partida.abandona(j.getPosicao());
                    liberaJogo();
                }
                // Desfaz link sala->jogador
                jogadores[i] = null;
                // Desfaz link jogador->sala
                j.setSala(null);
                // Se a posição 1 ficou vazia (mas ainda tem gente na sala),
                // rotaciona até que alguém a ocupe (será o novo gerente)
                while (getNumPessoas() > 0 && jogadores[0] == null) {
                    for (int x = 1; x <= 3; x++) {
                        jogadores[x - 1] = jogadores[x];
                        jogadores[x] = null;
                    }
                }
                atualizaColecoesDeSalas();
                return true;
            }
        }
        return false;
    }

    /**
     * Mantém as coleções atualizadas quando um jogador entra ou sai da sala
     * <p>
     * Sala com 1-3 jogadores vai para salasPublicasDisponiveis
     * Sala com 4 jogadores vai para salasPublicasLotadas
     * Sala com 0 jogadores não vai para nenhuma coleção (e vai ser garbage collected)
     */
    private void atualizaColecoesDeSalas() {
        if (codigo == null) {
            salasPublicasDisponiveis.remove(this);
            salasPublicasLotadas.remove(this);
            if (this.getNumPessoas() == 4) {
                salasPublicasLotadas.add(this);
            } else if (this.getNumPessoas() > 0) {
                salasPublicasDisponiveis.add(this);
            }
        }
    }

    /**
     * Recupera a partida que está rolando na sala (para dar comandos, etc.)
     */
    public Partida getPartida() {
        return partida;
    }

    /**
     * Manda a notificação de informação da sala ("I ...") para todos os membros.
     */
    public synchronized void mandaInfoParaTodos() {
        String strSala = (codigo == null) ? "PUB" : "PRI-" + codigo;
        String mensagem = montaNotificacaoI(jogadores, modo, strSala);
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] instanceof JogadorConectado) {
                ((JogadorConectado) jogadores[i]).println(
                    mensagem.replace(POSICAO_PLACEHOLDER, String.valueOf(i + 1)));
            }
        }
    }

    /**
     * Inicia a partida (completando a mesa com bots), desde que:
     * <ul>
     *   <li>não haja uma partida em andamento</li>
     *   <li>o solicitante seja o gerente</li>
     *   <li>o servidor não esteja sendo desligado</li>
     * </ul>
     *
     * @param solicitante Jogador que solicitou o início da partida.
     */
    public synchronized void iniciaPartida(Jogador solicitante) {
        if (partida != null) {
            return;
        }
        if (solicitante != getGerente()) {
            return;
        }
        if (JogadorConectado.servidorSendoDesligado) {
            return;
        }
        // Completa as posições vazias com bots
        int n = 1;
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] == null) {
                jogadores[i] = new JogadorBot(Thread.ofVirtual().factory());
            }
        }
        // Cria a partida com as regras selecionadas, adiciona os jogadores na
        // ordem e inicia
        partida = new PartidaLocal(false, false, modo);
        for (Jogador j : jogadores) {
            partida.adiciona(j);
            if (j instanceof JogadorConectado) {
                ((JogadorConectado) j).jogando = true;
            }

        }
        Thread.ofVirtual().start(partida);
    }

    /**
     * Retorna a posição do jogador na sala
     *
     * @param j Jogador consultado
     * @return posição de 1 a 4, ou 0 se o jogador não está na sala
     */
    public synchronized int getPosicao(Jogador j) {
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] == j) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Recupera o jogador em uma determinada posição
     *
     * @param i posição do jogador (de 1 a 4)
     * @return objeto que representa o jogador, ou null se a posição for
     * inválida ou não estiver ocupada
     */
    public synchronized Jogador getJogador(int i) {
        if (i >= 1 && i <= 4)
            return jogadores[i - 1];
        else
            return null;
    }

    /**
     * Desvincula a partida da sala, eliminado eventuais bots
     */
    public synchronized void liberaJogo() {
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] instanceof JogadorBot) {
                jogadores[i] = null;
            }
        }
        this.partida = null;
    }

    /**
     * Rotaciona os outros jogadores, trocando o adversário a cada chamada.
     *
     * @param solicitante Jogador que solicitou a rotação
     * @return true se rotacionou, false se o solicitante não for o gerente, ou
     *         houver jogo em andamento
     */
    public synchronized boolean trocaParceiro(JogadorConectado solicitante) {
        if (solicitante != getGerente() || getPartida() != null) {
            return false;
        }

        int i1 = getPosicao(getGerente());
        int i2 = (i1 + 1) % 4;
        int i3 = (i2 + 1) % 4;

        Jogador temp = jogadores[i2];
        jogadores[i2] = jogadores[i3];
        jogadores[i3] = jogadores[i1];
        jogadores[i1] = temp;

        return true;
    }

    /**
     * Inverte a dupla adversária.
     *
     * @param solicitante Jogador que solicitou a inversão
     * @return true se inverteu, false se o solicitante não for o gerente, ou
     *         houver jogo em andamento
     */
    public synchronized boolean inverteAdversarios(JogadorConectado solicitante) {
        if (solicitante != getGerente() || getPartida() != null) {
            return false;
        }

        int i1 = getPosicao(getGerente());
        int i2 = (i1 + 2) % 4;

        Jogador temp = jogadores[i1];
        jogadores[i1] = jogadores[i2];
        jogadores[i2] = temp;

        return true;
    }
}
