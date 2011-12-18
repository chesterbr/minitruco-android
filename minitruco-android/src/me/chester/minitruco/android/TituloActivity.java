package me.chester.minitruco.android;

import me.chester.minitruco.R;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
 * Tela inicial do jogo. Permite mudar opções e inciar uma partida (
 * <code>TrucoActivity</code>).
 */
public class TituloActivity extends BaseActivity {

	SharedPreferences preferences;
	private Class<?> classeBluetoothActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.titulo);
		habilitaBluetoothSeExistir();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean mostraInstrucoes = preferences.getBoolean("mostraInstrucoes",
				true);
		if (mostraInstrucoes) {
			Editor e = preferences.edit();
			e.putBoolean("mostraInstrucoes", false);
			e.commit();
			mostraAlertBox(this.getString(R.string.titulo_ajuda),
					this.getString(R.string.texto_ajuda));
		}
	}

	private void habilitaBluetoothSeExistir() {
		try {
			if (BluetoothAdapter.getDefaultAdapter() != null) {
				classeBluetoothActivity = Class
						.forName("me.chester.minitruco.android.bluetooth.BluetoothActivity");
			}
		} catch (Exception e) {
			return;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.titulo, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menuitem_bluetooth).setVisible(
				classeBluetoothActivity != null);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_opcoes:
			Intent settingsActivity = new Intent(getBaseContext(),
					OpcoesActivity.class);
			startActivity(settingsActivity);
			return true;
		case R.id.menuitem_bluetooth:
			Intent bluetoothActivity = new Intent(getBaseContext(),
					classeBluetoothActivity);
			startActivity(bluetoothActivity);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void jogarClickHandler(View v) {
		Intent intent = new Intent(TituloActivity.this, TrucoActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}