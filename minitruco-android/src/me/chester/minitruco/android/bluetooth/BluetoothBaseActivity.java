package me.chester.minitruco.android.bluetooth;

import java.util.UUID;

import me.chester.minitruco.R;
import me.chester.minitruco.android.BaseActivity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;

/**
 * Tarefas comuns ao cliente e ao servidor Bluetooth: mostrar quem está
 * conectado, garantir que o bt está ligado, iniciar a thread, etc.
 */
public abstract class BluetoothBaseActivity extends BaseActivity implements
		Runnable {

	/**
	 * Separador de linha recebido
	 */
	public static final int SEPARADOR_REC = '*';

	/**
	 * Separador de linha enviado (tanto no sentido client-server quanto no
	 * server-client).
	 * <p>
	 * É propositalmente um conjunto de SEPARADOR_REC, para garantir que o
	 * recebimento seja detectado (linhas em branco são ignoradas de qualquer
	 * forma).
	 */
	public static final byte[] SEPARADOR_ENV = "**".getBytes();

	/**
	 * Identificadores Bluetooth do "serviço miniTruco"
	 */
	public static final String NOME_BT = "miniTruco";
	public static final UUID UUID_BT = UUID
			.fromString("3B175368-ABB4-11DB-A508-C2B155D89593");

	protected BluetoothAdapter btAdapter;
	protected String[] apelidos = new String[4];
	protected String regras;
	protected Button btnIniciar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		btnIniciar = (Button) findViewById(R.id.btnIniciarBluetooth);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	protected void atualizaDisplay() {
		Message.obtain(handlerAtualizaDisplay).sendToTarget();
	}

	protected abstract int getNumClientes();

	Handler handlerAtualizaDisplay = new Handler() {
		public void handleMessage(Message msg) {
			((TextView) findViewById(R.id.textViewJogador1))
					.setText(apelidos[0]);
			((TextView) findViewById(R.id.textViewJogador2))
					.setText(apelidos[1]);
			((TextView) findViewById(R.id.textViewJogador3))
					.setText(apelidos[2]);
			((TextView) findViewById(R.id.textViewJogador4))
					.setText(apelidos[3]);
			btnIniciar.setEnabled(getNumClientes() > 0);
		}
	};

}
