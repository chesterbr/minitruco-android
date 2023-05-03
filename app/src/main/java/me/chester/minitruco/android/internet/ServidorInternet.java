package me.chester.minitruco.android.internet;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import static android.provider.Settings.Global.DEVICE_NAME;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import me.chester.minitruco.R;

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

    private String servidor;

    private ServidorInternet() {
    }

    public ServidorInternet getInstance() {
        return INSTANCE;
    }

    public EditText editNome;

    public void conecta(Context context, String servidor) {
        this.context = context;
        this.servidor = servidor;
        editNome = new EditText(context);
        String nome = null;
        // TODO armazenar último nome usado e recuperar aqui
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            nome = Settings.System.getString(context.getContentResolver(), DEVICE_NAME);
        }
        if (nome == null) {
            // Não-documentado e só funciona se tiver Bluetooth, cf https://stackoverflow.com/a/67949517/64635
            nome = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
        }
        if (nome == null) {
            nome = "um nome aleatório";
        }
        editNome.setText(nome);
        internetThread = new Thread(this);
        internetThread.start();
    }

    @Override
    public void run() {
        try {
            internetSocket = new Socket(servidor, 6912);
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


	/**
	 * Envia um comando ao servidor
	 *
	 * @param comando
	 *            texto do comando a enviar
	 */
	public void enviaComando(String comando) {
        out.write(comando);
        out.write('\n');
        out.flush();
        // TODO log
//			Jogo.log(comando);
    }

    private void pedeNome() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
        			.setIcon(R.drawable.icon)
                    .setTitle("Nome")
        			.setMessage("Qual nome você gostaria de usar?")
                    .setView(editNome)
                    .setPositiveButton("Ok", (dialog, which) -> defineNome())
                    .setNegativeButton("Cancela", null) // TODO desconectar
                    .show();
            }
        });
    }

    private void defineNome() {
        enviaComando("N "+editNome.getText().toString());
        // TODO implementar
        Log.d("Internet", editNome.getText().toString());
    }
}
