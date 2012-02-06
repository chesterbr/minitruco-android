package me.chester.minitruco.android.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.core.Jogo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class ClienteBluetoothActivity extends BluetoothBaseActivity implements
		Runnable {

	private static final int REQUEST_ENABLE_BT = 1;

	private static ClienteBluetoothActivity currentInstance;

	private Set<BluetoothDevice> devicesEncontrados;
	private Thread threadConexao;
	private Thread threadMonitoraConexao;
	private JogoBluetooth jogo;
	private BluetoothSocket socket = null;
	private InputStream in;
	private OutputStream out;
	private int posJogador;

	private BroadcastReceiver receiverDescobreServidor = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				devicesEncontrados = new HashSet<BluetoothDevice>();
				setMensagem("Procurando aparelhos...");
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				devicesEncontrados.add(device);
				setMensagem("Achou " + devicesEncontrados.size() + "...");
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				if (!isFinishing()) {
					threadConexao = new Thread(ClienteBluetoothActivity.this);
					threadConexao.start();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentInstance = this;
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiverDescobreServidor, filter);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		iniciaProcuraDeCelulares();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiverDescobreServidor);
		btAdapter.cancelDiscovery();
		finalizaThreadFechandoConexoes();
	}

	private void iniciaProcuraDeCelulares() {
		if (btAdapter.isEnabled()) {
			boolean result = btAdapter.startDiscovery();
			Log.w("MINITRUCO", "discovery: " + result);
			return;
		}
		Intent enableBtIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_CANCELED) {
				// Sem bluetooth, sem cliente
				finish();
			} else {
				iniciaProcuraDeCelulares();
			}
		}
	}

	public void run() {
		atualizaDisplay();
		socket = procuraServidorNosDevicesEncontrados();
		if (socket == null) {
			msgErroFatal("Jogo não encontrado. Veja se o seu aparelho está pareado/autorizado com o que criou o jogo e tente novamente.");
			return;
		}
		sleep(500);
		setMensagem(null);
		iniciaMonitorConexao();
		// Loop principal: decodifica as notificações recebidas e as
		// processa (ou encaminha ao JogoBT, se estivermos em jogo)
		int c;
		StringBuffer sbLinha = new StringBuffer();
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
			while ((c = in.read()) != -1) {
				if (c == SEPARADOR_REC) {
					if (sbLinha.length() > 0) {
						Log.w("MINITRUCO", "Recebeu:" + sbLinha.toString());
						char tipoNotificacao = sbLinha.charAt(0);
						String parametros = sbLinha.delete(0, 2).toString();
						switch (tipoNotificacao) {
						case 'I':
							exibeMesaForaDoJogo(parametros);
							break;
						case 'P':
							iniciaTrucoActivitySePreciso();
							// Não tem mesmo um break aqui, o início de partida
							// também precisa ser processado pelo jogo anterior
							// (para limpar o placar)
						default:
							if (jogo != null) {
								jogo.processaNotificacao(tipoNotificacao,
										parametros);
							}
						}
						sbLinha.setLength(0);
					}
				} else {
					sbLinha.append((char) c);
				}
			}
		} catch (IOException e) {
			if (!isFinishing()) {
				if (jogo != null) {
					jogo.abortaJogo(0);
				}
				msgErroFatal("Você foi desconectado");
			}
		}
	}

	private void iniciaMonitorConexao() {
		if (threadMonitoraConexao == null) {
			threadMonitoraConexao = new Thread() {
				public void run() {
					while (threadConexao.isAlive()) {
						// Envia comando vazio, apenas para garantir desbloqueio
						// de I/O na thread principal se o servidor sumir
						for (int i = 0; i <= 2; i++) {
							enviaLinha("");
						}
						ClienteBluetoothActivity.this.sleep(2000);
					}
				}
			};
			threadMonitoraConexao.start();
		}
	}

	private BluetoothSocket procuraServidorNosDevicesEncontrados() {
		for (BluetoothDevice device : devicesEncontrados) {
			BluetoothSocket candidato = null;
			try {
				setMensagem("Consultando " + device.getName());
				candidato = device.createRfcommSocketToServiceRecord(UUID_BT);
				sleep(1000);
				candidato.connect();
				setMensagem("Conectado!");
				return candidato;
			} catch (Exception e) {
				Log.w("MINITRUCO",
						"Falhou conexao com device " + device.getName());
				Log.w("MINITRUCO", e);
				try {
					candidato.close();
				} catch (Exception e1) {
					// Sem problemas, era só pra garantir
				}
			}
		}
		setMensagem(null);
		return null;
	}

	private void exibeMesaForaDoJogo(String parametros) {
		parametros = tiraEspacoDosNomes(parametros);
		if (jogo != null) {
			jogo.abortaJogo(0);
			jogo = null;
		}
		// Exibe as informações recebidas fora do jogo
		String[] tokens = parametros.split(" ");
		posJogador = Integer.parseInt(tokens[2]);
		regras = tokens[1];
		encaixaApelidosNaMesa(tokens[0].split("\\|"));
		atualizaDisplay();
	}

	private void encaixaApelidosNaMesa(String[] apelidosOriginais) {
		for (int n = 1; n <= 4; n++) {
			apelidos[getPosicaoMesa(n) - 1] = apelidosOriginais[n - 1];
		}
	}

	private String tiraEspacoDosNomes(String parametros) {
		while (parametros.split(" ").length > 3) {
			parametros = parametros.replaceFirst(" ", "_");
		}
		return parametros;
	}

	@Override
	public int getNumClientes() {
		return 0;
	}

	/**
	 * Manda um comando para o celular do servidor (se houver um conectado).
	 * 
	 * @param linha
	 */
	public synchronized void enviaLinha(String linha) {
		try {
			if (out == null) {
				return;
			}
			if (linha.length() > 0) {
				Log.w("MINITRUCO", "Enviando:" + linha);
			}
			out.write(linha.getBytes());
			out.write(ClienteBluetoothActivity.SEPARADOR_ENV);
			out.flush();
		} catch (IOException e) {
			Log.w("MINITRUCO", e);
			// Não preciso tratar, desconexões são identificadas no loop do in
		}
	}

	/**
	 * Recupera a posição "visual" correspondente a uma posição de jogo (i.e.,
	 * uma posição no servidor)
	 * <p>
	 * A idéia é que o jogador local fique sempre na parte inferior da tela,
	 * então o método retorna 1 para o jogador local, 2 para quem está à direita
	 * dele, etc.
	 * 
	 * @param i
	 *            posição (no servidor) do jogador que queremos consultar
	 */
	public int getPosicaoMesa(int i) {
		int retorno = i - posJogador + 1;
		if (retorno < 1)
			retorno += 4;
		return retorno;
	}

	private void finalizaThreadFechandoConexoes() {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	public static Jogo criaNovoJogo(JogadorHumano jogadorHumano) {
		return currentInstance._criaNovoJogo(jogadorHumano);
	}

	public Jogo _criaNovoJogo(JogadorHumano jogadorHumano) {
		jogo = new JogoBluetooth(this);
		// Adiciona o jogador na posição correta
		// (preenchendo as outras com dummies)
		for (int i = 1; i <= 4; i++) {
			if (i == posJogador) {
				jogo.adiciona(jogadorHumano);
			} else {
				jogo.adiciona(new JogadorDummy());
			}
		}
		return jogo;
	}

}
