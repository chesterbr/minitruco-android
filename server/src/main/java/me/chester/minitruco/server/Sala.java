package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import static java.lang.Thread.sleep;
import static me.chester.minitruco.core.JogadorBot.APELIDO_BOT;

import java.util.Date;
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

    private static final String POSICAO_PLACEHOLDER = "$POSICAO";

    /**
     * Salas criadas por usuários (a chave é o código da sala)
     */
    private static final Map<String, Sala> salasPrivadas = new HashMap<>();

    /**
     * Salas públicas que ainda tem espaço para pelo menos um jogaor
     */
    private static final Set<Sala> salasPublicasDisponiveis = new HashSet<>();

    private static final Set<Sala> salasPublicasLotadas = new HashSet<>();

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
            for (Jogador j : sala.jogadores) {
                if (j instanceof JogadorConectado) {
                    sala.remove((JogadorConectado) j);
                }
            }
        }
    }

    /**
     * Código usado para os amigos acharem a sala; null se for uma sala pública
     */
    String codigo;

    final String modo;

    /**
     * Jogadores presentes na sala
     */
    private Jogador[] jogadores = new Jogador[4];

    /**
     * Partida que está rodando nessa sala (se houver)
     */
    private PartidaLocal partida = null;

    /**
     * Cria uma sala .
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
     *
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
     * Adiciona um jogador na sala, garantindo os links bidirecionais e, se necessário,
     * trocando entre a lista das lotadas e das disponíveis.
     *
     * @param j Jogador a adicionar
     * @return true se tudo correr bem, false se a sala estiver lotada ou o
     * jogador já estiver em outra sala
     */
    public boolean adiciona(JogadorConectado j) {
        // Se o jogador já está numa sala, não permite
        if (j.getSala() != null) {
            return false;
        }
        // Procura um lugarzinho na sala. Se achar, adiciona
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] == null) {
                // Garante timestamps diferentes (Date tem resolução de 1ms)
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // Link sala->jogador
                jogadores[i] = j;
                j.timestampSala = new Date();
                // Link jogador->sala
                j.setSala(this);
                atualizaColecoesDeSalas();
                return true;
            }
        }
        return false;
    }

    /**
     * Recupera o gerente da sala, i.e., o <code>JogadorRemoto</code> mais
     * antigo nela
     *
     * @return Jogador mais antigo, ou null se a sala não tiver jogadores
     * remotos
     */
    public Jogador getGerente() {
        JogadorConectado g = null;
        for (int i = 0; i <= 3; i++) {
            if (!(jogadores[i] instanceof JogadorConectado)) {
                continue;
            }
            JogadorConectado j = (JogadorConectado) jogadores[i];
            if (g == null || j.timestampSala.before(g.timestampSala)) {
                g = j;
            }
        }
        return g;
    }

    /**
     * Conta quantas pessoas tem na sala
     *
     * @return Número de Pessoas
     */
    public int getNumPessoas() {
        int numPessoas = 0;
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] != null) {
                numPessoas++;
            }
        }
        return numPessoas;
    }

    /**
     * Remove um jogador da sala.
     * <p>
     * Se houver um partida em andamento, interrompe o mesmo.
     *
     * @param j Jogador a remover
     * @return true se removeu, false se ele não estava lá
     */
    public boolean remove(JogadorConectado j) {
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
                j.querJogar = false;
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
    public void mandaInfoParaTodos() {
        String mensagem = getInfo();
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] instanceof JogadorConectado) {
                ((JogadorConectado) jogadores[i]).println(
                    mensagem.replace(POSICAO_PLACEHOLDER, String.valueOf(i + 1)));
            }
        }
    }

    /**
     * Monta a string de informação da sala.
     * <p>
     * Chamadores devem substituir a string POSICAO_PLACEHOLDER pela posição do jogdaor
     * para o qual a informação será enviada.
     *
     * @return String no formato "I ..." definido em protocolo.txt
     */
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        // I numsala
        // TODO codigo da sala privada
        sb.append("I ");

        // Nomes dos jogadores, separados por pipe (posições vazias são bots -
        // ou, mais precisamente, vão ser bots quando o jogo começar
        for (int i = 0; i <= 3; i++) {
            sb.append(i == 0 ? "" : '|');
            sb.append(jogadores[i] == null ? APELIDO_BOT : jogadores[i].getNome());
        }
        sb.append(' ');

        // Posição do jogador que solicitou a informação
        sb.append(POSICAO_PLACEHOLDER);
        sb.append(' ');

        // Modo de partida
        sb.append(modo);
        sb.append(' ');

        // Status de "quero jogar" dos jogadores (posições vazias são T,
        // indicando que serão preenchidas por robôs caso a partida inicie)
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] instanceof JogadorConectado) {
                if (((JogadorConectado) jogadores[i]).querJogar) {
                    sb.append('T');
                } else {
                    sb.append('F');
                }
            } else {
                sb.append('T');
            }
        }

//        // Posição do gerente
        sb.append(' ');
        sb.append(getPosicao(getGerente()));

        return sb.toString();
    }

    /**
     * Inicia a partida (completando a mesa com bots), caso ela ainda
     * não tenha iniciado e o jogador que solicitou seja o gerente.
     *
     * @param solicitante Jogador que solicitou o início da partida.
     */
    public void iniciaPartida(Jogador solicitante) {
        if (partida != null) {
            return;
        }
        if (solicitante != getGerente()) {
            return;
        }
        // Completa as posições vazias com bots
        int n = 1;
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] == null) {
                jogadores[i] = new JogadorBot();
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
        Thread t = new Thread(partida);
        t.start();
    }

    /**
     * Retorna a posição do jogador na sala
     *
     * @param j Jogador consultado
     * @return posição de 1 a 4, ou 0 se o jogador não está na sala
     */
    public int getPosicao(Jogador j) {
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
    public Jogador getJogador(int i) {
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
     * <p>
     * Não faz nada se o solicitante não for o gerente.
     *
     * @param solicitante Jogador que solicitou a rotação
     * @return true se rotacionou, false se não (porque o solicitante não é o gerente)
     */
    public boolean trocaParceiro(JogadorConectado solicitante) {
        if (solicitante != getGerente()) {
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
     * <p>
     * Não faz nada se o solicitante não for o gerente.
     *
     * @param solicitante Jogador que solicitou a inversão
     * @return true se inverteu, false se não (porque o solicitante não é o gerente)
     */
    public boolean inverteAdversarios(JogadorConectado solicitante) {
        if (solicitante != getGerente()) {
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
