package me.chester.minitruco.android;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import me.chester.minitruco.R;

public class PreferenceUtils {
    public static Boolean isServidorLocal(Context context) {
        return getPreferences(context).getBoolean("servidorLocal", false);
    }

    public static Boolean valeUm(Context context) {
        return getPreferences(context).getBoolean("valeUm", false);
    }

    public static String getLetraDoModo(Context context) {
        return getPreferences(context).getString("modo", "P");
    }

    public static String getServidor(Context context) {
        return isServidorLocal(context) ?
            context.getString(R.string.opcoes_default_servidor_local) :
            context.getString(R.string.opcoes_default_servidor);
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
