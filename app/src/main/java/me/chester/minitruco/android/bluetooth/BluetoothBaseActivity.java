package me.chester.minitruco.android.bluetooth;

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

import java.util.UUID;

import me.chester.minitruco.R;
import me.chester.minitruco.android.BaseActivity;
import me.chester.minitruco.android.TrucoActivity;

/*
 * Copyright © 2005-2012 Carlos Duarte do Nascimento "Chester" <cd@pobox.com>
 * Todos os direitos reservados.
 *
 * A redistribuição e o uso nas formas binária e código fonte, com ou sem
 * modificações, são permitidos contanto que as condições abaixo sejam
 * cumpridas:
 * 
 * - Redistribuições do código fonte devem conter o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente.
 * 
 * - Redistribuições na forma binária devem reproduzir o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente na documentação e/ou materiais fornecidos com
 *   a distribuição.
 *   
 * - Nem o nome do Chester, nem o nome dos contribuidores podem ser
 *   utilizados para endossar ou promover produtos derivados deste
 *   software sem autorização prévia específica por escrito.
 * 
 * ESTE SOFTWARE É FORNECIDO PELOS DETENTORES DE DIREITOS AUTORAIS E
 * CONTRIBUIDORES "COMO ESTÁ", ISENTO DE GARANTIAS EXPRESSAS OU TÁCITAS,
 * INCLUINDO, SEM LIMITAÇÃO, QUAISQUER GARANTIAS IMPLÍCITAS DE
 * COMERCIABILIDADE OU DE ADEQUAÇÃO A FINALIDADES ESPECÍFICAS. EM NENHUMA
 * HIPÓTESE OS TITULARES DE DIREITOS AUTORAIS E CONTRIBUIDORES SERÃO
 * RESPONSÁVEIS POR QUAISQUER DANOS, DIRETOS, INDIRETOS, INCIDENTAIS,
 * ESPECIAIS, EXEMPLARES OU CONSEQUENTES, (INCLUINDO, SEM LIMITAÇÃO,
 * FORNECIMENTO DE BENS OU SERVIÇOS SUBSTITUTOS, PERDA DE USO OU DADOS,
 * LUCROS CESSANTES, OU INTERRUPÇÃO DE ATIVIDADES), CAUSADOS POR QUAISQUER
 * MOTIVOS E SOB QUALQUER TEORIA DE RESPONSABILIDADE, SEJA RESPONSABILIDADE
 * CONTRATUAL, RESTRITA, ILÍCITO CIVIL, OU QUALQUER OUTRA, COMO DECORRÊNCIA
 * DE USO DESTE SOFTWARE, MESMO QUE HOUVESSEM SIDO AVISADOS DA
 * POSSIBILIDADE DE TAIS DANOS.
 * 
 */

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
