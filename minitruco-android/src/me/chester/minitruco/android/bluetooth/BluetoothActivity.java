package me.chester.minitruco.android.bluetooth;

import java.util.UUID;

import me.chester.minitruco.R;
import me.chester.minitruco.android.BaseActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * Tarefas comuns ao cliente e ao servidor Bluetooth: mostrar quem está
 * conectado, garantir que o bt está ligado, iniciar a thread, etc.
 */
public abstract class BluetoothActivity extends BaseActivity implements
		Runnable {

	protected static final byte[] SEPARADOR_ENV = "**".getBytes();
	protected BluetoothAdapter btAdapter;
	/**
	 * Identificadores Bluetooth do "serviço miniTruco"
	 */
	public static final String NOME_BT = "miniTruco";
	public static final UUID UUID_BT = UUID
			.fromString("3B175368-ABB4-11DB-A508-C2B155D89593");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

}
