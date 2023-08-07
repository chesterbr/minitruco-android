package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import me.chester.minitruco.core.Jogador;

/**
 * Efetua o log dos eventos do servidor.
 * <p>
 * Isso veio do servidor antigo, que era um aplicativo Java SE; foi convertido
 * para um wrapper do Java Logger, mas eu totalmente deveria refatorar para
 * usar o Java Logger direto.
 */
public class ServerLogger {

    private final static Logger LOGGER = Logger.getLogger("ServerLogger");

    private static final DateFormat dataLog = new SimpleDateFormat("yyyyMMdd.HHmmss");

    /**
     * Guarda um evento no log.
     * <p>
     * Obs.: Está como synchronized para evitar encavalamento de mensagens. Se
     * isso for ruim em termos de performance, uma idéia seria consolidar os
     * println com um StringBuilder e cuspir de uma vez só.
     *
     * @param j        Jogador com que ocorreu (opcional)
     * @param mensagem Mensagem do evento
     */
    public static synchronized void evento(Jogador j, String mensagem) {
        Sala sala = null;
        StringBuilder sb = new StringBuilder();
        if (j instanceof JogadorConectado) {
            sb.append(j);
        }
        sb.append(mensagem);
        LOGGER.info(sb.toString());
    }

    /**
     * Guarda um erro no log
     *
     * @param e        Exceção associada ao erro (opcional)
     * @param mensagem Mensagem do erro
     */
    public static void evento(Exception e, String mensagem) {
        evento((Jogador) null, mensagem + ". Detalhe do erro:");
        e.printStackTrace();
    }

    /**
     * Gera um evento não ligado a um jogador (startup, shtudown, etc.)
     *
     * @param mensagem Mensagem do evento
     */
    public static void evento(String mensagem) {
        evento((Jogador) null, mensagem);
    }
}
