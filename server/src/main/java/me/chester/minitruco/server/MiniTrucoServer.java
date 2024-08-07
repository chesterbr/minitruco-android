package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.Signal;

public class MiniTrucoServer {

    static {
        // Configura todos os loggers daqui pra frente com o nosso formatador
        // (incluindo o dessa classe, logo abaixo)
        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (Handler handler : handlers) {
            handler.setFormatter(new LogFormatter());
        }
    }

    private static final Logger LOGGER = Logger.getLogger("MiniTrucoServer");

    /**
     * Porta onde o servidor escuta por conexões. Acho que ninguém nunca
     * entendeu porque eu escolhi este número. 🤡
     */
    public static final int PORTA_SERVIDOR = 6912;

    // Na real é o próximo, mas ok, esse nem tem o botão de internet
    public static final int BUILD_MINIMO_CLIENTE = 30001;

    public static final int MAX_JOGADORES = 1024;

    /**
     * Guarda as threads dos jogadores conectados (para que possamos
     * esperar elas finalizarem quando o servidor for desligado).
     */
    private static Set<Thread> threadsJogadores = new HashSet<>();

    /**
     * Ponto de entrada do servidor. Apenas dispara a thread que aceita
     * conexões e encerra ela quando o launcher.sh solicitar.
     */
    public static void main(String[] args) {
        // Enquanto esta thread estiver rodando, o servidor vai aceitar conexões
        // e colocar o socket de cada cliente numa thread separada
        Thread threadAceitaConexoes = new Thread(() -> aceitaConexoes());
        threadAceitaConexoes.start();

        // Se recebermos um USR1, o .jar foi atualizado. Nesse caso, vamos parar
        // de aceitar conexões (liberando a porta para a nova versão) e
        // avisar as threads de JogadorConectado que o servidor está sendo
        // desligado. Elas vão deixar os jogadores concluirem a partida em
        // que estão (se houver uma) e desconectar/encerrar em seguida.
        Signal.handle(new Signal("USR1"), signal -> {
            LOGGER.info("Recebido sinal USR1 - interrompendo threadAceitaConxoes");
            threadAceitaConexoes.interrupt();
            LOGGER.info("Avisando threads de JogadorConectado que o servidor está sendo desligado");
            JogadorConectado.servidorSendoDesligado = true;
        });

        // Quando *todas* as threads encerrarem, loga o evento final
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("JVM desligando. Tchau e obrigado pelos 🎏.");
        }));

        // O servidor continua rodandno até que a threadAceitaConexoes se
        // encerre (e ela, por sua vez, aguarda que todas as threads de
        // JogadorConectado se encerrem).
    }

    /**
     * Loop que aceita conexões de clientes e coloca o socket de cada um num
     * objeto JogadorConectado que roda em uma thread separada.
     * <p>
     * Permanece em execução até que a thread onde ele está rodando receba
     * um interrupt; a partir daí ele libera a porta e aguarda que as
     * threads de JogadorConectado se encerrem.
     */
    public static void aceitaConexoes() {
        LOGGER.info("Servidor inicializado e escutando na porta " + PORTA_SERVIDOR);
        ServerSocket s = null;
        try {
            s = new ServerSocket(PORTA_SERVIDOR);
            // Vamos checar a cada 1s se recebemos um interrupt
            s.setSoTimeout(1000);
            while (true) {
                Socket sCliente;
                try {
                    sCliente = s.accept();
                } catch (SocketTimeoutException e) {
                    // Era um interrupt, vamos sair do loop
                    if (Thread.interrupted()) {
                        break;
                    }
                    // Era só o timeout, vamos continuar
                    continue;
                }
                if (threadsJogadores.size() >= MAX_JOGADORES) {
                    LOGGER.info("Máximo de jogadores (" + MAX_JOGADORES + ") atingido, recusando conexão");
                    sCliente.getOutputStream().write("! T Servidor lotado, tente novamente mais tarde.\n".getBytes());
                    sCliente.close();
                    continue;
                }
                JogadorConectado j = new JogadorConectado(sCliente);
                j.setOnFinished((t) -> {
                    threadsJogadores.remove(t);
                    LOGGER.info("Jogadores conectados: " + threadsJogadores.size());
                });
                threadsJogadores.add(Thread.ofVirtual().name(j.getNome()).start(j));
                LOGGER.info("Jogadores conectados: " + threadsJogadores.size());
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Erro de I/O no ServerSocket", e);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    LOGGER.log(Level.INFO, "Erro de I/O ao fechar ServerSocket", e);
                }
            }
            LOGGER.info("Servidor não está mais escutando (porta liberada)");
            aguardaThreadsJogadoresFinalizarem();
        }
    }

    // A partida mais lesada do mundo demora 45 minutos pra terminar, 60 tá
    // de bom tamanho pra remover threads mortas e afins
    public static final int MINUTOS_ATE_DESISTIR_DE_ESPERAR = 60;

    /**
     * Aguarda que as threads dos jogadores conectados se encerrem.
     * <p>
     * Normalmente a JVM aguarda todas as threads encerrarem, mas como se
     * trata de virtual threads, é preciso que uma thread "real" chame
     * este método para que os jogadores tenham chance de terminar suas
     * partidas.
     */
    private static void aguardaThreadsJogadoresFinalizarem() {
        int minutosEsperando = 0;
        while (!threadsJogadores.isEmpty()) {
            LOGGER.info("Aguardando " + threadsJogadores.size() + " jogadores (threads) finalizarem. Tempo esperado até agora: " +
                minutosEsperando  + " minutos.");
            try {
                Thread.sleep(60000);
                minutosEsperando += 1;
                if (minutosEsperando >= MINUTOS_ATE_DESISTIR_DE_ESPERAR) {
                    LOGGER.info("Desistindo de esperar após " + minutosEsperando +
                        " minutos, encerrando threads restantes");
                    for (Thread t : threadsJogadores) {
                        LOGGER.info("Encerrando " + t);
                        t.interrupt();
                    }
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Todos os jogadores finalizaram.");
    }

    /**
     * Gera o status do servidor (a ser retornado logo após o OK quando
     * for solicitada via HTTP a URL /status).
     * <p>
     * Quem processa a URL (e usa este método) é o JogadorConectado
     *
     * @return linhas no formato "CHAVE valor", separadas por \n
     */
    public static String status() {
        return "ONLINE " + (threadsJogadores.size() - 1) + "\n" +
               "AGUARDANDO " + Sala.modosAguardandoJogadores();
    }
}
