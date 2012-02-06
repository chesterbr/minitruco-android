package me.chester.test;

import static org.mockito.Mockito.mock;
import junit.framework.TestCase;
import me.chester.minitruco.android.CartaVisual;
import me.chester.minitruco.android.MesaView;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.JogadorCPU;
import me.chester.minitruco.core.Jogo;
import me.chester.minitruco.core.JogoLocal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.Canvas;
import android.util.Log;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.util.Implements;

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

@RunWith(RobolectricTestRunner.class)
public class JogoTest extends TestCase {

	@Implements(Log.class)
	public static class ShadowLog {
		public static int i(java.lang.String tag, java.lang.String msg) {
			System.out.println("[" + tag + "] " + msg);
			return 0;
		}
	}

	@Before
	public void setUp() throws Exception {
		Robolectric.bindShadowClass(ShadowLog.class);
	}

	@Test
	public void testAnimacaoCartaNoTempo() throws InterruptedException {
		MesaView mesa = mock(MesaView.class);
		CartaVisual cv = new CartaVisual(mesa, 33, 66, null);
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

	@Test
	public void testEqualsEntreCartaECartaVisual() {
		// Arrays contém a versão normal e visual das cartas a testar
		Carta[] cartas = { new Carta("Ap"), new Carta("5o"), new Carta("3p") };
		CartaVisual[] cartasvisuais = { new CartaVisual(null, 0, 0, "Ap"),
				new CartaVisual(null, 0, 0, "5o"),
				new CartaVisual(null, 0, 0, "3p") };
		for (int i = 0; i < cartas.length; i++) {
			for (int j = 0; j < cartasvisuais.length; j++) {
				if (i == j) {
					assertTrue("Carta " + cartas[i]
							+ " devia ser equals a CartaVisual "
							+ cartasvisuais[j],
							cartas[i].equals(cartasvisuais[j]));
					assertTrue("CartaVisual " + cartasvisuais[j]
							+ " devia ser equals a Carta " + cartasvisuais[j],
							cartasvisuais[j].equals(cartas[i]));
				} else {
					assertFalse("Carta " + cartas[i]
							+ " não devia ser equals a CartaVisual "
							+ cartasvisuais[j],
							cartas[i].equals(cartasvisuais[j]));
					assertFalse("CartaVisual " + cartasvisuais[j]
							+ " não devia ser equals a Carta "
							+ cartasvisuais[j],
							cartasvisuais[j].equals(cartas[i]));
				}
			}
		}
	}

	@Test
	public void testJogoSemNenhumAumento() {
		Jogo jogo = new JogoLocal(false, false, false);
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertFalse(jogo.adiciona(new JogadorMock()));
		jogo.run();
	}

	// TODO: externalizar o fator aleatório do JogadorCPU para testes mais
	// consistentes

	@Test
	public void testJogoCom1CPUeOutrosSempreAceitamAumento() {
		Jogo jogo = new JogoLocal(false, false, false);
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorCPU()));
		assertFalse(jogo.adiciona(new JogadorMock()));
		jogo.run();
	}

	@Test
	public void testMaoDe11Com1CPU() {
		JogoLocal jogo = new JogoLocal(false, false, false);
		jogo.setPlacar(0, 11);
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorCPU()));
		assertFalse(jogo.adiciona(new JogadorMock()));
		jogo.run();
		jogo = new JogoLocal(false, false, false);
		jogo.setPlacar(11, 0);
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorCPU()));
		assertFalse(jogo.adiciona(new JogadorMock()));
		jogo.run();
		jogo = new JogoLocal(false, false, false);
		jogo.setPlacar(11, 11);
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorMock()));
		assertTrue(jogo.adiciona(new JogadorCPU()));
		assertFalse(jogo.adiciona(new JogadorMock()));
		jogo.run();
	}

	@Test
	public void testJogoCom4CPU() {
		Jogo jogo = new JogoLocal(false, false, false);
		assertTrue(jogo.adiciona(new JogadorCPU()));
		assertTrue(jogo.adiciona(new JogadorCPU()));
		assertTrue(jogo.adiciona(new JogadorCPU()));
		assertTrue(jogo.adiciona(new JogadorCPU()));
		assertFalse(jogo.adiciona(new JogadorCPU()));
		jogo.run();
	}

}
