package me.chester.minitruco.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.ComponentActivity;

import me.chester.minitruco.R;

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
 * Processa menus e diálogos comuns à tela de título (
 * <code>TituloActivity</code>) e à tela de jogo (<code>TrucoActivity</code>).
 * 
 * 
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
		case R.id.menuitem_ajuda:
			mostraAlertBox(this.getString(R.string.titulo_ajuda),
					this.getString(R.string.texto_ajuda));
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
		new AlertDialog.Builder(this).setTitle(titulo)
				.setMessage(Html.fromHtml(texto))
				.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

}
