package me.chester.minitruco.android;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import me.chester.minitruco.BuildConfig;
import me.chester.minitruco.R;
import me.chester.minitruco.android.multiplayer.bluetooth.ClienteBluetoothActivity;
import me.chester.minitruco.android.multiplayer.bluetooth.ServidorBluetoothActivity;
import me.chester.minitruco.android.multiplayer.internet.ClienteInternetActivity;
import me.chester.minitruco.core.Jogo;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Tela inicial do jogo. Permite mudar opções e inciar uma partida (
 * <code>TrucoActivity</code>).
 */
public class TituloActivity extends BaseActivity {

	SharedPreferences preferences;
	Boolean mostrarMenuBluetooth;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.titulo);

		((TextView) findViewById(R.id.versao_app)).setText("versão " + BuildConfig.VERSION_NAME);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		habilitaBluetoothSeExistir();
		mostraNotificacaoInicial();
		migraOpcoesLegadas();

		// TODO ver se tem um modo mais central de garantir este default (e outros)
		//      (provavelmente quando migrar esse PreferenceManager deprecado
		//      e começar a centralizar as preferencias nesta view)
		selecionaModo(preferences.getString("modo", "P"));
	}

	/**
	 * Até a versão 2.3.9, a pessoa configurava se queria tento mineiro, baralho limpo ou
	 * manilha velha; a 2.4.0 trocou isso pelo modo de jogo. Esse método migra as opções
	 * antigas da melhor forma possível.
	 *
	 * Seria bom deixar ele até que a maior parte das pessoas esteja >= 2.4.0.
	 */
	private void migraOpcoesLegadas() {
		if (preferences.contains("baralhoLimpo")) {
			boolean tentoMineiro = preferences.getBoolean("tentoMineiro", false);
			boolean manilhaVelha = preferences.getBoolean("manilhaVelha", false);
			boolean baralhoLimpo = preferences.getBoolean("baralhoLimpo", false);
			String modo;
			if (tentoMineiro && manilhaVelha) {
				modo = "M";
			} else if (baralhoLimpo) {
				modo = "L";
			} else {
				modo = "P";
			}
			preferences.edit()
					   .putString("modo", modo)
					   .remove("tentoMineiro")
					   .remove("baralhoLimpo")
					   .remove("manilhaVelha")
					   .apply();
		}
	}

	/**
	 * Na primeira vez que a app roda, mostra as instruções.
	 * Na primeira vez em que roda uma nova versão (sem ser a 1a. vez geral),
	 * mostra as novidades desta versão.
	 */
	private void mostraNotificacaoInicial() {
		boolean mostraInstrucoes = preferences.getBoolean("mostraInstrucoes",
			true);
		String versaoQueMostrouNovidades = preferences.getString("versaoQueMostrouNovidades", "");
		String versaoAtual = BuildConfig.VERSION_NAME;

		if (mostraInstrucoes) {
			mostraAlertBox(this.getString(R.string.titulo_ajuda), this.getString(R.string.texto_ajuda));
		} else if (!versaoQueMostrouNovidades.equals(versaoAtual)) {
			mostraAlertBox("Novidades", this.getString(R.string.novidades));
		}

		Editor e = preferences.edit();
		e.putBoolean("mostraInstrucoes", false);
		e.putString("versaoQueMostrouNovidades", versaoAtual);
		e.apply();

	}

	private void habilitaBluetoothSeExistir() {
		mostrarMenuBluetooth = BluetoothAdapter.getDefaultAdapter() != null;
		findViewById(R.id.btnBluetoothContainer).setVisibility(mostrarMenuBluetooth ? View.VISIBLE : View.GONE);
	}

	private void botoesHabilitados(boolean status) {
		findViewById(R.id.btnJogar).setActivated(status);
		findViewById(R.id.btnBluetooth).setActivated(status);
		findViewById(R.id.btnOpcoes).setActivated(status);
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

	private void perguntaCriarOuProcurarBluetooth() {
		botoesHabilitados(false);
		OnClickListener listener = (dialog, which) -> {
			botoesHabilitados(true);
			switch (which) {
			case AlertDialog.BUTTON_NEGATIVE:
				startActivity(new Intent(TituloActivity.this,
						ServidorBluetoothActivity.class));
				break;
			case AlertDialog.BUTTON_POSITIVE:
				startActivity(new Intent(TituloActivity.this,
						ClienteBluetoothActivity.class));
				break;
			}
		};
		new AlertDialog.Builder(this).setTitle("Bluetooth")
				.setMessage("Para jogar via Bluetooth, um celular deve criar o jogo e os outros devem procurá-lo.\n\nCertifique-se de que todos os celulares estejam pareados com o celular que criar o jogo.")
				.setNegativeButton("Criar Jogo", listener)
				.setPositiveButton("Procurar Jogo", listener)
				.show();
	}

	public void jogarClickHandler(View v) {
		Intent intent = new Intent(TituloActivity.this, TrucoActivity.class);
		startActivity(intent);
	}

	public void internetButtonClickHandler(View v) {
		startActivity(new Intent(getBaseContext(), ClienteInternetActivity.class));
	}

	public void bluetoothButtonClickHandler(View v) {
		perguntaCriarOuProcurarBluetooth();
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

	@Override
	public void onBackPressed() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			this.finishAndRemoveTask();
		}
	}

	public void modoButtonClickHandler(View view) {
		selecionaModo((String) view.getTag());
	}

	private void selecionaModo(String modo) {
		((TextView)findViewById(R.id.textViewModo)).setText(Jogo.textoModo(modo));
		preferences.edit().putString("modo", modo).apply();
	}
}
