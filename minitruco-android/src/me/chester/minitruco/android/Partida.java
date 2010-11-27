package me.chester.minitruco.android;

import me.chester.minitruco.R;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Interessado;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Jogo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Exibe o andamento de um jogo. Futuramente irá permitir ao JogadorHumano
 * associado a ela interagir com o mesmo.
 * 
 * @author chester
 * 
 */
public class Partida extends Activity implements Interessado {

	protected static final int WHAT_TRUCO = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.partida);
		mesa = (MesaView) findViewById(R.id.MesaView01);
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
		// Assumindo que o menu principal já adicionou os jogadores ao jogo,
		// inscreve a Mesa como interessado e inicia o jogo em sua própria
		// thread.
		jogo = MenuPrincipal.jogo;
		if (jogo != null) {
			jogo.adiciona(this);
			MesaView.jogo = jogo;
		} else {
			Log.w("Activity.onCreate",
					"Partida iniciada sem jogo (ok para testes)");
		}

	}

	public MesaView mesa;

	Jogo jogo;

	// /**
	// * Recebe mensagens (da view, principalmente) e executa usando a thread de
	// * UI (senão o Android não deixa).
	// */
	// public final Handler handler = new Handler() {
	// public void handleMessage(Message msg) {
	// // MesaView mesa = (MesaView) findViewById(R.id.MesaView01);
	// if (msg.what == WHAT_TRUCO) {
	//
	// }
	// }
	// };

	public void print(String s) {
		Log.i("Partida.print", s);
	}

	public void aceitouAumentoAposta(Jogador j, int valor) {
		MesaView.aguardaFimAnimacoes();
		Balao.diz("desce", j.getPosicao(), 800);
	}

	public void cartaJogada(Jogador j, Carta c) {
		MesaView.aguardaFimAnimacoes();
		mesa.descarta(c, j.getPosicao());
		// TODO Auto-generated method stub
		print("Jogador " + j.getPosicao() + " jogou " + c);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		// TODO Auto-generated method stub

	}

	public void entrouNoJogo(Interessado i, Jogo j) {
		// TODO Auto-generated method stub

	}

	public void informaMao11(Carta[] cartasParceiro) {
		// TODO Auto-generated method stub

	}

	public void inicioMao() {
		MesaView.aguardaFimAnimacoes();
		for (int i = 0; i <= 2; i++) {
			mesa.resultadoRodada[i] = 0;
		}
		mesa.distribuiMao();
	}

	public void inicioPartida() {
		// TODO Auto-generated method stub

	}

	public void jogoAbortado(int posicao) {
		// TODO Auto-generated method stub

	}

	public void jogoFechado(int numEquipeVencedora) {
		// TODO Auto-generated method stub
		print("Jogo fechado. Equipe vencedora:" + numEquipeVencedora);
	}

	public void maoFechada(int[] pontosEquipe) {
		mesa.placar[0] = pontosEquipe[0];
		mesa.placar[1] = pontosEquipe[1];
		MesaView.aguardaFimAnimacoes();
		mesa.recolheMao();

	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		MesaView.aguardaFimAnimacoes();
		Balao.diz("Truco!", j.getPosicao(), 1000 + 200 * (valor / 3));
		MesaView.aguardaFimAnimacoes();
		final Partida partida = this;
		final JogadorHumano jogadorHumano = jogo.getJogadorHumano();
		if (j.getEquipe() == 2 && jogadorHumano != null) {
			// handler.dispatchMessage(handler.obtainMessage(valor));
			runOnUiThread(new Runnable() {
				public void run() {
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							jogo.respondeAumento(jogadorHumano,
									which == DialogInterface.BUTTON_POSITIVE);

						}
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(
							partida);
					builder.setMessage("Aceita?").setPositiveButton("Sim",
							dialogClickListener).setNegativeButton("Não",
							dialogClickListener).show();
				}

			});
		}
	}

	public void recusouAumentoAposta(Jogador j) {
		MesaView.aguardaFimAnimacoes();
		Balao.diz("não quero", j.getPosicao(), 800);
	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		print("Rodada " + numRodada + "fechada. Resultado:" + resultado
				+ ". Quem torna: J" + jogadorQueTorna.getPosicao());
		mesa.resultadoRodada[numRodada - 1] = resultado;
		// TODO Auto-generated method stub

	}

	public void vez(Jogador j, boolean podeFechada) {
		MesaView.setVezHumano(j instanceof JogadorHumano);
	}

	// // Mensagens para a thread da UI
	// protected static final int WHAT_INVALIDATE = -1;

}
