package me.chester.minitruco.android.multiplayer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.BuildConfig;
import me.chester.minitruco.R;
import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

public class ServidorBluetoothActivity extends BluetoothBaseActivity {

	private final static Logger LOGGER = Logger.getLogger("ServidorBluetoothActivity");

	private static final char STATUS_AGUARDANDO = 'A';
	private static final char STATUS_LOTADO = 'L';
	private static final char STATUS_EM_JOGO = 'J';
	private static final char STATUS_BLUETOOTH_ENCERRADO = 'X';
	private static final int REQUEST_ENABLE_DISCOVERY = 1;
	private static final String APELIDO_BOT = "bot";

	private static ServidorBluetoothActivity currentInstance;

	private char status;
	private Thread threadMonitoraClientes;
	private Jogo jogo;
	private boolean aguardandoDiscoverable = false;
	private Thread threadAguardaConexoes;
	private BluetoothServerSocket serverSocket;
	private final BluetoothSocket[] connClientes = new BluetoothSocket[3];
	private final OutputStream[] outClientes = new OutputStream[3];

	private final boolean[] respondeuComVersaoOk = new boolean[3];

	private final BroadcastReceiver receiverMantemDiscoverable = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			pedePraHabilitarDiscoverableSePreciso();
		}
	};

	@Override
	Logger logger() {
		return LOGGER;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentInstance = this;
	}

	@Override
	void iniciaAtividadeBluetooth() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		// TODO titulo poderia passar como extra do intent
		modo = preferences.getString("modo", "P");
		layoutIniciar.setVisibility(View.VISIBLE);
		btnIniciar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				status = STATUS_EM_JOGO;
				iniciaTrucoActivitySePreciso();
			}
		});
		registerReceiver(receiverMantemDiscoverable, new IntentFilter(
				BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
		pedePraHabilitarDiscoverableSePreciso();
		if (!aguardandoDiscoverable) {
			iniciaThreads();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.servidorbluetooth, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO limpar essa sujeira, herança do Java ME
		// (e ver se é uma boa fazer isso na UI thread mesmo)
		switch (item.getItemId()) {
			case R.id.menuitem_troca_parceiro:
				trocaParceiro();
				return true;
			case R.id.menuitem_inverte_adversarios:
				inverteAdversarios();
				return true;
			default:
				return super.onOptionsItemSelected(item);
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
		super.onActivityResult(requestCode, resultCode, data);
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
		try {
			unregisterReceiver(receiverMantemDiscoverable);
		} catch (IllegalArgumentException e) {
			// Não tem API pra verificar se está registrado, então é o que tem pra hoje
			// cf. https://stackoverflow.com/a/3568906
			LOGGER.log(Level.INFO, "Activity destruída antes do receiver ser registrado");
		}
		encerraConexoes();
	}

	public void run() {
		LOGGER.log(Level.INFO, "iniciou atividade server");
		inicializaDisplay();
		atualizaDisplay();
		try {
			serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(
					NOME_BT, UUID_BT);
		} catch (IOException e) {
			LOGGER.log(Level.INFO, "Exceção ao tentar iniciar o listen no servidor", e);
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
				setMensagem(null);
				sleep(1000);
				continue;
			}
			pedePraHabilitarDiscoverableSePreciso();
			setMensagem("Aguardando conexões...");
			// Se chegamos aqui, estamos fora de jogo e com vagas
			try {
				BluetoothSocket socket = serverSocket.accept();
				setMensagem(null);
				if (socket != null) {
					int slot = encaixaEmUmSlot(socket);
					verificaVersaoCompativel(slot);
				}
			} catch (IOException e) {
				LOGGER.log(Level.INFO, "Exceção em serverSocket.accept()", e);
			}
			if (isFinishing()) {
				status = STATUS_BLUETOOTH_ENCERRADO;
			}
		}
		encerraConexoes();
		LOGGER.log(Level.INFO, "finalizou atividade server");
	}

	/**
	 * Verifica (assincronamente) se o cliente conectado é compatível com este celular
	 *
	 * @param slot slot no qual o cliente está conectado.
	 */
	private void verificaVersaoCompativel(int slot) {
		// Idealmente bastaria esperar numa thread pelo comando "B <numero do build",
		// mas clientes < 2.03.09 nem vão enviar o comando. Então serão duas threads:
		//
		// - Uma vai aguardar o comando e, se receber com o build correto, marca o slot
		//   como tendo um cliente com versão válida;
		// - A outra aguarda um tempo razoável e caso o slot não tenha sido marcado como
		//   válido, desconecta o cliente e pede pra atualizar.
		BluetoothSocket socket = connClientes[slot];
		respondeuComVersaoOk[slot] = false;
		new Thread(() -> {
			StringBuilder sb = new StringBuilder();
			int c;
			try {
				InputStream in = socket.getInputStream();
				while (socket.isConnected() && (c = in.read()) != -1) {
					if (c == SEPARADOR_REC) {
						break;
					}
					sb.append((char) c);
				}
				LOGGER.info("recebeu ao conectar: " + sb.toString());
				if (sb.toString().equals("B " + BuildConfig.VERSION_CODE)) {
					respondeuComVersaoOk[slot] = true;
				}

			} catch (IOException e) {
				desconecta(slot);
			} catch (NumberFormatException e) {
				desconecta(slot);
			}
		}).start();

		new Thread(() -> {
			sleep(3000);
			if (!respondeuComVersaoOk[slot]) {
				desconecta(slot);
				mostraAlertBox("Versão antiga",
						"O aparelho " + socket.getRemoteDevice().getName() +
								" está rodando uma versão diferente do miniTruco " +
								" e foi desconectado.\n\n" +
								" Atualize o jogo em todos os celulares e tente novamente.");
			}
		}).start();
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
				LOGGER.log(Level.INFO, "Exceção em serverSocket.close()", e);
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
						// Envia um comando vazio (apenas para testar a conexão
						// e processar qualquer desconexão que tenha ocorrido)
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

	private void inicializaDisplay() {
		apelidos[0] = btAdapter.getName().replace(" ","_");
		apelidos[1] = APELIDO_BOT;
		apelidos[2] = APELIDO_BOT;
		apelidos[3] = APELIDO_BOT;
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
		sbComando.append(modo);
		sbComando.append(' ');
		String comando = sbComando.toString();
		// Envia a notificação para cada jogador (com sua posição)
		for (int i = 0; i <= 2; i++) {
			enviaMensagem(i, comando + (i + 2));
		}
	}

	void desconecta(int slot) {
		LOGGER.log(Level.INFO, "desconecta() " + slot);
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
			respondeuComVersaoOk[slot] = false;
			apelidos[slot + 1] = APELIDO_BOT;
		}
		status = STATUS_AGUARDANDO;
		atualizaDisplay();
		atualizaClientes();
		if (jogo != null) {
			jogo.abortaJogo(slot + 2);
		}
	}

	public static Jogo criaNovoJogo(JogadorHumano jogadorHumano) {
		return currentInstance._criaNovoJogo(jogadorHumano);
	}

	public Jogo _criaNovoJogo(JogadorHumano jogadorHumano) {
		Jogo jogo = new JogoLocal(modo, false, false);
		jogo.adiciona(jogadorHumano);
		for (int i = 0; i <= 2; i++) {
			if (connClientes[i] != null) {
				jogo.adiciona(new JogadorBluetooth(connClientes[i], this));
			} else {
				JogadorBot bot = new JogadorBot();
				bot.setFingeQuePensa(false);
				jogo.adiciona(bot);
			}
		}
		this.jogo = jogo;
		return jogo;
	}

	// Os métodos abaixo são synchronized para evitar trocas na mesa enquanto um
	// jogador está entrando no jogo (ou uma desconexão é descoberta por envio
	// de mensagem)

	public synchronized void enviaMensagem(int slot, String comando) {
		if (outClientes[slot] != null) {
			if (comando.length() > 0) {
				LOGGER.log(Level.INFO, "enviando comando " + comando
						+ " para slot " + slot);
			}
			try {
				outClientes[slot].write(comando.getBytes());
				outClientes[slot].write(SEPARADOR_ENV);
				outClientes[slot].flush();
			} catch (IOException e) {
				LOGGER.log(Level.INFO, "exceção ao enviar mensagem", e);
				desconecta(slot);
			}
		}
	}

	private synchronized int encaixaEmUmSlot(BluetoothSocket socket)
			throws IOException {
		for (int i = 0; i <= 2; i++) {
			if (connClientes[i] == null) {
				connClientes[i] = socket;
				outClientes[i] = socket.getOutputStream();
				apelidos[i + 1] = socket.getRemoteDevice().getName()
						.replace(' ', '_');
				status = i == 2 ? STATUS_LOTADO : STATUS_AGUARDANDO;
				return i;
			}
		}
		return -1;
	}

	private synchronized void inverteAdversarios() {
		Object temp;
		temp = connClientes[0];
		connClientes[0] = connClientes[2];
		connClientes[2] = (BluetoothSocket) temp;
		temp = outClientes[0];
		outClientes[0] = outClientes[2];
		outClientes[2] = (OutputStream) temp;
		temp = apelidos[1];
		apelidos[1] = apelidos[3];
		apelidos[3] = (String) temp;
		atualizaDisplay();
		atualizaClientes();
	}

	private synchronized void trocaParceiro() {
		Object temp;
		temp = connClientes[2];
		connClientes[2] = connClientes[1];
		connClientes[1] = connClientes[0];
		connClientes[0] = (BluetoothSocket) temp;
		temp = outClientes[2];
		outClientes[2] = outClientes[1];
		outClientes[1] = outClientes[0];
		outClientes[0] = (OutputStream) temp;
		temp = apelidos[3];
		apelidos[3] = apelidos[2];
		apelidos[2] = apelidos[1];
		apelidos[1] = (String) temp;
		atualizaDisplay();
		atualizaClientes();
	}

}
