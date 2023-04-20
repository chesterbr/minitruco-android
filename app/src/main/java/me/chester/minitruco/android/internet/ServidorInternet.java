package me.chester.minitruco.android.internet;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.io.IOException;
import java.net.Socket;

/**
 * Singleton responsável por toda a comunicação com o servidor no jogo via internet.
 */
public enum ServidorInternet implements Runnable {

    // O enum garante a instância única, recuperável com ServidorInternet.INSTANCE.getInstance();
    INSTANCE;

	Socket internetSocket;
    Thread internetThread;

    private String info;

    private ServidorInternet() {
    }

    public ServidorInternet getInstance() {
        return INSTANCE;
    }

    public void conecta() {
        internetThread = new Thread(this);
        internetThread.start();
    }

    @Override
    public void run() {
        try {
            internetSocket = new Socket("localhost", 6912);
		} catch (IOException e) {
            // TODO: handle errors
			throw new RuntimeException(e);
		}
    }
}
