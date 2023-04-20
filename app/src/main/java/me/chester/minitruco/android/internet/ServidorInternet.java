package me.chester.minitruco.android.internet;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

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
                            pedeNome();
                    }

                }
            }
        } catch (IOException e) {
            // TODO: handle errors
            throw new RuntimeException(e);
        }
    }

    private void pedeNome() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Seu nome:");

        final EditText editNome = new EditText(context);
        editNome.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(editNome);

        builder.setPositiveButton("Confirma", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO: tentar setar o nome
                Log.d("Internet", editNome.getText().toString());
            }
        });
        builder.setNegativeButton("Cancela", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // TODO: desconectar
            }
        });

        ContextCompat.getMainExecutor(context).execute(()  -> {
            builder.show();
        });
    }
}
