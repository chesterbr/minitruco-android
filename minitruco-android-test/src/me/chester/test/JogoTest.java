package me.chester.test;

import junit.framework.TestCase;
import me.chester.minitruco.android.CartaVisual;
import me.chester.minitruco.android.Partida;
import me.chester.minitruco.core.Baralho;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Estrategia;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;
import me.chester.minitruco.core.SituacaoJogo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

public class JogoTest extends TestCase {

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

	public void testPartidaGeral() {
		// Cria um jogo com 4 CPUs e roda ele
		Jogo j = new JogoLocal(false, false);
		Jogador jogador = null;
		SituacaoJogo situacao = new SituacaoJogo();
		for (int i = 0; i < 4; i++) {
			jogador = new JogadorCPU(new EstrategiaSequencial());
			j.adiciona(jogador);
		}
		j.adiciona(new Partida());
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

	public void testAnimacaoCarta() throws InterruptedException {
		CartaVisual cv = new CartaVisual(33,66);
		assertEquals(33, cv.x);
		assertEquals(66, cv.y);
		Canvas canvas = new Canvas();
		// Posicionamento simples
		cv.movePara(10, 20);
		assertEquals(10, cv.x);
		assertEquals(20, cv.y);
		// Animação de 0,0 para 300,100 em 3 segundos
		cv.movePara(0, 0);
		cv.movePara(300, 100, 3000);
		Thread.sleep(1000);
		cv.draw(canvas);
		assertTrue("Carta devia andar 100 no x, andou " + cv.x, cv.x >= 100);
		assertTrue("Carta não pode andar além de 200 no X, andou " + cv.x,
				cv.x <= 200);
		assertTrue("Carta tem que andar 33 no Y. andou " + cv.y, cv.y >= 33);
		assertTrue("Carta não pode andar além de 66 no Y, andou " + cv.y,
				cv.y <= 66);
		Thread.sleep(2100);
		cv.draw(canvas);
		assertEquals("Carta tem que chegar ao 300 no X, chegou em " + cv.x,
				cv.x, 300);
		assertEquals("Carta tem que chegar aos 100 no Y, chegou em " + cv.y,
				cv.y, 100);
	}

	public void testDesenhoCarta() {
		int color = Color.MAGENTA;
		Bitmap bitmap = Bitmap.createBitmap(50, 50,
				Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(color);
		assertEquals(color, bitmap.getPixel(10, 10));
		assertEquals(color, bitmap.getPixel(40, 40));
		CartaVisual cv = new CartaVisual(5,5);
		CartaVisual.width = 30;
		CartaVisual.height = 30;
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

	public void testTamanhoCarta() {
		int[][] telas = { { 320, 200 }, { 200, 320 }, { 640, 480 },
				{ 120, 240 }, { 240, 120 } };
		for (int[] tela : telas) {
			int width = tela[0];
			int height = tela[1];
			Bitmap bitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.RGB_565);
			CartaVisual.ajustaTamanho(width, height);
			String result = "Tela " + width + "," + height + " =>  carta "
					+ CartaVisual.width + "," + CartaVisual.height;
			assertTrue(CartaVisual.width > 0);
			assertTrue(CartaVisual.height > 0);
			assertTrue("Tem que caber 6 cartas na largura. " + result,
					CartaVisual.width * 6 <= width);
			assertTrue("Tem que caber 5 cartas na altura. " + result,
					CartaVisual.height * 5 <= height);
		}
	}
}
