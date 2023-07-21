package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/**
 * Representa um cliente conectado, dentro ou fora de uma partida.
 * <p>
 * A classe é capaz de processar os comandos do jogador, e, uma vez associada à
 * partida, interagir com ela.
 */
public class JogadorConectado extends Jogador implements Runnable {

    private final Socket cliente;

    /**
     * Informa se o jogador está participando de uma partida
     */
    public boolean jogando = false;
    /**
     * Informa se o jogador autorizou o início da partida na sala
     */
    public boolean querJogar = false;

    private Sala sala;

    /**
     * Buffer de saída do jogador (para onde devemos "printar" os resultados dos
     * comandos)
     */
    private PrintStream out;

    /**
     * Cria um novo jogador
     *
     * @param cliente socket-cliente através do qual o jogador se conectou
     */
    public JogadorConectado(Socket cliente) {
        this.cliente = cliente;
    }

    /**
     * Envia uma linha de texto para o cliente (tipicamente o resultado de um
     * comando, ou um keepalive)
     *
     * @param linha linha de texto a enviar
     */
    public synchronized void println(String linha) {
        out.print(linha);
        out.print("\r\n");
        // Não fazemos log de keepalive
        if (!linha.startsWith("K")) {
            ServerLogger.evento(this, linha);
        }
    }

    /**
     * Aguarda comandos do jogador e os executa
     */
    public void run() {
        ServerLogger.evento(this, "conectou, iniciando thread");

        try {
            cliente.setSoTimeout(0);
            // Prepara os buffers de entrada e saída
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    cliente.getInputStream()));
            out = new PrintStream(cliente.getOutputStream());
            iniciaMonitorDeConexao();
            // Imprime info do servidor (como mensagem de boas-vindas)
            (new ComandoW()).executa(null, this);
            String linha = "";
            while (linha != null) {
                try {
                    linha = in.readLine();
                } catch (IOException e) {
                    // Como o SO_TIMEOUT está em zero, podemos assumir desconexão
                    break;
                }
                if (("K " + keepAlive).equals(linha)) {
                    keepAlive = 0;
                    continue;
                }
                Comando.interpreta(linha, this);
            }
        } catch (IOException e) {
            ServerLogger.evento(e, "Erro de I/O inesperado loop principal do jogador");
        } finally {
            finalizaMonitorDeConexao();
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
                ServerLogger.evento(this, "finalizou thread");
            }
        }

    }

    private long keepAlive;
    private Thread threadMonitorDeConexao;

    /**
     * Configura uma thread para testar a conexão, que envia um keepalive
     * para o cliente a cada 5 segundos.
     * <p>
     * Isso evita um timeout no cliente e permite desbloquear o readLine()
     * sem depender do timeout do socket, que é pouco confiável.
     */
    private void iniciaMonitorDeConexao() {
        Thread threadPrincipal = Thread.currentThread();
        threadMonitorDeConexao = new Thread(() -> {
            ServerLogger.evento(this, "Iniciando monitor de conexão");
            while (true) {
                keepAlive = System.currentTimeMillis();
                println("K " + keepAlive);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (keepAlive != 0) {
                    try {
                        ServerLogger.evento(this, "Keepalive não respondido, fechando socket");
                        cliente.close();
                        threadPrincipal.interrupt();
                    } catch (IOException e) {
                        ServerLogger.evento(e, "Erro de I/O inesperado no monitor de conexão");
                    }
                    break;
                }
            }
            ServerLogger.evento(this, "Monitor de conexão finalizado");
            threadMonitorDeConexao = null;
        });
        threadMonitorDeConexao.start();
    }

    private void finalizaMonitorDeConexao() {
        Thread t = threadMonitorDeConexao;
        if (threadMonitorDeConexao != null) {
            ServerLogger.evento("Interrompendo monitor de conexão");
            t.interrupt();
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
        querJogar = false;
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

    public String getIp() {
        return cliente.getInetAddress().getHostAddress();
    }

}
