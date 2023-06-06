package me.chester.minitruco.android;

import android.app.AlertDialog;
import android.text.Html;

import androidx.appcompat.app.AppCompatActivity;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Processa menus e diálogos comuns à tela de título (
 * <code>TituloActivity</code>) e à tela de jogo (<code>TrucoActivity</code>).
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void mostraAlertBox(String titulo, String texto) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this).setTitle(titulo)
                    .setMessage(Html.fromHtml(texto))
                    .setNeutralButton("Ok", (dialog, which) -> {
                    }).show();
        });
    }

}
