package me.chester.minitruco.android;

import me.chester.minitruco.R;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
 * Licença, ou (na sua opnião) qualquer versão.
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

public class MenuPrincipalActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.quit:
			finish();
			return true;
		case R.id.sobre:
			alert("Sobre o miniTruco", this.getString(R.string.texto_about));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static Jogo jogo;

	public void jogarClickHandler(View v) {
		jogo = new JogoLocal(false, false);
		jogo.adiciona(new JogadorHumano());
		for (int i = 2; i <= 4; i++) {
			jogo.adiciona(new JogadorCPU());
		}
		Intent intent = new Intent(MenuPrincipalActivity.this,
				PartidaActivity.class);
		startActivity(intent);
	}

	private void alert(String titulo, String texto) {
		new AlertDialog.Builder(this).setTitle(titulo)
				.setMessage(texto).setNeutralButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
	}
}