package me.chester.minitruco.android;

import me.chester.minitruco.R;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Interessado;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Jogo;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.partida);
		mesa = (MesaView) findViewById(R.id.MesaView01);
		CartaVisual.resources = getResources();
		// Assumindo que o menu principal já adicionou os jogadores ao jogo,
		// inscreve a Mesa como interessado e inicia o jogo em sua própria
		// thread.
		if (MenuPrincipal.jogo != null) {
			MenuPrincipal.jogo.adiciona(this);
		} else {
			Log.w("Activity.onCreate",
					"Partida iniciada sem jogo (ok para testes)");
		}
	}

	public MesaView mesa;

	// /**
	// * Recebe mensagens (da view, principalmente) e executa usando a thread de
	// * UI (senão o Android não deixa).
	// */
	// public final Handler handler = new Handler() {
	// public void handleMessage(Message msg) {
	// MesaView mesa = (MesaView) findViewById(R.id.MesaView01);
	// if (msg.what == WHAT_INVALIDATE) {
	// mesa.invalidate();
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
		MesaView.aguardaFimAnimacoes();
		mesa.recolheMao();

	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		MesaView.aguardaFimAnimacoes();
		Balao.diz("Truco!", j.getPosicao(), 1000 + 200 * (valor / 3));
	}

	public void recusouAumentoAposta(Jogador j) {
		MesaView.aguardaFimAnimacoes();
		Balao.diz("não quero", j.getPosicao(), 800);
	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		print("Rodada " + numRodada + "fechada. Resultado:" + resultado
				+ ". Quem torna: J" + jogadorQueTorna.getPosicao());
		// TODO Auto-generated method stub

	}

	public void vez(Jogador j, boolean podeFechada) {
		// TODO Auto-generated method stub

	}

	// // Mensagens para a thread da UI
	// protected static final int WHAT_INVALIDATE = -1;

}