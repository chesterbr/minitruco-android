package me.chester.minitruco.android;

import me.chester.minitruco.R;
import me.chester.minitruco.android.bluetooth.ClienteBluetoothActivity;
import me.chester.minitruco.android.bluetooth.ServidorBluetoothActivity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/*
 * Copyright © 2005-2012 Carlos Duarte do Nascimento "Chester" <cd@pobox.com>
 * Todos os direitos reservados.
 *
 * A redistribuição e o uso nas formas binária e código fonte, com ou sem
 * modificações, são permitidos contanto que as condições abaixo sejam
 * cumpridas:
 * 
 * - Redistribuições do código fonte devem conter o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente.
 * 
 * - Redistribuições na forma binária devem reproduzir o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente na documentação e/ou materiais fornecidos com
 *   a distribuição.
 *   
 * - Nem o nome do Chester, nem o nome dos contribuidores podem ser
 *   utilizados para endossar ou promover produtos derivados deste
 *   software sem autorização prévia específica por escrito.
 * 
 * ESTE SOFTWARE É FORNECIDO PELOS DETENTORES DE DIREITOS AUTORAIS E
 * CONTRIBUIDORES "COMO ESTÁ", ISENTO DE GARANTIAS EXPRESSAS OU TÁCITAS,
 * INCLUINDO, SEM LIMITAÇÃO, QUAISQUER GARANTIAS IMPLÍCITAS DE
 * COMERCIABILIDADE OU DE ADEQUAÇÃO A FINALIDADES ESPECÍFICAS. EM NENHUMA
 * HIPÓTESE OS TITULARES DE DIREITOS AUTORAIS E CONTRIBUIDORES SERÃO
 * RESPONSÁVEIS POR QUAISQUER DANOS, DIRETOS, INDIRETOS, INCIDENTAIS,
 * ESPECIAIS, EXEMPLARES OU CONSEQUENTES, (INCLUINDO, SEM LIMITAÇÃO,
 * FORNECIMENTO DE BENS OU SERVIÇOS SUBSTITUTOS, PERDA DE USO OU DADOS,
 * LUCROS CESSANTES, OU INTERRUPÇÃO DE ATIVIDADES), CAUSADOS POR QUAISQUER
 * MOTIVOS E SOB QUALQUER TEORIA DE RESPONSABILIDADE, SEJA RESPONSABILIDADE
 * CONTRATUAL, RESTRITA, ILÍCITO CIVIL, OU QUALQUER OUTRA, COMO DECORRÊNCIA
 * DE USO DESTE SOFTWARE, MESMO QUE HOUVESSEM SIDO AVISADOS DA
 * POSSIBILIDADE DE TAIS DANOS.
 * 
 */

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
		mostrarMenuBluetooth = BluetoothAdapter.getDefaultAdapter() != null;
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
		switch (item.getItemId()) {
		case R.id.menuitem_opcoes:
			Intent settingsActivity = new Intent(getBaseContext(),
					OpcoesActivity.class);
			startActivity(settingsActivity);
			return true;
		case R.id.menuitem_bluetooth:
			perguntaCriarOuProcurarBluetooth();
			return true;
		default:
			return super.onOptionsItemSelected(item);
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
				.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				}).show();
	}

	public void jogarClickHandler(View v) {
		Intent intent = new Intent(TituloActivity.this, TrucoActivity.class);
		startActivity(intent);
	}

	public void menuButtonClickHandler(View v) {
		openOptionsMenu();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}