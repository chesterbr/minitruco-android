package me.chester.minitruco.android.multiplayer.bluetooth;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import me.chester.minitruco.R;
import me.chester.minitruco.android.BaseActivity;
import me.chester.minitruco.android.TrucoActivity;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Tarefas comuns ao cliente e ao servidor Bluetooth: mostrar quem está
 * conectado, garantir que o bt está ligado e as permissões cedidas, etc.
 */
public abstract class BluetoothBaseActivity extends BaseActivity implements
		Runnable {

	public static String[] BLUETOOTH_PERMISSIONS;
	{
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
			// Android 11 ou anterior pede permissões genéricas
			BLUETOOTH_PERMISSIONS = new String[] {
					Manifest.permission.BLUETOOTH,
					Manifest.permission.BLUETOOTH_ADMIN,
			};
		} else {
			// Android 12 ou superior pede permissões mais refinadas
			BLUETOOTH_PERMISSIONS = new String[] {
					Manifest.permission.BLUETOOTH_CONNECT,
					Manifest.permission.BLUETOOTH_SCAN,
					Manifest.permission.BLUETOOTH_ADVERTISE,
			};
		}
	};

	/**
	 * Vamos usar o logger da classe correta (cliente ou servidor)
	 */
	abstract Logger logger();

	/**
	 * Separador de linha recebido
	 */
	public static final int SEPARADOR_REC = '*';

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
	 * Identificadores Bluetooth do "serviço miniTruco"
	 */
	public static final String NOME_BT = "miniTruco";
	public static final UUID UUID_BT = UUID
			.fromString("3B175368-ABB4-11DB-A508-C2B155D89593");

	private static final int MSG_MOSTRA_MENSAGEM = 1;
	private static final int MSG_ERRO_FATAL = 2;

	protected BluetoothAdapter btAdapter;
	protected String[] apelidos = new String[4];
	protected String regras;
	protected Button btnIniciar;
	protected View layoutIniciar;
	private TextView textViewMensagem;
	private TextView textViewRegras;
	private TextView[] textViewsJogadores;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		layoutIniciar = (View) findViewById(R.id.layoutIniciar);
		btnIniciar = (Button) findViewById(R.id.btnIniciarBluetooth);
		textViewMensagem = ((TextView) findViewById(R.id.textViewMensagem));
		textViewRegras = (TextView) findViewById(R.id.textViewRegras);
		textViewsJogadores = new TextView[4];
		textViewsJogadores[0] = (TextView) findViewById(R.id.textViewJogador1);
		textViewsJogadores[1] = (TextView) findViewById(R.id.textViewJogador2);
		textViewsJogadores[2] = (TextView) findViewById(R.id.textViewJogador3);
		textViewsJogadores[3] = (TextView) findViewById(R.id.textViewJogador4);
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		String[] permissoesFaltantes = permissoesBluetoothFaltantes();
		if (permissoesFaltantes.length == 0) {
			iniciaAtividadeBluetooth();
		} else {
			permissionsLauncher.launch(permissoesFaltantes);
		}

	}
	private String[] permissoesBluetoothFaltantes() {
		// Antes do Android 6, permissões eram declaradas no manifest, e a app simplesmente
		// assumia que foi autorizada. A vida era simples. Eu sinto falta disso.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return new String[0];
		}
		// Versões mais novas pedem permissões de runtime, então começa a dança da manivela:
		List<String> permissoes = new ArrayList<>();
		for (String permission: BLUETOOTH_PERMISSIONS) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				permissoes.add(permission);
			}
		}
		return permissoes.toArray(new String[0]);
	}


	ActivityResultLauncher<String[]> permissionsLauncher =
		registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
				result -> {
					String[] permissoesFaltantes = permissoesBluetoothFaltantes();
					if (permissoesFaltantes.length == 0) {
						iniciaAtividadeBluetooth();
					} else {
						for (int i = 0; i < permissoesFaltantes.length; i++) {
							permissoesFaltantes[i] = permissoesFaltantes[i].replace("android.permission.", "");
						}
						msgErroFatal("Não foi possivel obter permissões: " +
								     Arrays.toString(permissoesFaltantes) + ".\n\n" +
								     "Se o problema persistir, tente autorizar "+
								     "nas configurações do celular (em \"Aplicativos\"), " +
								     "ou desinstalar e reinstalar o jogo.");
					}
				});

	/**
	 * As atividades de Bluetooth são vão iniciar quando as permissões estiverem garantidas,
	 * através da chamada deste método
	 */
	abstract void iniciaAtividadeBluetooth();

	protected void atualizaDisplay() {
		Message.obtain(handlerAtualizaDisplay).sendToTarget();
	}

	protected void setMensagem(String mensagem) {
		Message.obtain(handlerAtualizaDisplay, MSG_MOSTRA_MENSAGEM, mensagem)
				.sendToTarget();
	}

	protected void msgErroFatal(String mensagem) {
		Message.obtain(handlerAtualizaDisplay, MSG_ERRO_FATAL, mensagem)
				.sendToTarget();
	}

	protected abstract int getNumClientes();

	Handler handlerAtualizaDisplay = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_MOSTRA_MENSAGEM:
				if (msg.obj == null) {
					textViewMensagem.setVisibility(View.GONE);
				} else {
					textViewMensagem.setVisibility(View.VISIBLE);
					textViewMensagem.setText((String) msg.obj);
				}
				break;
			case MSG_ERRO_FATAL:
				new AlertDialog.Builder(BluetoothBaseActivity.this)
						.setTitle("Erro")
						.setMessage((String) msg.obj)
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
				break;
			}
			for (int i = 0; i < 4; i++) {
				textViewsJogadores[i].setText(apelidos[i]);
			}
			textViewRegras.setText(getTextoRegras());
			btnIniciar.setEnabled(getNumClientes() > 0);
		}

	};

	protected void iniciaTrucoActivitySePreciso() {
		if (!TrucoActivity.isViva()) {
			Intent intent = new Intent(this, TrucoActivity.class);
			if (this instanceof ClienteBluetoothActivity) {
				intent.putExtra("clienteBluetooth", true);
			} else {
				intent.putExtra("servidorBluetooth", true);
			}
			startActivity(intent);
		}
	}

	protected String getTextoRegras() {
		if (regras == null || regras.length() < 2) {
			return "";
		}
		return (regras.charAt(0) == 'T' ? "Baralho Limpo" : "Baralho Sujo")
				+ " / "
				+ (regras.charAt(1) == 'T' ? "Manilha Velha" : "Manilha Nova");
	}

	protected void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// não precisa tratar
		}
	}

}
