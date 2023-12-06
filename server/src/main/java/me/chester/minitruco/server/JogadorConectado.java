package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/**
 * Representa um cliente conectado, dentro ou fora de uma partida.
 * <p>
 * A classe é capaz de processar os comandos do jogador, e, uma vez associada à
 * partida, interagir com ela.
 */
public class JogadorConectado extends Jogador implements Runnable {

    private final static Logger LOGGER = Logger.getLogger("JogadorConectado");

    public static final int TEMPO_KEEPALIVE = 10000;
    private final Socket cliente;

    /**
     * Informa se o jogador está participando de uma partida
     */
    public boolean jogando = false;

    /**
     * Sala em que o jogador se encontra (null se nenhuma)
     */
    private Sala sala;

    /**
     * Buffer de saída do jogador (para onde devemos "printar" os resultados dos
     * comandos)
     */
    private PrintStream out;

    /**
     * Se true, notifica jogadores em partida de tempos em tempos que o servidor
     * será desligado, e desconecta jogadores fora de partida.
     */
    public static boolean servidorSendoDesligado = false;

    /**
     * Cria um novo jogador
     *
     * @param cliente socket-cliente através do qual o jogador se conectou
     */
    public JogadorConectado(Socket cliente) {
        this.cliente = cliente;
        // atribui um nome padrão, sem gerar log de troca de nome
        super.setNome(Jogador.sanitizaNome(""));
    }

    @FunctionalInterface
    public interface OnFinishCallback {
        void execute(Thread thread);
    }

    private OnFinishCallback onFinishCallback;

    public void setOnFinished(OnFinishCallback onFinishCallback) {
        this.onFinishCallback = onFinishCallback;
    }

    /**
     * Envia uma linha de texto para o cliente (tipicamente o resultado de um
     * comando, ou um keepalive)
     *
     * @param linha linha de texto a enviar
     */
    public void println(String linha) {
        out.println(linha);
        out.flush();
        // Não fazemos log de keepalive
        if (!linha.startsWith("K")) {
            LOGGER.info(this + " recebeu " + linha);
        }
    }

    /**
     * Aguarda comandos do jogador e os executa
     */
    public void run() {
        LOGGER.info(this + " conectou, iniciando thread");

        try {
            cliente.setSoTimeout(0);
            // Prepara os buffers de entrada e saída
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    cliente.getInputStream()));
            out = new PrintStream(cliente.getOutputStream());
            iniciaThreadAuxiliar();
            String linha = "";
            while (linha != null) {
                try {
                    linha = in.readLine();
                } catch (IOException e) {
                    // Como o SO_TIMEOUT está em zero, podemos assumir desconexão
                    break;
                }
                if (linha != null && linha.matches("(GET|HEAD) /status HTTP/.*")) {
                    // Desabilita o monitor de conexão (se o GET chegar antes de ele
                    // começar, senão também paciência)
                    threadMonitorDeConexao.interrupt();
                    LOGGER.info("Status do servidor solicitado (HTTP); enviando e desconectando");
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/plain");
                    out.println();
                    if (linha.startsWith("HEAD")) {
                        out.flush();
                        return;
                    }
                    out.println("OK");
                    out.flush();

                    out.println(MiniTrucoServer.status());
                    out.println();
                    out.flush();
                    return;
                }
                if (("K " + keepAlive).equals(linha)) {
                    keepAlive = 0;
                    continue;
                } else {
                    LOGGER.info(this + " mandou " + linha);
                }
                Comando.interpreta(linha, this);
            }
        } catch (IOException e) {
            LOGGER.info(this + " Erro de I/O inesperado loop principal do jogador");
        } finally {
            Sala s = getSala();
            // Se houver um jogo em andamento (e ainda tivermos comunicação), encerra
            Comando.interpreta("A", this);
            if (s != null) {
                // Dá um tempo para o cliente receber o comando A e mostrar o balão de "adeus"
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // Garante que o jogador saiu da sala, e os clientes vão ser notificados
                s.remove(this);
                s.mandaInfoParaTodos();
                LOGGER.info(this + " finalizou thread");
            }
            finalizaThreadAuxiliar();
            if (onFinishCallback != null) {
                onFinishCallback.execute(Thread.currentThread());
            }
        }

    }

    private long keepAlive;
    private Thread threadMonitorDeConexao;

    /**
     * Configura uma thread que executa tarefas do jogador, tais como:
     * <ul>
     *     <li>Enviar um keepalive para o cliente a cada 5s (evitando
     *         timeout e bloqueio do readLine())</li>
     *     <li>Se o servidor estiver sendo desligado (porque uma versão
     *         atualizada subiu), desconecta jogadores fora de partida
     *         e mantém os outros informados enquanto durar a partida.</li>
     * </ul>
     */
    private void iniciaThreadAuxiliar() {
        Thread threadPrincipal = Thread.currentThread();
        threadMonitorDeConexao = Thread.ofVirtual().start(() -> {
            LOGGER.info(this + " Aguardando para iniciar monitor de conexão");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }
            LOGGER.info(this + " Iniciando monitor de conexão");
            boolean avisouQueVaiDesconectarNoFimDaPartida = false;
            while (true) {
                //// Checagem de servidor dando shutdown
                if (servidorSendoDesligado) {
                    if (!jogando) {
                        println("! T Servidor atualizado. Conecte novamente para jogar.");
                        desconecta();
                        threadPrincipal.interrupt();
                        break;
                    } else if (!avisouQueVaiDesconectarNoFimDaPartida) {
                        println("! T Servidor atualizado. Finalize esta partida e conecte novamente.");
                        avisouQueVaiDesconectarNoFimDaPartida = true;
                    }
                }

                //// Checagem de keepalive
                keepAlive = System.currentTimeMillis();
                println("K " + keepAlive);
                try {
                    Thread.sleep(TEMPO_KEEPALIVE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (keepAlive != 0) {
                    LOGGER.info(this + " Keepalive não respondido, fechando socket");
                    desconecta();
                    threadPrincipal.interrupt();
                    break;
                }
            }
            LOGGER.info(this + " Monitor de conexão finalizado");
            threadMonitorDeConexao = null;
        });
    }

    private void finalizaThreadAuxiliar() {
        if (threadMonitorDeConexao != null) {
            LOGGER.info(this + " Interrompendo monitor de conexão");
            threadMonitorDeConexao.interrupt();
        }
    }

    public void desconecta() {
        try {
            cliente.close();
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Erro de I/O inesperado ao fechar socket", e);
        }
    }

    @Override
    public void cartaJogada(Jogador j, Carta c) {
        String param;
        if (c.isFechada()) {
            if (j.equals(this)) {
                param = " " + c + " T";
            } else {
                param = "";
            }
        } else {
            param = " " + c;
        }
        println("J " + j.getPosicao() + param);
    }

    @Override
    public void inicioMao(Jogador jogadorQueAbre) {
        StringBuilder comando = new StringBuilder("M");
        comando.append(" ").append(jogadorQueAbre.getPosicao());
        for (int i = 0; i <= 2; i++)
            comando.append(" ").append(getCartas()[i]);
        if (!partida.getModo().isManilhaVelha()) {
            comando.append(" ").append(partida.cartaDaMesa);
        }
        println(comando.toString());
    }

    @Override
    public void inicioPartida(int placarEquipe1, int placarEquipe2) {
        // TODO comparar com bluetooth (todos os eventos, alias)
        println("P " + getPosicao());
    }

    @Override
    public void vez(Jogador j, boolean podeFechada) {
        println("V " + j.getPosicao() + ' ' + (podeFechada ? 'T' : 'F'));
    }

    @Override
    public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {
        println("T " + j.getPosicao() + ' ' + valor + ' ' + rndFrase);
    }

    @Override
    public void aceitouAumentoAposta(Jogador j, int valor, int rndFrase) {
        println("D " + j.getPosicao() + ' ' + valor + ' ' + rndFrase);
    }

    @Override
    public void recusouAumentoAposta(Jogador j, int rndFrase) {
        println("C " + j.getPosicao() + ' ' + rndFrase);
    }

    @Override
    public void rodadaFechada(int numRodada, int resultado,
                              Jogador jogadorQueTorna) {
        println("R " + resultado + ' ' + jogadorQueTorna.getPosicao());
    }

    @Override
    public void maoFechada(int[] pontosEquipe) {
        println("O " + pontosEquipe[0] + ' ' + pontosEquipe[1]);
    }

    @Override
    public void jogoFechado(int numEquipeVencedora, int rndFrase) {
        desvinculaJogo();
        println("G " + numEquipeVencedora + " " + rndFrase);
    }

    @Override
    public void decidiuMaoDeX(Jogador j, boolean aceita, int rndFrase) {
        println("H " + j.getPosicao() + (aceita ? " T" : " F") + ' ' + rndFrase);
    }

    @Override
    public void informaMaoDeX(Carta[] cartasParceiro) {
        StringBuilder sbComando = new StringBuilder("F ");
        for (int i = 0; i <= 2; i++) {
            sbComando.append(cartasParceiro[i]);
            if (i != 2)
                sbComando.append(' ');
        }
        println(sbComando.toString());
    }

    @Override
    public void jogoAbortado(int posicao, int rndFrase) {
        desvinculaJogo();
        println("A " + posicao + ' ' + rndFrase);
    }

    /**
     * Desvincula a partida do jogador, e, se necessário, da sala
     */
    private synchronized void desvinculaJogo() {
        jogando = false;
        Sala s = getSala();
        if (s != null)
            s.liberaJogo();
    }

    /**
     * Recupera a sala em que o jogado restá
     *
     * @return objeto representando a sala, ou null se estiver fora de uma sala
     */
    public Sala getSala() {
        return sala;
    }

    /**
     * Associa o jogador com a sala. NÃO deve ser usado diretamente (ao invés disso,
     * use Sala.adiciona() e Sala.remove())
     */
    public void setSala(Sala sala) {
        this.sala = sala;
    }

    private String ip = null;

    public String getIp() {
        if (ip == null && cliente != null) {
            InetAddress inetAddress = cliente.getInetAddress();
            if (inetAddress != null) {
                ip = inetAddress.getHostAddress();
            }
        }
        return ip;
    }

    /**
     * @return Jogador com nome, info de IP e sala (para logs)
     */
    @Override
    public String toString() {
        Sala s = sala; // caso o jogador saia da sala entre o null check e o Sala.toString
        return new StringBuilder()
            .append('[')
            .append(getNome())
            .append('@')
            .append(getIp())
            .append(s == null ? "" : " em ")
            .append(s == null ? "" : s)
            .append(']')
            .toString();
    }

    @Override
    public void setNome(String nome) {
        LOGGER.info(this + " mudou nome para " + nome);
        super.setNome(nome);
    }

}
