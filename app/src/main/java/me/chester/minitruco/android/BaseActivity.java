package me.chester.minitruco.android;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.ComponentActivity;

import me.chester.minitruco.R;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Processa menus e diálogos comuns à tela de título (
 * <code>TituloActivity</code>) e à tela de jogo (<code>TrucoActivity</code>).
 */
public abstract class BaseActivity extends ComponentActivity {

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuitem_instrucoes:
            mostraAlertBox(this.getString(R.string.titulo_instrucoes),
                    this.getString(R.string.texto_instrucoes));
            return true;
        case R.id.menuitem_sobre:
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(this);
            int partidas = preferences.getInt("statPartidas", 0);
            int vitorias = preferences.getInt("statVitorias", 0);
            int derrotas = preferences.getInt("statDerrotas", 0);
            String versao;
            try {
                versao = getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName;
            } catch (NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            String stats_versao = "Esta é a versão " + versao
                    + " do jogo. Você já iniciou " + partidas
                    + " partidas, ganhou " + vitorias + " e perdeu " + derrotas
                    + ".<br/><br/>";
            mostraAlertBox(this.getString(R.string.titulo_sobre), stats_versao
                    + this.getString(R.string.texto_sobre));
            return true;
        case R.id.menuitem_sair_titulo:
        case R.id.menuitem_sair_truco:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected void mostraAlertBox(String titulo, String texto) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this).setTitle(titulo)
                    .setMessage(Html.fromHtml(texto))
                    .setNeutralButton("Ok", (dialog, which) -> {
                    }).show();
        });
    }

}
