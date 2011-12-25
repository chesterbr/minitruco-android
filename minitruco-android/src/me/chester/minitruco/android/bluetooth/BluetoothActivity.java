package me.chester.minitruco.android.bluetooth;

import java.util.UUID;

import me.chester.minitruco.R;
import me.chester.minitruco.android.BaseActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

/**
 * Tarefas comuns ao cliente e ao servidor Bluetooth: mostrar quem está
 * conectado, garantir que o bt está ligado, iniciar a thread, etc.
 */
public abstract class BluetoothActivity extends BaseActivity implements
		Runnable {

	private static final int REQUEST_ENABLE_BT = 1;
	protected BluetoothAdapter btAdapter;
	private Thread threadConexao;
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

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		ensureBluetoothAndStartThread();
	}

	private void ensureBluetoothAndStartThread() {
		if (!btAdapter.isEnabled()) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivityForResult(discoverableIntent,REQUEST_ENABLE_BT);

//			Intent enableBtIntent = new Intent(
//					BluetoothAdapter.ACTION_REQUEST_ENABLE);
//			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			threadConexao = new Thread(this);
			threadConexao.start();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_CANCELED) {
				finish();
			} else {
				ensureBluetoothAndStartThread();
			}
		}
	}

}
