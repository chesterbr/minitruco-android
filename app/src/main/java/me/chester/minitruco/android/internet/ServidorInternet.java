package me.chester.minitruco.android.internet;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Singleton responsável por toda a comunicação com o servidor no jogo via internet.
 *
 * A instância pode ser recuperada com ServidorInternet.INSTANCE.getInstance();
 */
public enum ServidorInternet implements Runnable {

    // O enum garante a instância única
    INSTANCE;

    private Socket internetSocket;
    private Thread internetThread;

    private PrintWriter out;
    private BufferedReader in;

    private String info;
    private Context context;

    private ServidorInternet() {
    }

    public ServidorInternet getInstance() {
        return INSTANCE;
    }

    public void conecta(Context context) {
        this.context = context;
        internetThread = new Thread(this);
        internetThread.start();
    }

    @Override
    public void run() {
        try {
            internetSocket = new Socket("10.0.2.2", 6912);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    internetSocket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(
                    internetSocket.getInputStream()));
            // TODO implement some disconnection
            while (true) {
                String line = in.readLine();
                if (line != null && line.length() > 0) {
                    switch (line.charAt(0)) {
                        case 'W':
                            // O servidor manda um W quando conecta
                            // TODO: pedir o nome do jogador e tentar setar no servidor
                            // pedeNome();
                    }

                }
            }
        } catch (IOException e) {
            // TODO: handle errors
            throw new RuntimeException(e);
        }
    }
}
