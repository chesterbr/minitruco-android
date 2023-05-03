package me.chester.minitruco.android.internet;

import android.app.Activity;
import android.os.Bundle;

import me.chester.minitruco.R;

public class ClienteInternetActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.internet_conectando);
    }

    // TODO: fazer isso quando entrar, só que trazendo os métodos pra cá
    //		ServidorInternet s = ServidorInternet.INSTANCE.getInstance();
//		s.conecta(this, preferences.getString("servidor", this.getString(R.string.opcoes_default_servidor)));

    // TODO botão de back tem que
    //  - Sair da sala se estiver jogando
    //  - Fechar a activity se não estiver
    // TODO no destroy da activity, desconectar
}