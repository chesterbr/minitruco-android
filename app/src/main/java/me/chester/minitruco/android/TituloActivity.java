package me.chester.minitruco.android;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import me.chester.minitruco.BuildConfig;
import me.chester.minitruco.R;
import me.chester.minitruco.android.bluetooth.ClienteBluetoothActivity;
import me.chester.minitruco.android.bluetooth.ServidorBluetoothActivity;
import me.chester.minitruco.android.internet.ServidorInternet;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Tela inicial do jogo. Permite mudar opções e inciar uma partida (
 * <code>TrucoActivity</code>).
 */
public class TituloActivity extends BaseActivity {

	public static final String[] BLUETOOTH_PERMISSIONS = new String[] {
		// Permissões que nem deveriam estar aqui, vide callback abaixo
		Manifest.permission.BLUETOOTH,
		Manifest.permission.BLUETOOTH_ADMIN,
		// Permissões runtime
		Manifest.permission.BLUETOOTH_CONNECT,
		Manifest.permission.BLUETOOTH_SCAN
	};
	SharedPreferences preferences;
	Boolean mostrarMenuBluetooth;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.titulo);
		habilitaBluetoothSeExistir();

		// Na primeira vez que a app roda, mostra as instruções
		// Na primeira vez em que roda uma nova versão (sem ser a 1a. vez geral), mostra as novidades
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean mostraInstrucoes = preferences.getBoolean("mostraInstrucoes",
				true);
		String versaoQueMostrouNovidades = preferences.getString("versaoQueMostrouNovidades", "");
		String versaoAtual = BuildConfig.VERSION_NAME;
		Editor e = preferences.edit();
		if (mostraInstrucoes) {
			mostraAlertBox(this.getString(R.string.titulo_ajuda), this.getString(R.string.texto_ajuda));
		} else if (!versaoQueMostrouNovidades.equals(versaoAtual)) {
			mostraAlertBox("Novidades", this.getString(R.string.novidades));
		}
		e.putBoolean("mostraInstrucoes", false);
		e.putString("versaoQueMostrouNovidades", versaoAtual);
		e.apply();
	}

	private void habilitaBluetoothSeExistir() {
		mostrarMenuBluetooth = BluetoothAdapter.getDefaultAdapter() != null;
		findViewById(R.id.btnBluetooth).setVisibility(mostrarMenuBluetooth ? View.VISIBLE : View.GONE);
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
		menu.findItem(R.id.menuitem_bluetooth).setVisible(mostrarMenuBluetooth);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menuitem_opcoes) {
			opcoesButtonClickHandler(null);
			return true;
		} else if (itemId == R.id.menuitem_bluetooth) {
			bluetoothButtonClickHandler(null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean verificaEPedePermissoesDeBluetooth(){
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
			ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			ActivityCompat.requestPermissions(this, BLUETOOTH_PERMISSIONS, 1);
			return false;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1) {
			// Normalmente a gente só pediria as duas permissões novas e checaria se foi dado grant
			// aqui; mas alguns aparelhos (mesmo com Android relativamente novo) simplesmente pulam
			// a fase de perguntar, então eu resolvi pular a checagem aqui, porque das três uma:
			// - Pessoa autorizou; vai dar tudo certo
			// - Pergunta não foi feita; vai dar tudo certo (as permissões básicas aparecem)
			// - Pessoa não autorizou (mesmo depois de clicar o botão Bluetooth): vai crashar,
			//   e eu não me importo. Como você quer jogar no Bluetooth sem Bluetooth? Abre a app
			//   e tenta de novo.
			//			if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				perguntaCriarOuProcurarBluetooth();
			//			}
		}
	}

	private void perguntaCriarOuProcurarBluetooth() {
		OnClickListener listener = new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case AlertDialog.BUTTON_POSITIVE:
					startActivity(new Intent(TituloActivity.this,
							ServidorBluetoothActivity.class));
					break;
				case AlertDialog.BUTTON_NEGATIVE:
					startActivity(new Intent(TituloActivity.this,
							ClienteBluetoothActivity.class));
					break;
				}
			}
		};
		new AlertDialog.Builder(this).setTitle("Bluetooth")
				.setPositiveButton("Criar Jogo", listener)
				.setNegativeButton("Procurar Jogo", listener)
				.show();
	}

	public void jogarClickHandler(View v) {
		Intent intent = new Intent(TituloActivity.this, TrucoActivity.class);
		startActivity(intent);
	}

	public void internetButtonClickHandler(View v) {
		ServidorInternet s = ServidorInternet.INSTANCE.getInstance();
		s.conecta();
	}

	public void bluetoothButtonClickHandler(View v) {
		if (verificaEPedePermissoesDeBluetooth()) {
			perguntaCriarOuProcurarBluetooth();
		}
	}

	public void opcoesButtonClickHandler(View v) {
		Intent settingsActivity = new Intent(getBaseContext(),
				OpcoesActivity.class);
		startActivity(settingsActivity);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
