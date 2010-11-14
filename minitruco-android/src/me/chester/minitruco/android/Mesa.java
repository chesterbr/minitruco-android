package me.chester.minitruco.android;

import me.chester.minitruco.R;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Interessado;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Jogo;
import android.app.Activity;
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
public class Mesa extends Activity implements Interessado {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Mesa", "create");

		setContentView(R.layout.mesa);
		// Assumindo que o menu principal já adicionou os jogadores ao jogo,
		// inscreve a Mesa como interessado e inicia o jogo em sua própria
		// thread.
		MenuPrincipal.jogo.adiciona(this);
		Thread t = new Thread(MenuPrincipal.jogo);
//		t.start();

	}

	// Define the Handler that receives messages from the thread and update the
	// progress
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
//			TextView textview = (TextView) findViewById(R.id.textview_log);
//			textview.append(msg.getData().getString("texto")+"\n");
		}
	};

	private void print(String s) {
		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putString("texto",s);
		msg.setData(b);
		handler.sendMessage(msg);
	}

	public void aceitouAumentoAposta(Jogador j, int valor) {
		// TODO Auto-generated method stub

	}

	public void cartaJogada(Jogador j, Carta c) {
		// TODO Auto-generated method stub
		print("Jogador " + j.getPosicao() + " jogou " + c );

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

		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		print("Mao fechada. Placar:" + pontosEquipe[0] + "x" + pontosEquipe[1]);

	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		// TODO Auto-generated method stub

	}

	public void recusouAumentoAposta(Jogador j) {
		// TODO Auto-generated method stub

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

}
