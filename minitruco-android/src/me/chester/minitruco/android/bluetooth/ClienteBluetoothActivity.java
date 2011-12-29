package me.chester.minitruco.android.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.core.Jogo;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

public class ClienteBluetoothActivity extends BluetoothBaseActivity implements
		Runnable {

	private static ClienteBluetoothActivity currentInstance;

	private boolean estaVivo;
	private Set<BluetoothDevice> devicesEncontrados;
	private Thread threadConsultaDevicesEncontrados;

	private BroadcastReceiver receiverDescobreServidor = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				devicesEncontrados = new HashSet<BluetoothDevice>();
				Log.w("MINITRUCO", "Iniciou Discovery");
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				devicesEncontrados.add(device);
				Log.w("MINITRUCO",
						"Achou:" + device.getName() + "," + device.getAddress());
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				if (!isFinishing()) {
					threadConsultaDevicesEncontrados = new Thread(
							ClienteBluetoothActivity.this);
					threadConsultaDevicesEncontrados.start();
				}
			}
		}
	};

	private int posJogador;

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
		estaVivo = true;
		btAdapter.startDiscovery();
		// pedePraHabilitarDiscoverableSePreciso();
		// if (!aguardandoDiscoverable) {
		// iniciaThreadsSeNecessario();
		// }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiverDescobreServidor);
		btAdapter.cancelDiscovery();

	}

	public void run() {
		Log.w("MINITRUCO", "iniciou atividade cliente");
		atualizaDisplay();

		BluetoothSocket socket = null;
		for (BluetoothDevice device : devicesEncontrados) {
			try {
				Log.w("MINITRUCO", "Tentando conectar em" + device.getName());
				socket = device.createRfcommSocketToServiceRecord(UUID_BT);
				socket.connect();
				break;
			} catch (IOException e) {
				Log.w("MINITRUCO", e);
			}
		}
		if (socket == null) {
			new AlertDialog.Builder(this)
					.setTitle("Erro")
					.setMessage(
							Html.fromHtml("Nenhum servidor foi encontrado. Tente parear (autorizar) os celulares e repita a operação."))
					.setOnCancelListener(new OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					})
					.setNeutralButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).show();
		}
		Log.w("MINITRUCO", "Achou serviço e conectou: ");

		// Loop principal: decodifica as notificações recebidas e as
		// processa (ou encaminha ao JogoBT, se estivermos em jogo)
		int c;
		StringBuffer sbLinha = new StringBuffer();
		InputStream in;
		OutputStream out;
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
			while (estaVivo && (c = in.read()) != -1) {
				if (c == SEPARADOR_REC) {
					if (sbLinha.length() > 0) {
						Log.w("MINITRUCO", "Recebeu:" + sbLinha.toString());
						char tipoNotificacao = sbLinha.charAt(0);
						String parametros = sbLinha.delete(0, 2).toString();
						switch (tipoNotificacao) {
						case 'I':
							parametros = tiraEspacoDosNomes(parametros);
							// Encerra qualquer jogo em andamento
							if (jogo != null) {
								// TODO
								// midlet.encerraJogo(jogo.getJogadorHumano()
								// .getPosicao(), false);
								// display.setCurrent(this);
								// jogo = null;
							}
							// Exibe as informações recebidas fora do jogo
							String[] tokens = parametros.split(" ");
							posJogador = Integer.parseInt(tokens[2]);
							regras = tokens[1];
							encaixaApelidosNaMesa(tokens[0].split("\\|"));
							atualizaDisplay();
							break;
						case 'P':
							// // TODO Cria um o jogo remoto
							// jogo = new JogoBT(this);
							// // Adiciona o jogador na posição correta
							// // (preenchendo as outras com dummies)
							// for (int i = 1; i <= 4; i++) {
							// if (i == posJogador) {
							// midlet.jogadorHumano = new JogadorHumano(
							// display, midlet.mesa);
							// jogo.adiciona(midlet.jogadorHumano);
							//
							// } else {
							// jogo.adiciona(new JogadorDummy());
							// }
							// }
							// midlet.iniciaJogo(jogo);
							break;
						// Os outros eventos ocorrem durante o jogo,
						// i.e., quando o Jogador local já existe, logo,
						// vamos encaminhar para o objeto JogoRemoto
						default:
							// TODO
							// jogo.processaNotificacao(tipoNotificacao,
							// parametros);
						}
						sbLinha.setLength(0);
					}
				} else {
					sbLinha.append((char) c);
				}
			}
		} catch (IOException e) {
			if (estaVivo) {
				mostraAlertBox("Erro", e.getMessage());
				estaVivo = false;
			}
		} finally {
			Log.w("MINITRUCO", "saiu do loop");
			// Se a desconexão foi forçada, avisa e sai
			if (estaVivo) {
				mostraAlertBox("Desconectado", "Você foi desconectado do jogo");
			}
			finish();
		}
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

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// não precisa tratar
		}
	}

	private Jogo jogo;

	@Override
	public int getNumClientes() {
		return 0;
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

	public static Jogo criaNovoJogo(JogadorHumano jogadorHumano) {
		return currentInstance._criaNovoJogo(jogadorHumano);
	}

	public Jogo _criaNovoJogo(JogadorHumano jogadorHumano) {
		// Jogo jogo = new JogoLocal(regras.charAt(0) == 'T',
		// regras.charAt(1) == 'T', false);
		// jogo.adiciona(jogadorHumano);
		// for (int i = 0; i <= 2; i++) {
		// if (connClientes[i] != null) {
		// jogo.adiciona(new JogadorBluetooth(connClientes[i], this));
		// } else {
		// jogo.adiciona(new JogadorCPU());
		// }
		// }
		// this.jogo = jogo;
		// return jogo;
		return null;
	}

}
