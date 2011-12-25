package me.chester.minitruco.android.bluetooth;

import java.io.IOException;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ServidorActivity extends BluetoothActivity {

	private BluetoothServerSocket serverSocket;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		encerraAtividade();
	}

	public void run() {
		Log.w("MINITRUCO", "iniciou atividade server");
		try {
			serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(
					NOME_BT, UUID_BT);
		} catch (IOException e) {
			Log.w("MINITRUCO", e);
			// TODO
		}
		BluetoothSocket socket = null;
		while (true) {
			try {
				socket = serverSocket.accept();
				if (socket != null) {
					// TODO Do work to manage the connection (in a separate
					// thread)
					// manageConnectedSocket(socket);
					Log.w("ACHOU", "cliente" + socket.toString());
				}
			} catch (IOException e) {
				Log.w("MINITRUCO", e);
				// TODO fechar todos os clientes
				break;
			}
		}
		Log.w("MINITRUCO", "finalizou atividade server");
	}

	public void encerraAtividade() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			Log.w("MINITRUCO", e);
		}
	}

}
