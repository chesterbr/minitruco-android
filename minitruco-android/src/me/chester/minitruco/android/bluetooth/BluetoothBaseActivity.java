package me.chester.minitruco.android.bluetooth;

import java.util.UUID;

import me.chester.minitruco.R;
import me.chester.minitruco.android.BaseActivity;
import me.chester.minitruco.android.TrucoActivity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Tarefas comuns ao cliente e ao servidor Bluetooth: mostrar quem está
 * conectado, garantir que o bt está ligado, iniciar a thread, etc.
 */
public abstract class BluetoothBaseActivity extends BaseActivity implements
		Runnable {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		layoutIniciar = (View) findViewById(R.id.layoutIniciar);
		btnIniciar = (Button) findViewById(R.id.btnIniciarBluetooth);
		textViewMensagem = ((TextView) findViewById(R.id.textViewMensagem));
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

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
			((TextView) findViewById(R.id.textViewJogador1))
					.setText(apelidos[0]);
			((TextView) findViewById(R.id.textViewJogador2))
					.setText(apelidos[1]);
			((TextView) findViewById(R.id.textViewJogador3))
					.setText(apelidos[2]);
			((TextView) findViewById(R.id.textViewJogador4))
					.setText(apelidos[3]);
			btnIniciar.setEnabled(getNumClientes() > 0);
		}
	};

	protected void iniciaPartida() {
		Intent intent = new Intent(this, TrucoActivity.class);
		if (this instanceof ClienteBluetoothActivity) {
			intent.putExtra("clienteBluetooth", true);
		} else {
			intent.putExtra("servidorBluetooth", true);
		}
		startActivity(intent);
	}

	protected void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// não precisa tratar
		}
	}

}
