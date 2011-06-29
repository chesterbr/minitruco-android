package me.chester.minitruco.android;

import me.chester.minitruco.R;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Interessado;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Jogo;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;

/*
 * Copyright © 2005-2011 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (na sua opnião) qualquer versão.
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.partida);
		mesa = ((MesaView) findViewById(R.id.MesaView01));
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
		jogo = MenuPrincipalActivity.jogo;
		if (jogo != null) {
			if (jogo.jogoFinalizado) {
				// Isso aqui é porque eu ainda não achei um jeito conveniente de
				// processar o rotate, então eu simplesmente mato a atividade se
				// ela voltar com um jogo já finalizado.
				// TODO: consertar
				finish();
			} else {
				jogo.adiciona(this);
				mesa.jogo = jogo;
			}
		} else {
			throw new IllegalStateException(
					"Activity.onCreate: Partida iniciada sem jogo");
		}

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

	public void aceitouAumentoAposta(Jogador j, int valor) {
		mesa.aguardaFimAnimacoes();
		mesa.diz("aumento_sim", j.getPosicao(), 1500);
		mesa.aceitouAumentoAposta(j, valor);
	}

	public void cartaJogada(Jogador j, Carta c) {
		mesa.aguardaFimAnimacoes();
		mesa.descarta(c, j.getPosicao());
		Log.i("Partida", "Jogador " + j.getPosicao() + " jogou " + c);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		if (j.getPosicao() != 1)
			decidiuMao11 = aceita;
		mesa.aguardaFimAnimacoes();
		mesa.mostrarPerguntaMao11 = false;
		mesa.diz(aceita ? "mao11_sim" : "mao11_nao", j.getPosicao(), 1500);
	}

	public void entrouNoJogo(Interessado i, Jogo j) {

	}

	public void informaMao11(Carta[] cartasParceiro) {
		// mesa.aguardaFimAnimacoes();
		if (jogo.getJogadorHumano() != null) {
			mesa.mostraCartasMao11(cartasParceiro);
			if (!decidiuMao11) {
				mesa.mostrarPerguntaMao11 = true;
			}
		}

	}

	private boolean decidiuMao11 = false;

	public void inicioMao() {
		decidiuMao11 = false;
		mesa.aguardaFimAnimacoes();
		for (int i = 0; i <= 2; i++) {
			mesa.resultadoRodada[i] = 0;
		}
		mesa.distribuiMao();
	}

	public void inicioPartida() {

	}

	public void jogoAbortado(int posicao) {

	}

	public void jogoFechado(int numEquipeVencedora) {
		mesa.aguardaFimAnimacoes();
		mesa.diz(numEquipeVencedora == 1 ? "vitoria" : "derrota", 1, 10000);
	}

	public void maoFechada(int[] pontosEquipe) {
		mesa.aguardaFimAnimacoes();
		mesa.atualizaPontosEquipe(pontosEquipe);
		mesa.aguardaFimAnimacoes();
		mesa.recolheMao();

	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		mesa.aguardaFimAnimacoes();
		mesa.diz("aumento_" + valor, j.getPosicao(), 1500 + 200 * (valor / 3));
		if (j.getEquipe() == 2 && jogo.getJogadorHumano() != null) {
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
		mesa.mostrarPerguntaMao11 = false;
		mesa.mostrarPerguntaAumento = false;
		if (j instanceof JogadorHumano) {
			Log.i("Partida", "Partida percebeu que é vez do humano");
		}
		MesaView.setVezHumano(j instanceof JogadorHumano, podeFechada);
	}

	public MesaView getMesa() {
		return mesa;
	}

	private MesaView mesa;

	private Jogo jogo;

}
