package me.chester.minitruco.android.bluetooth;

import java.io.IOException;
import java.io.OutputStream;

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.android.TrucoActivity;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ServidorBluetoothActivity extends BluetoothBaseActivity {

	private static final char STATUS_AGUARDANDO = 'A';
	private static final char STATUS_LOTADO = 'L';
	private static final char STATUS_EM_JOGO = 'J';
	private static final char STATUS_BLUETOOTH_ENCERRADO = 'X';
	private static final int REQUEST_ENABLE_DISCOVERY = 1;
	private static final String[] APELIDOS_CPU = { "CPU1", "CPU2", "CPU3" };

	private static ServidorBluetoothActivity currentInstance;

	private char status;
	private Thread threadMonitoraClientes;
	private Jogo jogo;
	private boolean aguardandoDiscoverable = false;
	private Thread threadAguardaConexoes;
	private BluetoothServerSocket serverSocket;
	private BluetoothSocket[] connClientes = new BluetoothSocket[3];
	private OutputStream[] outClientes = new OutputStream[3];

	private BroadcastReceiver receiverMantemDiscoverable = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			pedePraHabilitarDiscoverableSePreciso();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentInstance = this;
		// TODO Usa as regras escolhidas pelo usuário
		this.regras = "FF";
		layoutIniciar.setVisibility(View.VISIBLE);
		btnIniciar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				status = STATUS_EM_JOGO;
				iniciaPartida();
			}
		});
		registerReceiver(receiverMantemDiscoverable, new IntentFilter(
				BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		pedePraHabilitarDiscoverableSePreciso();
		if (!aguardandoDiscoverable) {
			iniciaThreads();
		}
	}

	private void pedePraHabilitarDiscoverableSePreciso() {
		if (aguardandoDiscoverable
				|| status == STATUS_EM_JOGO
				|| btAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			return;
		}
		aguardandoDiscoverable = true;
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_DISCOVERY) {
			aguardandoDiscoverable = false;
			if (resultCode == RESULT_CANCELED) {
				// Sem discoverable, sem servidor
				finish();
			} else {
				iniciaThreads();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiverMantemDiscoverable);
		encerraConexoes();
	}

	public void run() {
		Log.w("MINITRUCO", "iniciou atividade server");
		inicializaDisplay();
		atualizaDisplay();
		try {
			serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(
					NOME_BT, UUID_BT);
		} catch (IOException e) {
			Log.w("MINITRUCO", e);
			return;
		}
		while (status != STATUS_BLUETOOTH_ENCERRADO) {
			while (status == STATUS_EM_JOGO) {
				sleep(500);
				continue;
			}
			atualizaDisplay();
			atualizaClientes();
			if (status == STATUS_LOTADO) {
				sleep(1000);
				continue;
			}
			// Se chegamos aqui, estamos fora de jogo e com vagas
			try {
				BluetoothSocket socket = serverSocket.accept();
				if (socket != null) {
					encaixaEmUmSlot(socket);
				}
			} catch (IOException e) {
				Log.w("MINITRUCO", e);
			}
			if (isFinishing()) {
				status = STATUS_BLUETOOTH_ENCERRADO;
			}
		}
		encerraConexoes();
		Log.w("MINITRUCO", "finalizou atividade server");
	}

	private void encerraConexoes() {
		status = STATUS_BLUETOOTH_ENCERRADO;
		for (int slot = 0; slot <= 2; slot++) {
			desconecta(slot);
		}
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				Log.w("MINITRUCO", e);
			}
		}

	}

	private void iniciaThreads() {
		if (threadAguardaConexoes == null) {
			threadAguardaConexoes = new Thread(this);
			threadAguardaConexoes.start();
		}
		if (threadMonitoraClientes == null) {
			threadMonitoraClientes = new Thread() {
				public void run() {
					// Executa enquanto o servidor não for encerrado
					while (status != STATUS_BLUETOOTH_ENCERRADO) {
						// Envia um comando vazio (apenas para testar a conexão,
						// processar qualquer desconexão que tenha ocorrido)
						for (int i = 0; i <= 2; i++) {
							enviaMensagem(i, "");
						}
						try {
							sleep(2000);
						} catch (InterruptedException e) {
							// não precisa tratar
						}
					}
				}
			};
			threadMonitoraClientes.start();
		}
	}

	private synchronized void encaixaEmUmSlot(BluetoothSocket socket)
			throws IOException {
		// synchronized para evitar encaixes enquanto um jogador é trocado de
		// lugar (que ainda vai ser implementado)
		for (int i = 0; i <= 2; i++) {
			if (connClientes[i] == null) {
				connClientes[i] = socket;
				outClientes[i] = socket.getOutputStream();
				apelidos[i + 1] = socket.getRemoteDevice().getName()
						.replace(' ', '_');
				status = i == 2 ? STATUS_LOTADO : STATUS_AGUARDANDO;
				return;
			}
		}
	}

	private void inicializaDisplay() {
		apelidos[0] = btAdapter.getName();
		for (int i = 0; i <= 2; i++) {
			apelidos[i + 1] = APELIDOS_CPU[i];
		}
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// não precisa tratar
		}
	}

	@Override
	public int getNumClientes() {
		int numClientes = 0;
		for (int i = 0; i <= 2; i++) {
			if (connClientes[i] != null) {
				numClientes++;
			}
		}
		return numClientes;
	}

	private void atualizaClientes() {

		// Monta o comando de dados no formato:
		// I apelido1|apelido2|apelido3|apelido4 regras
		StringBuffer sbComando = new StringBuffer("I ");
		for (int i = 0; i <= 3; i++) {
			sbComando.append(apelidos[i]);
			sbComando.append(i < 3 ? '|' : ' ');
		}
		sbComando.append(regras);
		sbComando.append(' ');
		String comando = sbComando.toString();
		// Envia a notificação para cada jogador (com sua posição)
		for (int i = 0; i <= 2; i++) {
			enviaMensagem(i, comando + (i + 2));
		}
	}

	public synchronized void enviaMensagem(int slot, String comando) {
		if (outClientes[slot] != null) {
			Log.w("MINITRUCO", "enviando comando " + comando + " para slot "
					+ slot);
			try {
				outClientes[slot].write(comando.getBytes());
				outClientes[slot].write(SEPARADOR_ENV);
				outClientes[slot].flush();
			} catch (IOException e) {
				Log.w("MINITRUCO", e);
				// Libera o slot e encerra o jogo em andamento
				desconecta(slot);
				if (jogo != null) {
					jogo.abortaJogo(slot + 2);
				}
			}
		}
	}

	void desconecta(int slot) {
		Log.w("MINITRUCO", "desconecta() " + slot);
		try {
			outClientes[slot].close();
		} catch (Exception e) {
			// No prob, já deve ter morrido
		}
		try {
			connClientes[slot].close();
		} catch (Exception e) {
			// No prob, já deve ter morrido
		}
		if (slot >= 0) {
			connClientes[slot] = null;
			outClientes[slot] = null;
			apelidos[slot + 1] = APELIDOS_CPU[slot];
		}
		// -1 vai notificar que o servidor (posição -1+2=1) desistiu
		// -2 não notifica ninguém (posição -2+2=0)
		// TODO ver o que faz com isso
		// midlet.encerraJogo(slot + 2, false);
		status = STATUS_AGUARDANDO;
		atualizaDisplay();
		atualizaClientes();
	}

	public static Jogo criaNovoJogo(JogadorHumano jogadorHumano) {
		return currentInstance._criaNovoJogo(jogadorHumano);
	}

	public Jogo _criaNovoJogo(JogadorHumano jogadorHumano) {
		Jogo jogo = new JogoLocal(regras.charAt(0) == 'T',
				regras.charAt(1) == 'T', false);
		jogo.adiciona(jogadorHumano);
		for (int i = 0; i <= 2; i++) {
			if (connClientes[i] != null) {
				jogo.adiciona(new JogadorBluetooth(connClientes[i], this));
			} else {
				jogo.adiciona(new JogadorCPU());
			}
		}
		this.jogo = jogo;
		return jogo;
	}

}
