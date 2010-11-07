package me.chester;

import android.app.Activity;
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
		Log.i("create",""+button);
		button.setOnClickListener(this);
	}

	public void onClick(View v) {
		Jogo j = new JogoLocal(false, false);
		for (int i = 0; i < 4; i++) {
			j.adiciona(new JogadorCPU());
		}
		Thread t = new Thread(j);
		t.start();
	}
}