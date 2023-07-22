package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import sun.misc.Signal;

public class MiniTrucoServer {

    /**
     * Porta onde o servidor escuta por conexões. Acho que ninguém nunca
     * entendeu porque eu escolhi este número. 🤡
     */
    public static final int PORTA_SERVIDOR = 6912;

    /**
     * Versão do servidor (3.0 é a primeira pós-J2ME)
     */
    public static final String VERSAO_SERVER = "3.0";

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
        // de aceitar conexões (liberando a porta para a nova versão) mas as
        // threads dos jogadores conectados e partidas em andamento continuam
        // rodando.
        Signal.handle(new Signal("USR1"), signal -> {
            ServerLogger.evento("Recebido sinal USR1 - interrompendo threadAceitaConxoes");
            threadAceitaConexoes.interrupt();
        });

        // Quando *todas* as threads encerrarem, loga o evento final
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ServerLogger.evento("Servidor finalizando");
        }));

        // A thread inicial termina por aqui, mas o servidor continua rodandno
        // até que todas as threads se encerrem.
    }

    /**
     * Loop que aceita conexões de clientes e coloca o socket de cada um num
     * objeto JogadorConectado que roda em uma thread separada.
     * <p>
     * Permanece em execução até que a thread onde ele está rodando receba
     * um interrupt.
     */
    public static void aceitaConexoes() {
        ServerLogger.evento("Servidor inicializado e escutando na porta " + PORTA_SERVIDOR);
        try {
            ServerSocket s = new ServerSocket(PORTA_SERVIDOR);
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
                JogadorConectado j = new JogadorConectado(sCliente);
                (new Thread(j)).start();
            }
        } catch (IOException e) {
            ServerLogger.evento(e, "Erro de I/O no ServerSocket, saindo do programa");
        } finally {
            ServerLogger.evento("Servidor não está mais escutando; aguardando finalização dos jogadores conectados.");
        }
    }
}
