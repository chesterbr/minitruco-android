package me.chester.minitruco.android;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import me.chester.minitruco.R;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Activity que permite configurar manilhas, baralho e outras opções
 */
public class OpcoesActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opcoes);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            usaServidorDefaultSeVazio(key);
        });
    }

    private void usaServidorDefaultSeVazio(String key) {
        if (key.equals("servidor")) {
            String original = getString(R.string.opcoes_default_servidor);
            String atual = preferences.getString("servidor", original);
            if (atual.trim().isEmpty()) {
                preferences.edit().putString("servidor", original).apply();
            } else if (atual.equals("c")) {
                // Esse é o meu computador, troque pelo seu IP e use "c" para
                // rapidamente alternar entre o servidor local e o de produçnao
                preferences.edit().putString("servidor", "192.168.2.10").apply();
            }
        }
    }
}
