package me.chester.minitruco.server;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;

/**
 * Representa uma sala, onde ocorre um jogo
 *
 * @author Chester
 */
public class Sala {

    /**
     * Salas criadas por usuários (a chave é o código da sala)
     */
    private static Map<String, Sala> salasPrivadas = new HashMap<>();

    /**
     * Salas públicas que ainda tem espaço para pelo menos um jogaor
     */
    private static Set<Sala> salasPublicasDisponiveis = new HashSet<>();

    private static Set<Sala> salasPublicasLotadas = new HashSet<>();

    /**
     * Código usado para os amigos acharem a sala; null se for uma sala pública
     */
    String codigo;

    boolean baralhoLimpo = false;
    boolean manilhaVelha = false;
    boolean tentoMineiro = false;

    /**
     * Jogadores presentes na sala
     */
    private Jogador[] jogadores = new Jogador[4];
    /**
     * Timestamp de entrada de cada jogador (usada para determinar o gerente)
     */

    private Date[] timestamps = new Date[4];
    /**
     * Jogo que está rodando nessa sala (se houver)
     */
    private JogoLocal jogo = null;

    /**
     * Cria uma sala e coloca o jogador nela.
     */
    public Sala(JogadorConectado j, boolean publica, boolean baralhoLimpo, boolean manilhaVelha, boolean tentoMineiro) {
        if (publica) {
            salasPublicasDisponiveis.add(this);
        } else {
            String codigo = UUID.randomUUID().toString().substring(0, 5);
            this.codigo = codigo;
            salasPrivadas.put(codigo, this);
        }
        this.baralhoLimpo = baralhoLimpo;
        this.manilhaVelha = manilhaVelha;
        this.tentoMineiro = tentoMineiro;
        adiciona(j);
    }

    /**
     * Coloca o jogador em uma sala pública que tenha as regras especificadas,
     * criando uma caso estejam todas lotadas
     */
    public static synchronized void colocaEmSalaPublica(JogadorConectado j, boolean baralhoLimpo, boolean manilhaVelha, boolean tentoMineiro) {
        Sala sala = salasPublicasDisponiveis.stream().filter(s ->
            s.baralhoLimpo == baralhoLimpo &&
            s.manilhaVelha == manilhaVelha &&
            s.tentoMineiro == tentoMineiro
        ).findFirst().orElse(
            new Sala(j, false, baralhoLimpo, manilhaVelha, tentoMineiro)
        );
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
                // Link sala->jogador
                jogadores[i] = j;
                timestamps[i] = new Date();
                // Link jogador->sala
                j.setSala(this);
                // Se for uma sala pública, coloca na lista certa (para agilizar busca de salas abertas)
                if (codigo == null) {
                    if (this.getNumPessoas() == 4) {
                        salasPublicasLotadas.add(this);
                        salasPublicasDisponiveis.remove(this);
                    } else {
                        salasPublicasLotadas.remove(this);
                        salasPublicasDisponiveis.add(this);
                    }
                }
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
        int posGerente = -1;
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] instanceof JogadorConectado) {
                if (posGerente == -1
                        || timestamps[i].before(timestamps[posGerente])) {
                    posGerente = i;
                }
            }
        }
        if (posGerente != -1) {
            return jogadores[posGerente];
        } else {
            return null;
        }
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
     * Se houver um jogo em andamento, interrompe o mesmo.
     *
     * @param j Jogador a remover
     * @return true se removeu, false se ele não estava lá
     */
    public boolean remove(JogadorConectado j) {
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] == j) {
                // Finaliza jogo em andamento, se houver.
                if (jogo != null) {
                    jogo.abortaJogo(j.getPosicao());
                    liberaJogo();
                }
                // Desfaz link sala->jogador
                jogadores[i] = null;
                // Desfaz link jogador->sala
                j.setSala(null);
                return true;
            }
        }
        return false;
    }

    /**
     * Recupera o jogo que está rolando na sala (para dar comandos, etc.)
     */
    public Jogo getJogo() {
        return jogo;
    }

    /**
     * Envia uma notificação para todos os jogadores na sala
     *
     * @param mensagem linha de texto a ser enviada
     */
    public void notificaJogadores(String mensagem) {
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] instanceof JogadorConectado) {
                ((JogadorConectado) jogadores[i]).println(mensagem);
            }
        }
    }

    /**
     * Verifica se a mesa está completa, i.e., se a sala tem 4 jogadores
     * dispostos a jogar, e se já não tem um jogo rolando.
     * <p>
     * Se isto acontecer, inicia a partida.
     */
    public void verificaMesaCompleta() {
        // Se estamos em jogo, desencana
        if (jogo != null) {
            return;
        }
        // Todos os remotos conectados têm que querer jogar
        for (int i = 0; i <= 3; i++) {
            if ((jogadores[i] instanceof JogadorConectado)
                    && !((JogadorConectado) jogadores[i]).querJogar) {
                return;
            }
        }
        // Completa as posições vazias com bots
        int n = 1;
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] == null) {
                jogadores[i] = new JogadorCPU("Sortear");
                jogadores[i].setNome("[ROBO_" + (n++) + "]");
            }
        }
        // Cria o jogo com as regras selecionadas, adiciona os jogadores na
        // ordem e inicia
        jogo = new JogoLocal(baralhoLimpo, manilhaVelha, tentoMineiro, false, false);
        for (Jogador j : jogadores) {
            jogo.adiciona(j);
            if (j instanceof JogadorConectado) {
                ((JogadorConectado) j).jogando = true;
            }

        }
        Thread t = new Thread(jogo);
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
     * Desvincula o jogo da sala, eliminado eventuais bots
     */
    public synchronized void liberaJogo() {
        for (int i = 0; i <= 3; i++) {
            if (jogadores[i] instanceof JogadorCPU) {
                jogadores[i] = null;
            }
        }
        this.jogo = null;
    }

    /**
     * Recupera a string de informação da sala.
     *
     * @return String no formato "I sala nome1|nome2|nome3|nome4 vontade posicao
     * regras"
     */
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        // I numsala
        // TODO representar salas sem código (vai ser 'I null' como está)
        sb.append("I " + codigo);
        // Nomes dos jogadores, separados por pipe (posições vazias são strings
        // vazias)
        for (int i = 0; i <= 3; i++) {
            sb.append(i == 0 ? ' ' : '|');
            sb.append(jogadores[i] == null ? "" : jogadores[i].getNome());
        }
        sb.append(' ');
        // Status de "quer jogar" dos jogadores (posições vazias são T,
        // indicando que serão preenchidas por robôs caso o jogo inicie)
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
        sb.append(' ');
        // Posição do gerente
        sb.append(getPosicao(getGerente()));
        sb.append(' ');
        // Regras
        sb.append((baralhoLimpo ? 'T' : 'F'));
        sb.append((manilhaVelha ? 'T' : 'F'));
        return sb.toString();
    }

    /**
     * Troca o parceiro do gerente da sala (fazendo um rodízio de todo mundo
     * menos o gerente)
     */
    public void trocaParceiroDoGerente() {

        Jogador gerente = getGerente();

        // Cria uma lista das posições a trocar, duplicando a primeira no final
        List<Integer> posicoes = new ArrayList<Integer>();
        int posGerente = 0;
        for (int i = 1; i <= 4; i++) {
            if (!gerente.equals(this.getJogador(i))) {
                posicoes.add(i);
            } else {
                posGerente = i;
            }
        }
        posicoes.add(posicoes.get(0));

        // Cria novos arrays de jogadores/timestamps, rotacionando as posições
        // com base na lista acima (jogando o próximo da lista no atual)
        Jogador[] novosJogadores = new Jogador[4];
        Date[] novosTimestamps = new Date[4];
        for (int i = 0; i <= 2; i++) {
            novosJogadores[posicoes.get(i) - 1] = getJogador(posicoes
                    .get(i + 1));
            novosTimestamps[posicoes.get(i) - 1] = timestamps[posicoes
                    .get(i + 1) - 1];
        }

        // Complementa a lista com o gerente e troca a lista atual por essa
        novosJogadores[posGerente - 1] = gerente;
        novosTimestamps[posGerente - 1] = timestamps[posGerente - 1];
        jogadores = novosJogadores;
        timestamps = novosTimestamps;

    }

    public void inverteAdversariosDoGerente() {

        // Acha o gerente
        Jogador gerente = getGerente();
        int posGerente = 0;
        for (int i = 0; i <= 3; i++) {
            if (!gerente.equals(jogadores[i])) {
                posGerente = i;
            }
        }
        // Acha as posições dos adversários
        int posAdv1 = posGerente + 1;
        int posAdv2 = posGerente + 3;
        if (posAdv1 > 4)
            posAdv1 -= 4;
        if (posAdv2 > 4)
            posAdv2 -= 4;

        // Troca jogadores e timestamps
        posAdv1--;
        posAdv2--;

        Jogador tempJogador = jogadores[posAdv1];
        jogadores[posAdv1] = jogadores[posAdv2];
        jogadores[posAdv2] = tempJogador;

        Date tempTimestamp = timestamps[posAdv1];
        timestamps[posAdv1] = timestamps[posAdv2];
        timestamps[posAdv2] = tempTimestamp;

    }

}
