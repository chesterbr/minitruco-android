package me.chester.minitruco.android.bluetooth;

import java.util.UUID;

import me.chester.minitruco.R;
import me.chester.minitruco.android.BaseActivity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

/**
 * Tarefas comuns ao cliente e ao servidor Bluetooth: mostrar quem está
 * conectado, garantir que o bt está ligado, iniciar a thread, etc.
 */
public abstract class BluetoothActivity extends BaseActivity implements
		Runnable {

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
	 * Separador de linha recebido
	 */
	public static final int SEPARADOR_REC = '*';

	/**
	 * Identificadores Bluetooth do "serviço miniTruco"
	 */
	public static final String NOME_BT = "miniTruco";
	public static final UUID UUID_BT = UUID
			.fromString("3B175368-ABB4-11DB-A508-C2B155D89593");

	protected BluetoothAdapter btAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

}
