package me.chester.minitruco.android;

import android.app.Activity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2026 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Utilitário para lidar com o modo edge-to-edge obrigatório no Android 15+.
 *
 * A partir do API 35, o Android renderiza o conteúdo por trás das barras do sistema
 * (status bar e navigation bar). Esta classe aplica padding para evitar que
 * elementos da UI fiquem escondidos atrás dessas barras.
 */
public class EdgeToEdgeHelper {

    /**
     * Aplica padding automático para compensar as barras do sistema.
     * Compatível com todas as versões do Android via AndroidX.
     *
     * @param activity a Activity onde aplicar os insets
     */
    public static void aplicaSystemBarInsets(Activity activity) {
        ViewCompat.setOnApplyWindowInsetsListener(
            activity.findViewById(android.R.id.content),
            (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        );
    }
}
