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
	}

}
