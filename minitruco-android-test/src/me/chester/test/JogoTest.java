package me.chester.test;

import junit.framework.TestCase;
import me.chester.Baralho;
import me.chester.Carta;
import me.chester.Estrategia;
import me.chester.Jogador;
import me.chester.JogadorCPU;
import me.chester.Jogo;
import me.chester.JogoLocal;
import me.chester.Mesa;
import me.chester.SituacaoJogo;

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
		j.adiciona(new Mesa());
		j.run();
		// Verifica que um dos dois realmente fez 12 pontos ou mais
		j.atualizaSituacao(situacao, jogador);

		assertTrue("Jogo deveria terminar com alguem ganhando: Jogo: "
				+ situacao, Math.max(situacao.pontosEquipe[0],
				situacao.pontosEquipe[1]) >= 12);

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
}
