package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MiniTrucoServer {

    public static final int PORTA_SERVIDOR = 6912;

    /**
     * Versão do servidor (3.0 é a primeira pós-J2ME)
     */
    public static final String VERSAO_SERVER = "3.0";

    public static DateFormat dfStartup;

    public static Date dataStartup;

    public static String strDataStartup;

    public static void main(String[] args) {

        try {

            // Guarda a data de início do servidor num formato apropriado para HTTP
            // vide JogadorContectado.serveArquivosApplet

            dfStartup = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",
                    Locale.US);
            dataStartup = new Date();
            strDataStartup = dfStartup.format(dataStartup);

            ServerLogger
                    .evento("Servidor Inicializado, pronto para escutar na porta "
                            + PORTA_SERVIDOR);

            try {
                ServerSocket s = new ServerSocket(PORTA_SERVIDOR);
                while (true) {
                    Socket sCliente = s.accept();
                    JogadorConectado j = new JogadorConectado(sCliente);
                    Thread t = new Thread(j);
                    t.start();
                }
            } catch (IOException e) {
                ServerLogger.evento(e, "Erro de I/O no ServerSocket, saindo do programa");
            }

        } finally {
            ServerLogger.evento("Servidor Finalizado");
        }

    }

}
