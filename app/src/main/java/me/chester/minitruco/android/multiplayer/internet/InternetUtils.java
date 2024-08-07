package me.chester.minitruco.android.multiplayer.internet;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import static me.chester.minitruco.android.PreferenceUtils.getLetraDoModo;
import static me.chester.minitruco.android.PreferenceUtils.getServidor;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Métodos utilitários relacionados ao jogo online.
 */
public class InternetUtils {

    static boolean mostrouConvite = false;

    /**
     * Diz se devemos mostrar um toast convidando a pessoa para jogar na
     * internet, verificando se existe sala no servidor no modo de jogo
     * selecionado que esteja esperando pessoas.
     * <p>
     * O objetivo é reduzir a espera no servidor, mas sem irritar quem
     * não quer jogar online, então a resposta positiva só é retornada
     * uma vez por execução do aplicativo, a não ser que repete seja true.
     *
     *
     * @param context Context usado para ler preferências e mostrar toast
     * @param repete Se true, esquece toasts anteriores (útil para quando
     *               a pessoa seleciona um modo de jogo diferente)
     */
    public static boolean isPromoveJogoInternet(Context context, boolean repete) {
        if (repete) {
            mostrouConvite = false;
        }
        if (mostrouConvite) {
            return false;
        }

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("http://" + getServidor(context) + ":6912/status");
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("AGUARDANDO ") && line.substring(11).contains(getLetraDoModo(context))) {
                    mostrouConvite = true;
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return false;
    }
}
