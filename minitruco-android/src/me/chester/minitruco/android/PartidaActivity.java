package me.chester.minitruco.android;

import me.chester.minitruco.R;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Interessado;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
 * Copyright © 2005-2011 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença.
 *
 * Este programa é distribuido na esperança que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO
 * a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença
 * Pública Geral GNU para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa, se não, escreva para a Fundação do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Activity que efetivamente permite jogar uma partida.
 * <p>
 * A partida é exibida através de uma <code>MesaView</code>. Ela descobre o jogo
 * que tem que jogar lendo a propriedade jogo da classe
 * <code>MenuPrincipal</code>.
 * 
 * @author chester
 * 
 */
public class PartidaActivity extends Activity implements Interessado {

	private static final String[] TEXTO_BOTAO_AUMENTO = { "Truco", "Seis!",
			"NOVE!", "DOZE!!!" };

	private int valorProximaAposta;

	private boolean mostrarBotaoAumento = false;

	private boolean mostrarBotaoAbertaFechada = false;

	private MesaView mesa;

	Jogo jogo;

	private int[] placar = new int[2];

	private static final int MSG_ATUALIZA_PLACAR = 0;
	private static final int MSG_TIRA_DESTAQUE_PLACAR = 1;
	private static final int MSG_MOSTRA_BTN_NOVA_PARTIDA = 2;
	private static final int MSG_ESCONDE_BTN_NOVA_PARTIDA = 3;

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			TextView tvNos = (TextView) findViewById(R.id.textview_nos);
			TextView tvEles = (TextView) findViewById(R.id.textview_eles);
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
			case MSG_MOSTRA_BTN_NOVA_PARTIDA:
			case MSG_ESCONDE_BTN_NOVA_PARTIDA:
				Button btnNovaPartida = (Button) findViewById(R.id.btnNovaPartida);
				btnNovaPartida
						.setVisibility(msg.what == MSG_MOSTRA_BTN_NOVA_PARTIDA ? Button.VISIBLE
								: Button.INVISIBLE);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * Cria e inicia um novo jogo. É chamada pela primeira vez a partir da
	 * MesaView (para garantir que o jogo só role quando ela estiver
	 * inicializada) e dali em diante pelo botão de nova partida.
	 */
	public void criaNovoJogo() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean baralhoLimpo = preferences.getBoolean("baralhoLimpo", false);
		boolean manilhaVelha = preferences.getBoolean("manilhaVelha", false)
				&& !baralhoLimpo;
		Log.d("opcoes_bl_mv", baralhoLimpo + "," + manilhaVelha);
		jogo = new JogoLocal(baralhoLimpo, manilhaVelha);
		jogo.adiciona(new JogadorHumano());
		for (int i = 2; i <= 4; i++) {
			jogo.adiciona(new JogadorCPU());
		}
		jogo.adiciona(this);
		(new Thread(jogo)).start();
	}

	// Eventos da Activity (chamados pelo Android)

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.partida);
		mesa = ((MesaView) findViewById(R.id.MesaView01));
		mesa.setPartidaActivity(this);
		// Inicializa componentes das classes visuais que dependem de métodos
		// disponíveis exclusivamente na Activity
		if (MesaView.iconesRodadas == null) {
			MesaView.iconesRodadas = new Bitmap[4];
			MesaView.iconesRodadas[0] = ((BitmapDrawable) getResources()
					.getDrawable(R.drawable.placarrodada0)).getBitmap();
			MesaView.iconesRodadas[1] = ((BitmapDrawable) getResources()
					.getDrawable(R.drawable.placarrodada1)).getBitmap();
			MesaView.iconesRodadas[2] = ((BitmapDrawable) getResources()
					.getDrawable(R.drawable.placarrodada2)).getBitmap();
			MesaView.iconesRodadas[3] = ((BitmapDrawable) getResources()
					.getDrawable(R.drawable.placarrodada3)).getBitmap();
		}
		if (CartaVisual.resources == null) {
			CartaVisual.resources = getResources();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuitem_aumento:
			mostrarBotaoAumento = false;
			mesa.setStatusVez(MesaView.STATUS_VEZ_HUMANO_AGUARDANDO);
			jogo.aumentaAposta(jogo.getJogadorHumano());
			return true;
		case R.id.menuitem_aberta:
			mesa.vaiJogarFechada = false;
			return true;
		case R.id.menuitem_fechada:
			mesa.vaiJogarFechada = true;
			return true;
		case R.id.menuitem_sair_jogo:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.partida_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem miAumento = menu.findItem(R.id.menuitem_aumento);
		MenuItem miAberta = menu.findItem(R.id.menuitem_aberta);
		MenuItem miFechada = menu.findItem(R.id.menuitem_fechada);
		if (mostrarBotaoAumento) {
			miAumento
					.setTitle(TEXTO_BOTAO_AUMENTO[(valorProximaAposta / 3) - 1]);
		}
		miAumento.setVisible(mostrarBotaoAumento);
		miAberta.setVisible(mostrarBotaoAbertaFechada && mesa.vaiJogarFechada);
		miFechada
				.setVisible(mostrarBotaoAbertaFechada && !mesa.vaiJogarFechada);
		return true;
	}

	public void novaPartidaClickHandler(View v) {
		handler.sendMessage(Message.obtain(handler,
				MSG_ESCONDE_BTN_NOVA_PARTIDA));
		criaNovoJogo();
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
		jogo.abortaJogo(1);
	}

	// Eventos de Interessado (chamados pelo jogo)

	public void aceitouAumentoAposta(Jogador j, int valor) {
		if (jogo.getJogadorHumano() != null) {
			if (j.getEquipe() == 1) {
				// Nós aceitamos um truco, então podemos pedir 6, 9 ou 12
				if (valor != 12) {
					valorProximaAposta = valor + 3;
				}
			} else {
				// Eles aceitaram um truco, temos que esperar eles pedirem
				valorProximaAposta = 0;
			}
		}
		mesa.aguardaFimAnimacoes();
		mesa.diz("aumento_sim", j.getPosicao(), 1500);
		mesa.aceitouAumentoAposta(j, valor);
	}

	public void cartaJogada(Jogador j, Carta c) {
		mesa.mostrarPerguntaMao11 = false;
		mesa.mostrarPerguntaAumento = false;
		mostrarBotaoAumento = false;
		mesa.aguardaFimAnimacoes();
		mesa.descarta(c, j.getPosicao());
		Log.i("Partida", "Jogador " + j.getPosicao() + " jogou " + c);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		if (j.getPosicao() == 3 && aceita) {
			mesa.mostrarPerguntaMao11 = false;
		}
		mesa.aguardaFimAnimacoes();
		mesa.diz(aceita ? "mao11_sim" : "mao11_nao", j.getPosicao(), 1500);
	}

	public void entrouNoJogo(Interessado i, Jogo j) {

	}

	public void informaMao11(Carta[] cartasParceiro) {
		// mesa.aguardaFimAnimacoes();
		if (jogo.getJogadorHumano() != null) {
			mesa.mostraCartasMao11(cartasParceiro);
			mesa.mostrarPerguntaMao11 = true;
		}

	}

	public void inicioMao() {
		valorProximaAposta = 3;
		mesa.aguardaFimAnimacoes();
		for (int i = 0; i <= 2; i++) {
			mesa.resultadoRodada[i] = 0;
		}
		mesa.distribuiMao();
		handler.sendMessage(Message.obtain(handler, MSG_TIRA_DESTAQUE_PLACAR));
	}

	public void inicioPartida(int placarEquipe1, int placarEquipe2) {
		placar[0] = placarEquipe1;
		placar[1] = placarEquipe2;
		handler.sendMessage(Message.obtain(handler, MSG_ATUALIZA_PLACAR, placarEquipe1, placarEquipe2));
	}

	public void jogoAbortado(int posicao) {

	}

	public void jogoFechado(int numEquipeVencedora) {
		mesa.aguardaFimAnimacoes();
		mesa.diz(numEquipeVencedora == 1 ? "vitoria" : "derrota", 1, 1000);
		mesa.aguardaFimAnimacoes();
		handler.sendMessage(Message
				.obtain(handler, MSG_MOSTRA_BTN_NOVA_PARTIDA));
	}

	public void maoFechada(int[] pontosEquipe) {
		mostrarBotaoAumento = false;
		mesa.aguardaFimAnimacoes();
		handler.sendMessage(Message.obtain(handler, MSG_ATUALIZA_PLACAR,
				pontosEquipe[0], pontosEquipe[1]));
		mesa.aguardaFimAnimacoes();
		mesa.recolheMao();

	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		mesa.aguardaFimAnimacoes();
		mesa.diz("aumento_" + valor, j.getPosicao(), 1500 + 200 * (valor / 3));
		Log.d("PartidaActivity", "Jogador " + j.getPosicao()
				+ " pediu aumento ");
		if (j.getEquipe() == 2 && jogo.getJogadorHumano() != null) {
			Log.d("PartidaActivity", "pedindo para mostrar pergunta aumento");
			mesa.mostrarPerguntaAumento = true;
		}
	}

	public void recusouAumentoAposta(Jogador j) {
		mesa.aguardaFimAnimacoes();
		mesa.diz("aumento_nao", j.getPosicao(), 1300);
	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		mesa.mostrarPerguntaMao11 = false;
		mesa.mostrarPerguntaAumento = false;
		mesa.aguardaFimAnimacoes();
		mesa.atualizaResultadoRodada(numRodada, resultado, jogadorQueTorna);
	}

	public void vez(Jogador j, boolean podeFechada) {
		Log.d("PartidaActivity", "vez do jogador " + j.getPosicao());
		mesa.aguardaFimAnimacoes();
		mostrarBotaoAumento = (j instanceof JogadorHumano)
				&& (valorProximaAposta > 0) && (placar[0] != 11)
				&& (placar[1] != 11);
		mostrarBotaoAbertaFechada = (j instanceof JogadorHumano) && podeFechada;
		mesa.vaiJogarFechada = false;
		mesa
				.setStatusVez(j instanceof JogadorHumano ? MesaView.STATUS_VEZ_HUMANO_OK
						: MesaView.STATUS_VEZ_OUTRO);
	}

}
