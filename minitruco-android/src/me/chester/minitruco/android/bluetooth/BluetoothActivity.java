package me.chester.minitruco.android.bluetooth;

import me.chester.minitruco.R;
import me.chester.minitruco.android.BaseActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class BluetoothActivity extends BaseActivity {

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			notificaUI(MSG_SEM_BLUETOOTH);
			return;
		}

		if (!btAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	private static final int REQUEST_ENABLE_BT = 1;
	private static final int MSG_SEM_BLUETOOTH = 1;
	private BluetoothAdapter btAdapter;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SEM_BLUETOOTH:
				mostraAlertBox("Erro ativando Bluetooth",
						"Seu aparelho não suporta Bluetooth (ou ele não foi disponibilizado)");
				finish();
				break;

			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);

	}

	private void notificaUI(int msg) {
		Message.obtain(handler, msg).sendToTarget();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
	}

}
