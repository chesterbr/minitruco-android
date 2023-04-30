package me.chester.minitruco.android.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.core.Jogo;


/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

public class ClienteBluetoothActivity extends BluetoothBaseActivity implements
		Runnable {

    private final static Logger LOGGER = Logger.getLogger("ClienteBluetoothActivity");

	private static final int REQUEST_ENABLE_BT = 1;

	private static ClienteBluetoothActivity currentInstance;

	private List<BluetoothDevice> dispositivosPareados;
	private Thread threadConexao;
	private Thread threadMonitoraConexao;
	private JogoBluetooth jogo;
	private BluetoothSocket socket = null;
	private InputStream in;
	private OutputStream out;
	private int posJogador;

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
		listaDispositivosPareados();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dispositivosPareados != null) {
			dispositivosPareados.clear();
		}
		finalizaThreadFechandoConexoes();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_CANCELED) {
				// Sem bluetooth, sem cliente
				finish();
			} else {
				listaDispositivosPareados();
			}
		}
	}

	public void run() {
		atualizaDisplay();

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
						LOGGER.log(Level.INFO, "Recebeu:" + sbLinha);
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
				LOGGER.log(Level.INFO, "Enviando:" + linha);
			}
			out.write(linha.getBytes());
			out.write(ClienteBluetoothActivity.SEPARADOR_ENV);
			out.flush();
		} catch (IOException e) {
			LOGGER.log(Level.INFO, "Excção em EnviaLinha (desconexão?)", e);
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

    /**
     * Verifica se o bluetooh está ativo e caso não esteja pede para habilitar
     *
     * @return true caso o blutooh esteja ativo
     */
    private boolean habilitaBluetooh() {
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    /**
     * Tenta conectar no servidor recebido e, caso consiga, inicia o jogo
     *
     * @param device Servidor do Mni Truco
     */
    private void conectaNoServidor(BluetoothDevice device) {
        try {
            setMensagem("Consultando " + device.getName());
            socket = device.createRfcommSocketToServiceRecord(UUID_BT);
            sleep(1000);
            socket.connect();
            setMensagem("Conectado!");

            threadConexao = new Thread(ClienteBluetoothActivity.this);
            threadConexao.start();

        } catch (Exception e) {
            LOGGER.log(Level.INFO,
                    "Falhou conexao com device " + device.getName(), e);
            msgErroFatal("Falhou conexao com device " + device.getName() + ". Veja se o seu aparelho está pareado/autorizado com o que criou o jogo e tente novamente.");
            try {
                socket.close();
            } catch (Exception e1) {
                // Sem problemas, era só pra garantir
            }
        }
    }

    /**
     * Cria um array somente com os nomes dos aparelhos que serão apresentados na lista
     *
     * @return array com o nomes dos aparelhos pareados
     */
    private CharSequence[] criaArrayComNomeDosAparelhosPareados() {
        // monta um array com o nome dos possíveis servidores
        CharSequence[] serverNameArray = new String[dispositivosPareados.size()];
        int i = 0;
        for (BluetoothDevice device : dispositivosPareados) {
            serverNameArray[i] = device.getName();
            i++;
        }

        return serverNameArray;
    }

    /**
     * Mostra um dialog com uma lista com os aparelhos pareados.
     * Caso o bluetooth não esteja habilidado, dispara o dialog que permite habilitar.
     * Caso não tenha nenhum aparelho pareado, mostra uma mensagem de erro.
     */
    private void listaDispositivosPareados() {

        setMensagem(null);

        if (!habilitaBluetooh()) {
            return;
        }

        dispositivosPareados = new ArrayList<BluetoothDevice>();
        dispositivosPareados.addAll(btAdapter.getBondedDevices());

        if (dispositivosPareados.size() == 0) {
            msgErroFatal("Não existem aparelhos pareados. Veja se o seu aparelho está pareado/autorizado com o que criou o jogo e tente novamente.");
            return;
        }

        new AlertDialog.Builder(this).setTitle("Escolha o celular que criou o jogo")
                .setItems(criaArrayComNomeDosAparelhosPareados(), new AlertDialog.OnClickListener() {

                    public void onClick(DialogInterface dialog, int posicaoNaLista) {

                        conectaNoServidor(dispositivosPareados.get(posicaoNaLista));
                    }
                }).show();
    }

}
