package me.chester.minitruco.android;

import me.chester.minitruco.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/*
 * Copyright © 2005-2011 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença.
 *
 * Este programa é distribuido na esperança que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO
 * a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença
 * Pública Geral GNU para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa, se não, escreva para a Fundação do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
	// Log.w("Zerando manilha velha quando acionou baralho limpo", key);
	// Editor e = sharedPreferences.edit();
	// e.putBoolean("manilhaVelha", false);
	// e.commit();
	// }
	// }
	// };

}
