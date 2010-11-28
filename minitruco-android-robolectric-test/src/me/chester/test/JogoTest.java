package me.chester.test;

import java.util.Date;

import junit.framework.TestCase;
import me.chester.minitruco.android.CartaVisual;
import me.chester.minitruco.android.MesaView;
import me.chester.minitruco.android.Partida;
import me.chester.minitruco.core.Baralho;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Estrategia;
import me.chester.minitruco.core.EstrategiaSellani;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;
import me.chester.minitruco.core.SituacaoJogo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.util.Implements;

@RunWith(RobolectricTestRunner.class)
public class JogoTest extends TestCase {

	// public JogoTest() {
	// super("me.chester.minitruco", Partida.class);
	// }

	// Meta-testes (testam as classes de mock)

	private Partida partida;

	private MesaView mesa;

	private Partida getActivity() {
		return partida;
	};

	@Before
	public void setUp() throws Exception {
		partida = new Partida();
		partida.onCreate(null);
		mesa = partida.getMesa();
		mesa.onSizeChanged(240, 320, 0, 0);
	}

	@Test
	public void testBaralhoOrdenado() {
		String[][] cartas = { { "Kp", "Jo" }, { "Ae" }, { "Kp" } };
		Baralho b = new BaralhoOrdenado(cartas);
		assertEquals("Kp", b.sorteiaCarta().toString());
		assertEquals(new Carta("Jo"), b.sorteiaCarta());
		assertEquals(null, b.sorteiaCarta());
		b.embaralha();
		assertEquals("Ae", b.sorteiaCarta().toString());
		b.embaralha();
		assertEquals("Kp", b.sorteiaCarta().toString());
		// Repete com embaralhadas "em falso"
		b = new BaralhoOrdenado(cartas);
		b.embaralha();
		assertEquals("Kp", b.sorteiaCarta().toString());
		assertEquals(new Carta("Jo"), b.sorteiaCarta());
		assertEquals(null, b.sorteiaCarta());
		b.embaralha();
		b.embaralha();
		b.embaralha();
		assertEquals("Ae", b.sorteiaCarta().toString());
		b.embaralha();
		assertEquals("Kp", b.sorteiaCarta().toString());
	}

	@Test
	public void testEstrategiaSequencial() {
		Carta[] r1 = { new Carta("Ke"), new Carta("Ac"), new Carta("3p") };
		Carta[] r2 = { new Carta("Ac"), new Carta("3p") };
		Carta[] r3 = { new Carta("3p") };
		Estrategia e = new EstrategiaSequencial();
		SituacaoJogo s = new SituacaoJogo();
		s.cartasJogador = r1;
		assertEquals(0, e.joga(s));
		s.cartasJogador = r2;
		assertEquals(0, e.joga(s));
		s.cartasJogador = r3;
		assertEquals(0, e.joga(s));
	}

	// Esses são os testes de verdade

	@Test
	public void testPartidaGeral() {
		// Cria um jogo com 4 CPUs e roda ele
		Jogo j = new JogoLocal(false, false);
		Jogador jogador = null;
		SituacaoJogo situacao = new SituacaoJogo();
		for (int i = 0; i < 4; i++) {
			jogador = new JogadorCPU(new EstrategiaSellani());
			j.adiciona(jogador);
		}
		j.adiciona(this.getActivity());
		// j.run();
		// // Verifica que um dos dois realmente fez 12 pontos ou mais
		// j.atualizaSituacao(situacao, jogador);
		//
		// assertTrue("Jogo deveria terminar com alguem ganhando: Jogo: "
		// + situacao, Math.max(situacao.pontosEquipe[0],
		// situacao.pontosEquipe[1]) >= 12);

		/*
		 * String[][] cartas = { { "Kp", "Jo", "Ap", "2p", "2e", "2o", "Ke",
		 * "Jp", "Ao", "3p", "4e", "5o", "7e" }, { "Qo", "Kp", "Jo", "Ap", "2p",
		 * "2e", "2o", "Ke", "Jp", "Ao", "3p", "4e", "5o", "7e" }, { "Qp", "Kp",
		 * "Jo", "Ap", "2p", "2e", "2o", "Ke", "Jp", "Ao", "3p", "4e", "5o",
		 * "7e" } }; Baralho b = new BaralhoOrdenado(cartas); Jogo j = new
		 * JogoLocal(b, false); Jogador jogador = null; SituacaoJogo situacao =
		 * new SituacaoJogo(); for (int i = 0; i < 4; i++) { jogador = new
		 * JogadorCPU(new EstrategiaSequencial()); j.adiciona(jogador); }
		 * j.run(); j.atualizaSituacao(situacao, jogador); // assertEquals(11,
		 * situacao.pontosEquipe[1]);
		 */
	}

	@Test
	public void testAnimacaoCartaNoTempo() throws InterruptedException {
		CartaVisual.resources = getActivity().getResources();
		CartaVisual cv = new CartaVisual(mesa, 33, 66);
		assertEquals(33, cv.left);
		assertEquals(66, cv.top);
		Canvas canvas = new Canvas();
		// Posicionamento simples
		cv.movePara(10, 20);
		assertEquals(10, cv.left);
		assertEquals(20, cv.top);
		// Animação de 0,0 para 300,100 em 3 segundos
		cv.movePara(0, 0);
		cv.movePara(300, 100, 3000);
		Thread.sleep(1000);
		cv.draw(canvas);
		assertTrue("Carta devia andar 100 no x, andou " + cv.left,
				cv.left >= 100);
		assertTrue("Carta não pode andar além de 200 no X, andou " + cv.left,
				cv.left <= 200);
		assertTrue("Carta tem que andar 33 no Y. andou " + cv.top, cv.top >= 33);
		assertTrue("Carta não pode andar além de 66 no Y, andou " + cv.top,
				cv.top <= 66);
		Thread.sleep(2100);
		cv.draw(canvas);
		assertEquals("Carta tem que chegar ao 300 no X, chegou em " + cv.left,
				cv.left, 300);
		assertEquals("Carta tem que chegar aos 100 no Y, chegou em " + cv.top,
				cv.top, 100);
		// Voltando
		cv.movePara(0, 0, 3000);
		Thread.sleep(1000);
		cv.draw(canvas);
		assertTrue("Carta devia andar -100 no x, esta em " + cv.left,
				cv.left <= 200);
		assertTrue(
				"Carta não pode andar além de -200 no X, esta em " + cv.left,
				cv.left >= 100);
		assertTrue("Carta tem que andar -33 no Y. esta em " + cv.top,
				cv.top <= 67);
		assertTrue("Carta não pode andar além de -66 no Y, esta em " + cv.top,
				cv.top >= 32);
		Thread.sleep(2100);
		cv.draw(canvas);
		assertEquals("Carta tem que chegar ao 0 no X, chegou em " + cv.left,
				cv.left, 0);
		assertEquals("Carta tem que chegar ao 0 no Y, chegou em " + cv.top,
				cv.top, 0);
	}

	// TODO fix this test to run without a real Canvas (maybe by mocking it?)
	public void testCartaDesenhadaNoLugarCerto() {
		CartaVisual.resources = getActivity().getResources();
		int color = Color.MAGENTA;
		Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(color);
		assertEquals(color, bitmap.getPixel(10, 10));
		assertEquals(color, bitmap.getPixel(40, 40));
		CartaVisual cv = new CartaVisual(mesa, 5, 5);
		CartaVisual.largura = 30;
		CartaVisual.altura = 30;
		cv.setCarta(new Carta("Ap"));
		cv.draw(canvas);
		assertFalse(color == bitmap.getPixel(10, 10));
		assertEquals(color, bitmap.getPixel(40, 40));
		canvas.drawColor(color);
		assertEquals(color, bitmap.getPixel(10, 10));
		cv.movePara(20, 20);
		cv.draw(canvas);
		assertEquals(color, bitmap.getPixel(10, 10));
		assertFalse(color == bitmap.getPixel(40, 40));
	}

	@Test
	public void testTamanhoDaCartaAutoAjustadoAoDoCanvas() {
		int[][] telas = { { 320, 200 }, { 200, 320 }, { 640, 480 },
				{ 120, 240 }, { 240, 120 } };
		for (int[] tela : telas) {
			int width = tela[0];
			int height = tela[1];
			CartaVisual.ajustaTamanho(width, height);
			String result = "Tela " + width + "," + height + " =>  carta "
					+ CartaVisual.largura + "," + CartaVisual.altura;
			assertTrue(CartaVisual.largura > 0);
			assertTrue(CartaVisual.altura > 0);
			assertTrue("Tem que caber 6 cartas na largura. " + result,
					CartaVisual.largura * 6 <= width);
			assertTrue("Tem que caber 5 cartas na altura. " + result,
					CartaVisual.altura * 5 <= height);
		}
	}

	// TODO este teste depende da parte visual, tem que ser repensado ou extinto
	public void testTimingBalaoCorreto() throws InterruptedException {
		Log.i("JogoTest", "Inicializa dates p/ nao atrapalhar o timing"
				+ new Date());
		mesa.diz("aumento_3", 1, 1000);
		// Na primeira e segunda vez, algum desenho deve ser feito
		try {
			mesa.draw(null);
			fail("Balao nao desenhou 1o. frame");
		} catch (NullPointerException e) {
		}
		Thread.sleep(200);
		try {
			mesa.draw(null);
			fail("Balao nao desenhou 2o. frame");
		} catch (NullPointerException e) {
		}
		Thread.sleep(800);
		// Nesse ponto, o tempo estourou, não deve ter desenho
		try {
			mesa.draw(null);
		} catch (NullPointerException e) {
			fail("Balao desenhou 3o. frame quando não devia");
		}
	}

	// TODO fazer funcionar
	@Implements(Log.class)
	public static class ShadowLog {
		public static int i(java.lang.String tag, java.lang.String msg) {
			System.out.println("[" + tag + "] " + msg);
			return 0;
		}
	}

}
