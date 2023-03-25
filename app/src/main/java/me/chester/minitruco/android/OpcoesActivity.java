package me.chester.minitruco.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import me.chester.minitruco.R;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Activity que permite configurar manilhas, baralho e outras opções
 */
public class OpcoesActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.opcoes);
		// PreferenceManager.getDefaultSharedPreferences(this)
		// .registerOnSharedPreferenceChangeListener(spChanged);
	}

	// Isso e o código comentado acima era pra desligar a manilha velha ao
	// acionar o baralho limpo, mas a UI não responde, então é inútil e
	// perigoso. Se não achar como notificar a UI, removo de vez
	//
	// SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
	// SharedPreferences.OnSharedPreferenceChangeListener() {
	// public void onSharedPreferenceChanged(
	// SharedPreferences sharedPreferences, String key) {
	// if ("baralhoLimpo".equals(key)) {
	// LOGGER.log(Level.INFO, key);
	// Editor e = sharedPreferences.edit();
	// e.putBoolean("manilhaVelha", false);
	// e.commit();
	// }
	// }
	// };

}
