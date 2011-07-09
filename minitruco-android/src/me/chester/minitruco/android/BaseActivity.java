package me.chester.minitruco.android;

import me.chester.minitruco.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
 * Processa menus e diálogos comuns à tela de título (
 * <code>TituloActivity</code>) e à tela de jogo (<code>TrucoActivity</code>).
 * 
 * @author chester
 * 
 */
public abstract class BaseActivity extends Activity {

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
			mostraAlertBox(this.getString(R.string.titulo_ajuda), this
					.getString(R.string.texto_ajuda));
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
		new AlertDialog.Builder(this).setTitle(titulo).setMessage(
				Html.fromHtml(texto)).setNeutralButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

}
