package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.chester.minitruco.core.Jogador;

/**
 * Efetua o log dos eventos do servidor.
 * <p>
 * A implementação atual coloca estes eventos em stdout num formato padronizado,
 * o que funciona bem para quem tem o grep à mão. Implementações futuras podem
 * fazer logs por sala, por jogador, efetuar alertas baseados em log, whatever.
 *
 * 
 */
public class ServerLogger {

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
        // Formato:
        // data thread [numsala|NA] jogador[@ip] mensagem
        System.out.print(dataLog.format(new Date()));
        System.out.print(' ');
        System.out.print(Thread.currentThread().getName());
        System.out.print(' ');
        Sala s = null;
        if (j instanceof JogadorConectado) {
            s = ((JogadorConectado) j).getSala();
        }
        if (s != null) {
            // TODO representar salas sem código
            System.out.print(s.codigo);
            System.out.print(' ');
        } else {
            System.out.print("[sem_sala] ");
        }
        if (j != null) {
            System.out.print(!j.getNome().equals("unnamed") ? j.getNome() : "[sem_nome]");
            if (j instanceof JogadorConectado) {
                System.out.print('@');
                System.out.print(((JogadorConectado) j).getIp());
            }
            System.out.print(' ');
        } else {
            System.out.print("[sem_jogador] ");
        }
        System.out.println(mensagem);
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
