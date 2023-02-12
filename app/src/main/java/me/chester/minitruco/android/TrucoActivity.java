package me.chester.minitruco.android;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.chester.minitruco.R;
import me.chester.minitruco.android.bluetooth.ClienteBluetoothActivity;
import me.chester.minitruco.android.bluetooth.ServidorBluetoothActivity;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;

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
 * Activity onde os jogos (partidas) efetivamente acontecem..
 * <p>
 * Ela inicializa o jogo e exibe sa cartas, "balões" de texto e diálogos através
 * de uma <code>MesaView</code>.
 * 
 * 
 */
public class TrucoActivity extends BaseActivity {

	private static final String[] TEXTO_BOTAO_AUMENTO = { "Truco", "Seis!",
			"NOVE!", "DOZE!!!" };

	private MesaView mesa;
	private TextView textViewAnuncio;
	private View layoutAnuncio;
	private View layoutFimDeJogo;
	private static boolean mIsViva = false;
	boolean jogoAbortado = false;

	JogadorHumano jogadorHumano;

	Jogo jogo;

	int[] placar = new int[2];

	static final int MSG_ATUALIZA_PLACAR = 0;
	static final int MSG_TIRA_DESTAQUE_PLACAR = 1;
	static final int MSG_OFERECE_NOVA_PARTIDA = 2;
	static final int MSG_REMOVE_NOVA_PARTIDA = 3;
	static final int MSG_MOSTRA_BOTAO_AUMENTO = 4;
	static final int MSG_ESCONDE_BOTAO_AUMENTO = 5;
	static final int MSG_MOSTRA_BOTAO_ABERTA_FECHADA = 6;
	static final int MSG_ESCONDE_BOTAO_ABERTA_FECHADA = 7;
	static final int MSG_ESCONDE_PATROCINIO = 8;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			TextView tvNos = (TextView) findViewById(R.id.textview_nos);
			TextView tvEles = (TextView) findViewById(R.id.textview_eles);
			Button btnAumento = (Button) findViewById(R.id.btnAumento);
			Button btnAbertaFechada = (Button) findViewById(R.id.btnAbertaFechada);
			Button btnNovaPartida = (Button) findViewById(R.id.btnNovaPartida);
			switch (msg.what) {
			case MSG_ATUALIZA_PLACAR:
				if (placar[0] != msg.arg1) {
					tvNos.setBackgroundColor(Color.YELLOW);
				}
				if (placar[1] != msg.arg2) {
					tvEles.setBackgroundColor(Color.YELLOW);
				}
				tvNos.setText("Nós: " + msg.arg1);
				tvEles.setText("Eles: " + msg.arg2);
				placar[0] = msg.arg1;
				placar[1] = msg.arg2;
				break;
			case MSG_TIRA_DESTAQUE_PLACAR:
				tvNos.setBackgroundColor(Color.TRANSPARENT);
				tvEles.setBackgroundColor(Color.TRANSPARENT);
				break;
			case MSG_ESCONDE_PATROCINIO:
				layoutAnuncio.setVisibility(View.INVISIBLE);
				break;
			case MSG_OFERECE_NOVA_PARTIDA:
				if (jogo instanceof JogoLocal) {
					layoutFimDeJogo.setVisibility(View.VISIBLE);
					if (jogo.isJogoAutomatico()) {
						btnNovaPartida.performClick();
					}
				}
				break;
			case MSG_REMOVE_NOVA_PARTIDA:
				layoutFimDeJogo.setVisibility(View.INVISIBLE);
				break;
			case MSG_MOSTRA_BOTAO_AUMENTO:
				btnAumento
						.setText(TEXTO_BOTAO_AUMENTO[(jogadorHumano.valorProximaAposta / 3) - 1]);
				btnAumento.setVisibility(Button.VISIBLE);
				break;
			case MSG_ESCONDE_BOTAO_AUMENTO:
				btnAumento.setVisibility(Button.GONE);
				break;
			case MSG_MOSTRA_BOTAO_ABERTA_FECHADA:
				btnAbertaFechada.setText(mesa.vaiJogarFechada ? "Aberta"
						: "Fechada");
				btnAbertaFechada.setVisibility(Button.VISIBLE);
				break;
			case MSG_ESCONDE_BOTAO_ABERTA_FECHADA:
				btnAbertaFechada.setVisibility(Button.GONE);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * Cria um novo jogo e dispara uma thread para ele. Para jogos multiplayer,
	 * a criação é terceirizada para a classe apropriada.
	 * <p>
	 * Este método é chamada pela primeira vez a partir da MesaView (para
	 * garantir que o jogo só role quando ela estiver inicializada) e dali em
	 * diante pelo botão de nova partida.
	 */
	public void criaEIniciaNovoJogo() {
		jogadorHumano = new JogadorHumano(this, mesa);
		if (getIntent().hasExtra("servidorBluetooth")) {
			jogo = ServidorBluetoothActivity.criaNovoJogo(jogadorHumano);
		} else if (getIntent().hasExtra("clienteBluetooth")) {
			jogo = ClienteBluetoothActivity.criaNovoJogo(jogadorHumano);
		} else {
			jogo = criaNovoJogoSinglePlayer(jogadorHumano);
		}
		(new Thread(jogo)).start();
	}

	private Jogo criaNovoJogoSinglePlayer(JogadorHumano humano) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean tentoMineiro = preferences.getBoolean("tentoMineiro", false);
		boolean baralhoLimpo = preferences.getBoolean("baralhoLimpo", false);
		boolean manilhaVelha = preferences.getBoolean("manilhaVelha", false)
				&& !baralhoLimpo;
		boolean humanoDecide = preferences.getBoolean("humanoDecide", true);
		boolean jogoAutomatico =  preferences.getBoolean("jogoAutomatico", false);
		Jogo novoJogo = new JogoLocal(baralhoLimpo, manilhaVelha, tentoMineiro, humanoDecide, jogoAutomatico);
		novoJogo.adiciona(jogadorHumano);
		for (int i = 2; i <= 4; i++) {
			novoJogo.adiciona(new JogadorCPU());
		}
		return novoJogo;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.truco);
		mIsViva = true;
		mesa = ((MesaView) findViewById(R.id.MesaView01));
		layoutFimDeJogo = findViewById(R.id.layoutFimDeJogo);

		mesa.setTrucoActivity(this);
		// Inicializa componentes das classes visuais que dependem de métodos
		// disponíveis exclusivamente na Activity
		if (CartaVisual.resources == null) {
			CartaVisual.resources = getResources();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.truco, menu);
		return true;
	}

	public void novaPartidaClickHandler(View v) {
		Message.obtain(handler, MSG_REMOVE_NOVA_PARTIDA).sendToTarget();
		criaEIniciaNovoJogo();
	}

	public void aumentoClickHandler(View v) {
		handler.sendMessage(Message.obtain(handler, MSG_ESCONDE_BOTAO_AUMENTO));
		mesa.setStatusVez(MesaView.STATUS_VEZ_HUMANO_AGUARDANDO);
		jogo.aumentaAposta(jogadorHumano);
	}

	public void abertaFechadaClickHandler(View v) {
		mesa.vaiJogarFechada = !mesa.vaiJogarFechada;
		handler.sendMessage(Message.obtain(handler,
				MSG_MOSTRA_BOTAO_ABERTA_FECHADA));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mesa.setVisivel(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mesa.setVisivel(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mIsViva = false;
		if (!jogoAbortado) {
			jogo.abortaJogo(1);
		}
	}

	public static boolean isViva() {
		return mIsViva;
	}
}
