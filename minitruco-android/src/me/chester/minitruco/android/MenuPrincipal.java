package me.chester.minitruco.android;

import me.chester.minitruco.R;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MenuPrincipal extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button button = (Button) findViewById(R.id.button_iniciar);
		System.out.println(button);
		button.setOnClickListener(this);
	}
	
	public static Jogo jogo;

	public void onClick(View v) {

		jogo = new JogoLocal(false, false);
		for (int i = 0; i < 4; i++) {
			jogo.adiciona(new JogadorCPU());
		}
		Intent intent = new Intent(MenuPrincipal.this, Partida.class);
		startActivity(intent);

	}
}