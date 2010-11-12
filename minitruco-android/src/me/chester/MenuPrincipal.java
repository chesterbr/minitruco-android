package me.chester;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
		Intent intent = new Intent(MenuPrincipal.this, Mesa.class);
		startActivity(intent);

	}
}