package me.chester.minitruco.core;

/*
 * Copyright (c) 2007-2010 Sandro Gasparotto (sandro.gasparoto@gmail.com)
 * Todos os direitos reservados.
 *
 * Nota 1: parte do código (métodos de suporte) adaptada
 * da Estratégia Sellani (Copyright (c) 2006 Leonardo Sellani)
 * Nota 2: lógica estratégica completamente nova
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

import java.util.Random;

/**
 * Estratégia inteligente para jogadores CPU
 * 
 */
public class EstrategiaGasparotto implements Estrategia {
	private static Random rand = new Random();

	int[] C = new int[3];

	private static int LIXO = 0;
	private static int AS = 1;
	private static int DOIS = 2;
	private static int TRES = 3;
	private static int PICAFUMO = 4;
	private static int ESPADILHA = 5;
	private static int ESCOPETA = 6;
	private static int ZAP = 7;

	// game level
	// each game level is associated with various different names
	// so we can create different playing levels and messages for each name...
	// 0: regular play (HAL)
	// 1: professional balanced play [Nero(HAL_PRO)]
	// 2: professional conservative play [Eliza(HAL_PRO)]
	// 3: professional balanced play [Alex(HAL_PRO)]
	// 4: professional agressive play [Constantine(HAL_PRO)]
	private int gameLevel = 0; // default

	private String[] nicks_estrategia = { "HAL", "nero(HAL_PRO)",
			"eliza(HAL_PRO)", "alex(HAL_PRO)", "constantine(HAL_PRO)" };

	private String nick = nicks_estrategia[gameLevel];

	// chat message
	// this for some "psychological" play and entertainment as well
	private String chatMsg = "";

	/**
	 * Retorna o nome "de tela" da Estrategia
	 */
	public String getNomeEstrategia() {
		return "HAL";
	}

	/**
	 * Retorna o nick da Estrategia
	 */
	public String getNickEstrategia() {
		return this.nick;
	}

	/**
	 * Seta o nick da Estrategia
	 */
	public void setNickEstrategia() {
		this.nick = nicks_estrategia[this.gameLevel];
		return;
	}

	/**
	 * Retorna informações de copyright e afins
	 */
	public String getInfoEstrategia() {
		return "v2.1 (c) 2007-2010 Sandro Gasparotto";
	}

	/**
	 * Retorna verdadeiro ou falso a partir de um número randômico e um fator de
	 * dúvida/incerteza. f = 100: sem dúvida vou em frente; f = 50: dúvida
	 * máxima; f = 0: sem dúvida NÃO vou em frente; ou seja, qto maior esse
	 * fator, maior a chance de "mandarmos bala". A idéia é gerar um pouco de
	 * aleatoriedade ao jogo para as jogadas não ficarem evidentes e confundir
	 * os adversários, dando também um toque humano à CPU...
	 */
	private boolean mandaBala(int fatorDeDuvida) {
		// this is where we adjust agressiveness as per the different "personas"
		if (gameLevel == 2)
			fatorDeDuvida = fatorDeDuvida - 15;
		if (gameLevel == 4)
			fatorDeDuvida = fatorDeDuvida + 15;
		if (fatorDeDuvida > 100)
			fatorDeDuvida = 100;
		if (fatorDeDuvida < 0)
			fatorDeDuvida = 0;
		return (rand.nextInt(100) + 1 <= fatorDeDuvida) ? true : false;
	}

	/**
	 * Seta as variáveis C[0],C[1] e C[2] com o índice da maior para menor carta
	 * da minha mão
	 */
	private void classificaCartas(SituacaoJogo s) {
		int i, i2, cAux;

		C[0] = 0;
		C[1] = 1;
		C[2] = 2;

		for (i = 0; i < s.cartasJogador.length; i++) {
			for (i2 = i; i2 < 3; i2++) {
				if (i2 > 0 && i2 >= s.cartasJogador.length)
					C[i2] = C[i2 - 1];
				else if (s.cartasJogador[C[i2]].getValorTruco(s.manilha) >= s.cartasJogador[C[i]]
						.getValorTruco(s.manilha)) {
					cAux = C[i];
					C[i] = C[i2];
					C[i2] = cAux;
				}
			}
		}
	}

	/**
	 * Retorna minha posição na rodada (0..3) (mão..pé)
	 */
	private int minhaPosicao(SituacaoJogo s) {
		int mPos;
		mPos = (eu(s) - (s.posJogadorQueAbriuRodada - 1) + ((eu(s) >= (s.posJogadorQueAbriuRodada - 1)) ? 0
				: 4));
		return mPos;
	}

	/**
	 * Retorna minha posição na mesa (0..3)
	 */
	private int eu(SituacaoJogo s) {
		return s.posJogador - 1;
	}

	/**
	 * Retorna a posicao do meu parceiro na mesa (0..3)
	 */
	private int parceiro(SituacaoJogo s) {
		return ((s.posJogador + 1) % 4);
	}

	/**
	 * Retorna a posicao do adversário 1 na mesa (0..3)
	 */
	private int adversario1(SituacaoJogo s) {
		return ((s.posJogador + 0) % 4);
	}

	/**
	 * Retorna a posicao do adversário 2 na mesa (0..3)
	 */
	private int adversario2(SituacaoJogo s) {
		return ((s.posJogador + 2) % 4);
	}

	/**
	 * Retorna o número da minha vez na rodada (0..3) (mão..pé)
	 */
	private int minhaVez(SituacaoJogo s) {
		return (eu(s) - (s.posJogadorQueAbriuRodada - 1) + ((eu(s) >= (s.posJogadorQueAbriuRodada - 1)) ? 0
				: 4));
	}

	/**
	 * Retorna o número da vez do trucador na rodada (0..3) (mão..pé)
	 */
	private int vezTrucador(SituacaoJogo s) {
		return ((s.posJogadorPedindoAumento - 1)
				- (s.posJogadorQueAbriuRodada - 1) + ((((s.posJogadorPedindoAumento - 1)) >= (s.posJogadorQueAbriuRodada - 1)) ? 0
				: 4));
	}

	/**
	 * Retorna o índice da maior carta da mesa na rodada atual, com preferência
	 * para o índice (pos. do jogador) apontar para nós em caso verdadeiro
	 */
	private int maiorCartaMesa(SituacaoJogo s) {
		int maiorCarta = 0;

		for (int i = 0; i <= 3; i++) {
			if (s.cartasJogadas[s.numRodadaAtual - 1][i] == null)
				continue;
			if (s.cartasJogadas[s.numRodadaAtual - 1][maiorCarta] == null) {
				maiorCarta = i;
				continue;
			}
			if ((eu(s) == i || parceiro(s) == i)
					&& s.cartasJogadas[s.numRodadaAtual - 1][i]
							.getValorTruco(s.manilha) >= s.cartasJogadas[s.numRodadaAtual - 1][maiorCarta]
							.getValorTruco(s.manilha))
				maiorCarta = i;
			else if (s.cartasJogadas[s.numRodadaAtual - 1][i]
					.getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual - 1][maiorCarta]
					.getValorTruco(s.manilha))
				maiorCarta = i;
		}
		return maiorCarta;
	}

	/**
	 * Retorna o índice da maior carta possível da mesa na rodada atual, com
	 * preferência para o índice (pos. do jogador) apontar para nós em caso
	 * verdadeiro
	 */
	private int maiorCartaPossivelMesa(SituacaoJogo s) {
		int maiorCarta = -1;

		boolean m12 = false, m13 = false, m14 = false;

		// procura pelas manilhas que já sairam nas rodadas anteriores
		for (int rodada = 0; rodada < (s.numRodadaAtual - 1); rodada++)
			for (int jogador = 0; jogador <= 3; jogador++) {
				if (s.cartasJogadas[rodada][jogador] != null) {
					switch (s.cartasJogadas[rodada][jogador]
							.getValorTruco(s.manilha)) {
					case 12:
						m12 = true;
						break; // espadilha já saiu
					case 13:
						m13 = true;
						break; // copas já saiu
					case 14:
						m14 = true;
						break; // zap já saiu
					}
				}
			}

		for (int i = 0; i <= 3; i++) {
			if (s.cartasJogadas[s.numRodadaAtual - 1][i] == null)
				continue;
			if ((eu(s) == i || parceiro(s) == i)
					&& s.cartasJogadas[s.numRodadaAtual - 1][i]
							.getValorTruco(s.manilha) == 14)
				maiorCarta = i;
			if ((eu(s) == i || parceiro(s) == i)
					&& m14
					&& s.cartasJogadas[s.numRodadaAtual - 1][i]
							.getValorTruco(s.manilha) == 13)
				maiorCarta = i;
			if ((eu(s) == i || parceiro(s) == i)
					&& m14
					&& m13
					&& s.cartasJogadas[s.numRodadaAtual - 1][i]
							.getValorTruco(s.manilha) == 12)
				maiorCarta = i;
			if ((eu(s) == i || parceiro(s) == i)
					&& m14
					&& m13
					&& m12
					&& s.cartasJogadas[s.numRodadaAtual - 1][i]
							.getValorTruco(s.manilha) == 11)
				maiorCarta = i;
		}

		return maiorCarta;
	}

	/**
	 * Retorna se a maior carta da mesa é a do meu parceiro ou não, e sem
	 * considerar empate
	 */
	private boolean maiorCartaEDoParceiro(SituacaoJogo s) {
		if (parceiro(s) == maiorCartaMesa(s) && !taAmarrado(s))
			return true;
		return false;
	}

	/**
	 * Retorna se a maior carta da mesa é minha ou do meu parceiro ou não
	 */
	private boolean maiorCartaENossaPodendoEstarAmarrado(SituacaoJogo s) {
		if (eu(s) == maiorCartaMesa(s) || parceiro(s) == maiorCartaMesa(s))
			return true;
		return false;
	}

	/**
	 * Retorna se a maior carta da mesa é minha ou do meu parceiro ou não, e sem
	 * considerar empate
	 */
	private boolean maiorCartaENossa(SituacaoJogo s) {
		if ((eu(s) == maiorCartaMesa(s) || parceiro(s) == maiorCartaMesa(s))
				&& !taAmarrado(s))
			return true;
		return false;
	}

	/**
	 * Retorna se a maior carta da mesa é minha ou do meu parceiro ou não
	 */
	private boolean maiorCartaPossivelENossa(SituacaoJogo s) {
		if ((eu(s) == maiorCartaPossivelMesa(s) || parceiro(s) == maiorCartaPossivelMesa(s)))
			return true;
		return false;
	}

	/**
	 * Retorna se eu tenho na mão alguma carta para matar a do adversário
	 */
	private boolean matoAdversario(SituacaoJogo s, boolean consideraEmpate) {
		if (minhaPosicao(s) == 0)
			return true;
		for (int i = 0; i < s.cartasJogador.length; i++) {
			if (s.cartasJogador[i].getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual - 1][maiorCartaMesa(s)]
					.getValorTruco(s.manilha))
				return true;
			if ((s.cartasJogador[i].getValorTruco(s.manilha) >= s.cartasJogadas[s.numRodadaAtual - 1][maiorCartaMesa(s)]
					.getValorTruco(s.manilha)) && consideraEmpate)
				return true;
		}
		return false;
	}

	/**
	 * Retorna o índice da menor carta que tenho na mão para matar a carta do
	 * adversário ou do parceiro na mesa, se não tiver a carta retorna a menor.
	 */
	private int menorCartaParaMatar(SituacaoJogo s) {
		// procura pela primeira carta que mata
		for (int i = (s.cartasJogador.length - 1); i >= 0; i--)
			if (s.cartasJogador[C[i]].getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual - 1][maiorCartaMesa(s)]
					.getValorTruco(s.manilha))
				return C[i]; // é essa!
		// se não encontrou nenhuma, joga a menor
		return C[2];
	}

	private int menorCartaParaMatarNaSegunda(SituacaoJogo s) {
		// procura pela primeira carta que mata
		for (int i = (s.cartasJogador.length - 1); i >= 0; i--)
			if (s.cartasJogador[C[i]].getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual - 1][maiorCartaMesa(s)]
					.getValorTruco(s.manilha))
				return C[i]; // é essa!
		// se não encontrou nenhuma, joga a menor
		return C[1];
	}

	/**
	 * Retorna o índice da menor carta que tenho na mão para matar ou pelo menos
	 * amarrar a carta do adversário ou do parceiro na mesa, se não tiver a
	 * carta retorna a menor.
	 */
	private int menorCartaParaMatarOuAmarrar(SituacaoJogo s) {
		// procura pela primeira carta que mata
		for (int i = (s.cartasJogador.length - 1); i >= 0; i--)
			if (s.cartasJogador[C[i]].getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual - 1][maiorCartaMesa(s)]
					.getValorTruco(s.manilha))
				return C[i]; // é essa!
		// procura pela primeira carta que amarra
		for (int i = (s.cartasJogador.length - 1); i >= 0; i--)
			if (s.cartasJogador[C[i]].getValorTruco(s.manilha) == s.cartasJogadas[s.numRodadaAtual - 1][maiorCartaMesa(s)]
					.getValorTruco(s.manilha))
				return C[i]; // é essa!
		// se não encontrou nenhuma, joga a menor
		return C[2];
	}

	/**
	 * Verifica se eu tenho a maior carta do jogo na mão, (por exemplo, o 7
	 * Copas já tendo saido o Zap), considerando apenas as manilhas.
	 */
	private boolean tenhoMaiorCartaSemContarRodadaAtual(SituacaoJogo s) {
		boolean m12 = false, m13 = false, m14 = false;

		if (s.cartasJogador.length == 0)
			return false;

		// o correto seria checar se todos os 3s já sairam, mas para
		// efeitos práticos vamos considerar que não, então com
		// certeza o nosso 3 não é a maior...
		if (s.cartasJogador[C[0]].getValorTruco(s.manilha) <= 10)
			return false;

		// se eu estiver com o zap, então não tem nem o que pensar
		if (s.cartasJogador[C[0]].getValorTruco(s.manilha) == 14)
			return true;

		// procura pelas manilhas que já sairam nas rodadas anteriores
		for (int rodada = 0; rodada < (s.numRodadaAtual - 1); rodada++)
			for (int jogador = 0; jogador <= 3; jogador++) {
				if (s.cartasJogadas[rodada][jogador] != null) {
					switch (s.cartasJogadas[rodada][jogador]
							.getValorTruco(s.manilha)) {
					case 12:
						m12 = true;
						break; // espadilha já saiu
					case 13:
						m13 = true;
						break; // copas já saiu
					case 14:
						m14 = true;
						break; // zap já saiu
					}
				}
			}
		// checa se estou com a maior manilha do jogo na mão
		if ((s.cartasJogador[C[0]].getValorTruco(s.manilha) == 13 && m14)
				|| (s.cartasJogador[C[0]].getValorTruco(s.manilha) == 12 && m14 && m13)
				|| (s.cartasJogador[C[0]].getValorTruco(s.manilha) == 11 && m14
						&& m13 && m12))
			return true; // ha!!!
		return false;
	}

	/**
	 * Retorna se a partida já esta garantida ou não (por exemplo, se estou com
	 * o Zap e a 1ª feita)
	 */
	private boolean partidaGanha(SituacaoJogo s) {
		if (s.numRodadaAtual == 1 && s.cartasJogador.length >= 2
				&& s.cartasJogador[C[0]].getValorTruco(s.manilha) == 14
				&& s.cartasJogador[C[1]].getValorTruco(s.manilha) == 13)
			return true;
		if (s.numRodadaAtual == 2
				&& (primeiraENossa(s) && tenhoMaiorCartaSemContarRodadaAtual(s) && (qualidadeMinhaMaior(s) > qualidadeMaiorMesa(s) || maiorCartaENossa(s)))
				|| maiorCartaPossivelENossa(s))
			return true;
		if (s.numRodadaAtual == 3
				&& (tenhoMaiorCartaSemContarRodadaAtual(s) && // pos. 0, 1 , 2
				(qualidadeMinhaMaior(s) > qualidadeMaiorMesa(s) || maiorCartaENossa(s)))
				|| // pos. 3
				maiorCartaPossivelENossa(s)) // já jogou
			return true; // fechamos todas as possibilidades considerando todas
							// as posições do jogador na rodada
		return false;
	}

	/**
	 * Retorna se a partida já esta garantida ou não para o adversário (com base
	 * nas cartas já descartadas)
	 */
	private boolean partidaGanhaParaAdversario(SituacaoJogo s) {
		// vamos verificar quais manilhas já sairam
		boolean ZAPJaSaiu = false;
		boolean ESCOPETAJaSaiu = false;
		boolean ESPADILHAJaSaiu = false;
		boolean PICAFUMOJaSaiu = false;
		for (int rodada = 0; rodada <= (s.numRodadaAtual - 1); rodada++)
			for (int jogador = 0; jogador <= 3; jogador++) {
				if (s.cartasJogadas[rodada][jogador] != null) {
					switch (s.cartasJogadas[rodada][jogador]
							.getValorTruco(s.manilha)) {
					case 11:
						PICAFUMOJaSaiu = true;
						break; // PICAFUMO já saiu
					case 12:
						ESPADILHAJaSaiu = true;
						break; // ESPADILHA já saiu
					case 13:
						ESCOPETAJaSaiu = true;
						break; // ESCOPETA já saiu
					case 14:
						ZAPJaSaiu = true;
						break; // ZAP já saiu
					}
				}
			}

		// Análise para 2a rodada
		if (s.numRodadaAtual == 2 && !primeiraENossa(s)
				&& !maiorCartaENossaPodendoEstarAmarrado(s)) {
			if (qualidadeMaiorMesa(s) == ZAP)
				return true;

			if (qualidadeMaiorMesa(s) == ESCOPETA && ZAPJaSaiu)
				return true;

			if (qualidadeMaiorMesa(s) == ESPADILHA && ZAPJaSaiu
					&& ESCOPETAJaSaiu)
				return true;

			if (qualidadeMaiorMesa(s) == PICAFUMO && ZAPJaSaiu
					&& ESCOPETAJaSaiu && ESPADILHAJaSaiu)
				return true;

			if (qualidadeMaiorMesa(s) == TRES && ZAPJaSaiu && ESCOPETAJaSaiu
					&& ESPADILHAJaSaiu && PICAFUMOJaSaiu)
				return true;
		}

		// Análise para 3a rodada
		if (s.numRodadaAtual == 3 && !maiorCartaENossaPodendoEstarAmarrado(s)) {
			if (qualidadeMaiorMesa(s) == ZAP)
				return true;

			if (qualidadeMaiorMesa(s) == ESCOPETA && ZAPJaSaiu)
				return true;

			if (qualidadeMaiorMesa(s) == ESPADILHA && ZAPJaSaiu
					&& ESCOPETAJaSaiu)
				return true;

			if (qualidadeMaiorMesa(s) == PICAFUMO && ZAPJaSaiu
					&& ESCOPETAJaSaiu && ESPADILHAJaSaiu)
				return true;

			if (qualidadeMaiorMesa(s) == TRES && ZAPJaSaiu && ESCOPETAJaSaiu
					&& ESPADILHAJaSaiu && PICAFUMOJaSaiu && !primeiraENossa(s))
				return true;

			// o correto seria checar se todos os 3s e manilhas sairam também,
			// mas isso seria (quase que) impossível, e também não significaria
			// muito na prática...
		}

		return false;
	}

	/**
	 * Retorna se a mesa está amarrada
	 */
	private boolean taAmarrado(SituacaoJogo s) {
		if (s.cartasJogadas[s.numRodadaAtual - 1][adversario1(s)] != null
				&& s.cartasJogadas[s.numRodadaAtual - 1][parceiro(s)] != null
				&& s.cartasJogadas[s.numRodadaAtual - 1][parceiro(s)]
						.getValorTruco(s.manilha) == s.cartasJogadas[s.numRodadaAtual - 1][adversario1(s)]
						.getValorTruco(s.manilha))
			return true;
		if (s.cartasJogadas[s.numRodadaAtual - 1][adversario2(s)] != null
				&& s.cartasJogadas[s.numRodadaAtual - 1][parceiro(s)] != null
				&& s.cartasJogadas[s.numRodadaAtual - 1][parceiro(s)]
						.getValorTruco(s.manilha) == s.cartasJogadas[s.numRodadaAtual - 1][adversario2(s)]
						.getValorTruco(s.manilha))
			return true;
		if (s.cartasJogadas[s.numRodadaAtual - 1][adversario1(s)] != null
				&& s.cartasJogadas[s.numRodadaAtual - 1][eu(s)] != null
				&& s.cartasJogadas[s.numRodadaAtual - 1][eu(s)]
						.getValorTruco(s.manilha) == s.cartasJogadas[s.numRodadaAtual - 1][adversario1(s)]
						.getValorTruco(s.manilha))
			return true;
		if (s.cartasJogadas[s.numRodadaAtual - 1][adversario2(s)] != null
				&& s.cartasJogadas[s.numRodadaAtual - 1][eu(s)] != null
				&& s.cartasJogadas[s.numRodadaAtual - 1][eu(s)]
						.getValorTruco(s.manilha) == s.cartasJogadas[s.numRodadaAtual - 1][adversario2(s)]
						.getValorTruco(s.manilha))
			return true;
		return false;

	}

	/**
	 * Retorna a exata qualificação de uma carta Talvez seja necessária alguma
	 * adaptação para o caso de "baralho limpo"
	 */
	private int qualidadeCarta(Carta carta, SituacaoJogo s) {
		// Declarações
		int qcarta; // return do método
		qcarta = LIXO; // apenas para ter a variável inicializada com algo
		// 4, 5, 6, 7, Q, J, K
		if (carta.getValorTruco(s.manilha) <= 7)
			qcarta = LIXO;
		// A
		else if (carta.getValorTruco(s.manilha) == 8)
			qcarta = AS;
		// 2
		else if (carta.getValorTruco(s.manilha) == 9)
			qcarta = DOIS;
		// 3
		else if (carta.getValorTruco(s.manilha) == 10)
			qcarta = TRES;
		// Picafumo
		else if (carta.getValorTruco(s.manilha) == 11)
			qcarta = PICAFUMO;
		// Espadilha
		else if (carta.getValorTruco(s.manilha) == 12)
			qcarta = ESPADILHA;
		// Escopeta
		else if (carta.getValorTruco(s.manilha) == 13)
			qcarta = ESCOPETA;
		// Zap
		else if (carta.getValorTruco(s.manilha) == 14)
			qcarta = ZAP;

		// caso a manilha seja um 3, o 2 passa a ter qualidade de 3!
		// e o A passa a ter valor de 2!
		// Obs: logicamente isso não se aplica para o caso de manilha velha...

		// Workaround devido ao fato da propriedade 'manilha' não poder ser
		// utilizada diretamente
		// Estas são cartas fictícias somente para os testes condicionais logo
		// abaixo
		Carta tres_testedemanilha = new Carta('3', 3);
		Carta dois_testedemanilha = new Carta('2', 3);
		if (qcarta == DOIS
				&& tres_testedemanilha.getValorTruco(s.manilha) == 14)
			qcarta = TRES;
		if (qcarta == AS && tres_testedemanilha.getValorTruco(s.manilha) == 14)
			qcarta = DOIS;
		// caso a manilha seja um 2, o A passa a ter valor de 2!
		if (qcarta == AS && dois_testedemanilha.getValorTruco(s.manilha) == 14)
			qcarta = DOIS;

		return qcarta;
	}

	/**
	 * Retorna a qualificação da maior carta da mesa na rodada atual
	 */
	private int qualidadeMaiorMesa(SituacaoJogo s) {
		if (s.cartasJogadas[s.numRodadaAtual - 1][maiorCartaMesa(s)] == null)
			return 0;
		return qualidadeCarta(
				s.cartasJogadas[s.numRodadaAtual - 1][maiorCartaMesa(s)], s);
	}

	/**
	 * Retorna a qualificação da maior carta da minha mão
	 */
	private int qualidadeMinhaMaior(SituacaoJogo s) {
		try {
			if (s.cartasJogador[C[0]] == null)
				return 0;
		} catch (Exception e) {
			return 0;
		}
		return qualidadeCarta(s.cartasJogador[C[0]], s);
	}

	/**
	 * Retorna se pode/compensa aumentar a aposta ou não, com input para o fator
	 * sorte. Jogador fica mais agressivo/cauteloso caso estejamos
	 * perdendo/vencendo...
	 */
	private boolean podeEValeAPenaAumentar(SituacaoJogo s, int fator3,
			int fator6, int fator9, int fator12) {
		// o valor da aposta vai ser maior do que eu preciso pra fechar o jogo?
		// não necessário...
		// if(12-s.pontosEquipe[eu(s)%2] < s.valorProximaAposta)
		// return false;

		// diminui agressividade para aumentar aposta caso estejamos com 9 ou
		// mais pontos
		if (s.pontosEquipe[eu(s) % 2] >= 9 && fator3 != 100) {
			fator3 = fator3 - 20; // 20% de diminuição na probabilidade...
		}
		if (s.pontosEquipe[eu(s) % 2] >= 9 && fator6 != 100) {
			fator6 = fator6 - 20;
		}
		if (s.pontosEquipe[eu(s) % 2] >= 9 && fator9 != 100) {
			fator9 = fator9 - 10;
		}
		if (s.pontosEquipe[eu(s) % 2] >= 9 && fator12 != 100) {
			fator12 = fator12 - 0;
		}

		// aumenta agressividade em caso de grande desvantagem no placar
		if ((s.pontosEquipe[adversario1(s) % 2] - s.pontosEquipe[eu(s) % 2]) >= 6
				&& fator3 != 100) {
			fator3 = fator3 + 20; // 20% de aumento na probabilidade...
		}
		if ((s.pontosEquipe[adversario1(s) % 2] - s.pontosEquipe[eu(s) % 2]) >= 6
				&& fator6 != 100) {
			fator6 = fator6 + 20;
		}
		if ((s.pontosEquipe[adversario1(s) % 2] - s.pontosEquipe[eu(s) % 2]) >= 6
				&& fator9 != 100) {
			fator9 = fator9 + 10;
		}
		if ((s.pontosEquipe[adversario1(s) % 2] - s.pontosEquipe[eu(s) % 2]) >= 6
				&& fator12 != 100) {
			fator12 = fator12 + 5;
		}

		// diminui agressividade em caso de grande vantagem no placar
		if ((s.pontosEquipe[eu(s) % 2] - s.pontosEquipe[adversario1(s) % 2]) >= 6
				&& fator3 != 100) {
			fator3 = fator3 - 20; // 20% de diminuição na probabilidade...
		}
		if ((s.pontosEquipe[eu(s) % 2] - s.pontosEquipe[adversario1(s) % 2]) >= 6
				&& fator6 != 100) {
			fator6 = fator6 - 10;
		}
		if ((s.pontosEquipe[eu(s) % 2] - s.pontosEquipe[adversario1(s) % 2]) >= 6
				&& fator9 != 100) {
			fator9 = fator9 - 10;
		}
		if ((s.pontosEquipe[eu(s) % 2] - s.pontosEquipe[adversario1(s) % 2]) >= 6
				&& fator12 != 100) {
			fator12 = fator12 - 5;
		}

		if (s.valorProximaAposta == 3 && mandaBala(fator3))
			return true;
		if (s.valorProximaAposta == 6 && mandaBala(fator6))
			return true;
		if (s.valorProximaAposta == 9 && mandaBala(fator9))
			return true;
		if (s.valorProximaAposta == 12 && mandaBala(fator12))
			return true;
		return false;
	}

	/**
	 * Retorna se compensa aceitar o aumento a aposta ou não, com input para o
	 * fator sorte. Jogador fica mais agressivo/cauteloso caso estejamos
	 * perdendo/vencendo... Se adversário estiver com 10 e nós com 5 ou menos,
	 * aceitar! (ia ficar difícil ganhar mesmo...)
	 */
	private boolean valeAPenaAceitar(SituacaoJogo s, int fatorF) {
		if (((s.pontosEquipe[adversario1(s) % 2] - s.pontosEquipe[eu(s) % 2]) >= 6)
				&& (fatorF != 0)) {
			fatorF = fatorF + 25; // 25% de aumento na probabilidade...
		}
		if (((s.pontosEquipe[eu(s) % 2] - s.pontosEquipe[adversario1(s) % 2]) >= 6)
				&& (fatorF != 100)) {
			fatorF = fatorF - 25; // 25% de diminuição na probabilidade...
		}
		if ((s.pontosEquipe[adversario1(s) % 2] == 10
				&& s.pontosEquipe[eu(s) % 2] <= 5 && podeEValeAPenaAumentar(s,
					100, 100, 100, 100)) && (fatorF > 0)) {
			fatorF = 100;
		}
		// o jogo já vai terminar de qualquer forma?
		if (s.valorProximaAposta == 6 && s.pontosEquipe[eu(s) % 2] >= 9)
			fatorF = 100;
		if (s.valorProximaAposta == 9 && s.pontosEquipe[eu(s) % 2] >= 6)
			fatorF = 100;
		if (s.valorProximaAposta == 12 && s.pontosEquipe[eu(s) % 2] >= 3)
			fatorF = 100;
		if (fatorF > 100)
			fatorF = 100;
		if (fatorF < 0)
			fatorF = 0;
		return (rand.nextInt(100) + 1 <= fatorF) ? true : false;
	}

	/**
	 * Retorna se a primeira rodada foi feita por mim ou pelo parceiro
	 */
	private boolean primeiraENossa(SituacaoJogo s) {
		if (s.numRodadaAtual == 0)
			return false;
		int res = s.resultadoRodada[0];
		int pj = s.posJogador;
		if (res == 1 && (pj == 1 || pj == 3))
			return true;
		if (res == 2 && (pj == 2 || pj == 4))
			return true;
		return false;
	}

	/**
	 * Retorna se a rodada amarrou ou não; numRod = 0 para a 1a, 1 para a 2a
	 */
	private boolean rodadaAmarrou(SituacaoJogo s, int numRod) {
		if (s.numRodadaAtual == 0)
			return false;
		if (s.resultadoRodada[numRod] == 3)
			return true;
		return false;
	}

	private int joga_primeira_rodada_mao(SituacaoJogo s) {
		// Esta é a pior posição da mesa
		// E para piorar ainda mais, não temos como saber o que o parceiro
		// tem...
		// Temos que inventar um sistema de sinal cibernético...
		// Então, pelos ensinamentos de meu avô, vamos tentar garantir a
		// primeira
		// Mas somente vale a pena se for o picafumo, espadilha ou escopeta
		// Um 3 morre fácil, e pode ferrar o jogo do parceiro,
		// a não ser que temos o zap também
		// E o zap é sempre melhor guardar neste caso...

		// Jogo.log("Checkpoint");

		// truco sem ver...
		if (s.pontosEquipe[adversario1(s) % 2] < 6
				&& s.pontosEquipe[eu(s) % 2] < 6
				&& podeEValeAPenaAumentar(s, 100, 100, 100, 100)) {
			if (gameLevel != 0 && mandaBala(5)) {
				// frases
				if (mandaBala(80)) {
					String[] m = new String[] { "Trucando sem ver...",
							"Trucando sem ver as cartas!", "Truco de primeira!" };
					int i = Math.abs(rand.nextInt()) % 2 + 1;
					chatMsg = m[i];
				}
				return -1;
			}
		}

		if (mandaBala(75)) {
			// Checa se temos zap e três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == TRES)
				// Jogar o três
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outro três),
			// mesmo assim vamos jogar o três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == TRES)
				// Jogar o três
				return C[2];
			// Checa se temos o picafumo, espadilha ou escopeta
			// Mas somente jogar caso não tenhamos o zap também
			// Isto quer dizer que uma dessas tem que ser a maior carta na
			// mão...
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == PICAFUMO
					|| qualidadeCarta(s.cartasJogador[C[0]], s) == ESPADILHA
					|| qualidadeCarta(s.cartasJogador[C[0]], s) == ESCOPETA)
				// Jogar então
				return C[0];
			return C[2]; // retorna a menor então
		} else {
			// frases
			if (mandaBala(40)) {
				String[] m = new String[] {
						"N&atilde;o tenho absolutamente nada!",
						"Maravilha, estou com tudo!",
						"Vamos come&ccedil;ar de leve..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			// jogar a menor carta
			return C[2];
		}
	}

	private int joga_primeira_rodada_posmao(SituacaoJogo s) {
		// Caso esteja vindo manilha:
		// Tentar matar a qualquer custo
		// Caso esteja vindo um três:
		// Se temos zap e três, jogar o três
		// Senão tentar matar a qualquer custo
		// Caso esteja vindo um dois:
		// Caso tenhamos zap e três, jogar o três
		// Caso tenhamos zap e dois, jogar o dois
		// Senão tentar matar com a menor manilha (menos o zap) ou um três pelo
		// menos
		// Caso esteja vindo um ás:
		// Caso tenhamos zap e três, jogar o três
		// Caso tenhamos zap e dois, jogar o dois
		// Caso tenhamos zap e ás, jogar o ás
		// Senão tentar matar com a menor manilha (menos o zap) ou um três ou
		// dois pelo menos
		// Senão deixar para o parceiro, ele deve se virar
		// Caso esteja vindo lixo:
		// Jogar a menor manilha (menos o zap)
		// Ou um três somente caso tenhamos o zap
		// Senão deixar para o parceiro, ele deve se virar (c/ média
		// probabilidade, afinal, ele é pé)

		if (qualidadeMaiorMesa(s) >= PICAFUMO)
			return menorCartaParaMatar(s);
		if (qualidadeMaiorMesa(s) == TRES) {
			// Checa se temos zap e três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == TRES)
				// Jogar o três
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outro três),
			// mesmo assim vamos jogar o três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == TRES)
				// Jogar o três
				return C[2];
			return menorCartaParaMatar(s);
		}
		if (qualidadeMaiorMesa(s) == DOIS && mandaBala(90)) {
			// Checa se temos zap e três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == TRES)
				// Jogar o três
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outro três),
			// mesmo assim vamos jogar o três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == TRES)
				// Jogar o três
				return C[2];
			// Checa se temos zap e dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == DOIS)
				// Jogar o dois
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outros
			// três/dois),
			// mesmo assim vamos jogar o dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == DOIS)
				// Jogar o dois
				return C[2];
			// Aqui vamos jogar a menor manilha (menos o zap)
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == PICAFUMO)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == PICAFUMO)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == PICAFUMO)
				return C[2];
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESPADILHA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESPADILHA)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar
								// enganar os adversários...
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESCOPETA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESCOPETA)
				return C[0]; // hummmm.. podemos jogar o zap para tentar enganar
								// os adversários...
			// Aqui vamos jogar o três ou dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == DOIS)
				return C[0]; // ok se amarrar
			return C[2]; // retorna a menor então
		}

		if (qualidadeMaiorMesa(s) == AS && mandaBala(80)) {
			// Checa se temos zap e três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == TRES)
				// Jogar o três
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outro três),
			// mesmo assim vamos jogar o três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == TRES)
				// Jogar o três
				return C[2];
			// Checa se temos zap e dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == DOIS)
				// Jogar o dois
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outros
			// três/dois),
			// mesmo assim vamos jogar o dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == DOIS)
				// Jogar o dois
				return C[2];
			// Checa se temos zap e ás
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == AS)
				// Jogar o ás
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outos
			// três/dois/ás),
			// mesmo assim vamos jogar o ás
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == AS)
				// Jogar o ás
				return C[2];
			// Aqui vamos jogar a menor manilha (menos o zap)
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == PICAFUMO)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == PICAFUMO)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == PICAFUMO)
				return C[2];
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESPADILHA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESPADILHA)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar
								// enganar os adversários...
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESCOPETA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESCOPETA)
				return C[0]; // hummmm... podemos jogar o zap para tentar
								// enganar os adversários...
			// Aqui vamos jogar o três ou dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == DOIS)
				return C[0];
			return C[2]; // retorna a menor então
		}

		if (qualidadeMaiorMesa(s) == LIXO && mandaBala(60)) {
			// Aqui vamos jogar a menor manilha (menos o zap)
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == PICAFUMO)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == PICAFUMO)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == PICAFUMO)
				return C[2];
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESPADILHA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESPADILHA)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar
								// enganar os adversários...
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESCOPETA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESCOPETA)
				return C[0]; // hummmm... podemos jogar o zap para tentar
								// enganar os adversários...
			// Checa se temos zap e três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == TRES)
				// Jogar o três
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outro três),
			// mesmo assim vamos jogar o três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == TRES)
				// Jogar o três
				return C[2];
			return C[2]; // retorna a menor então
		}
		// frases
		if (mandaBala(25)) {
			String[] m = new String[] { "Deixando para o p&eacute;...",
					"Vamos deixar para o p&eacute;!...",
					"Melhor guardar minha carta e deixar para o p&eacute;..." };
			int i = Math.abs(rand.nextInt()) % 2 + 1;
			chatMsg = m[i];
		}
		return C[2]; // retorna a menor então
	}

	private int joga_primeira_rodada_parape(SituacaoJogo s) {
		// 1) Caso a maior carta da mesa seja do parceiro:
		// Se for maior ou igual que um ás, ok, deixar passar
		// (apesar que aqui o correto seria "sinalizar" para o parceiro
		// caso tenhamos uma manilha (menos o zap) para tentar fazer
		// e não dar chance ao pé de amarrar...
		// assim como outros casos... mas tudo bem...
		// Se for lixo (menor que ás), vamos reforçar.
		// 2) Caso a maior que esteja vindo não seja a do parceiro:
		// Caso esteja vindo manilha:
		// Tentar primeiro matar com o zap, e depois
		// tentar matar a qualquer custo com a menor manilha
		// Caso esteja vindo um três:
		// Se temos zap e três, jogar o três
		// Senão tentar matar a qualquer custo ou amarrar pelo menos
		// Caso esteja vindo um dois:
		// Caso tenhamos zap e dois, jogar o dois
		// Caso tenhamos zap e três, jogar o três
		// Senão tentar matar com a menor manilha, um três ou pelo menos amarrar
		// Caso esteja vindo um ás/lixo:
		// Caso tenhamos zap e ás, jogar o ás
		// Caso tenhamos zap e dois, jogar o dois
		// Caso tenhamos zap e três, jogar o três
		// Senão tentar matar com a menor manilha, um três/dois/ás/... ou pelo
		// menos amarrar

		if (maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) >= AS)
			return C[2];

		// A maior carta que está vindo é do adversário portanto:

		if (qualidadeMaiorMesa(s) >= PICAFUMO) {
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP)
				return C[0];
			return menorCartaParaMatar(s);
		}
		if (qualidadeMaiorMesa(s) == TRES) {
			// Checa se temos zap e três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == TRES)
				// Jogar o três
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outro três),
			// mesmo assim vamos jogar o três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == TRES)
				// Jogar o três
				return C[2];
			// Aqui jogamos a melhor que temos então desde que não seja menor
			// que a que está já na mesa
			// Vamos tentar fazer com a maior ou pelo menos amarrar
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s))
				return C[0];
			return C[2];
		}
		if (qualidadeMaiorMesa(s) == DOIS) {
			// Checa se temos zap e três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == TRES)
				// Jogar o três
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outro três),
			// mesmo assim vamos jogar o três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == TRES)
				// Jogar o três
				return C[2];
			// Checa se temos zap e dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == DOIS)
				// Jogar o dois
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outros
			// três/dois),
			// mesmo assim vamos jogar o dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == DOIS)
				// Jogar o dois
				return C[2];
			// Aqui vamos primeiro tentar matar com a menor manilha
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == PICAFUMO)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == PICAFUMO)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == PICAFUMO)
				return C[2];
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESPADILHA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESPADILHA)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == ESPADILHA)
				return C[0]; // jogar o zap para tentar ludibriar os
								// adversários...
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESCOPETA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESCOPETA)
				if (mandaBala(50))
					return C[0]; // hummmm... podemos jogar o zap para tentar
									// ludibriar os adversários...
				else
					return C[2]; // ou fazer "cama"...
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP)
				return C[0];
			// Aqui jogamos a melhor que temos então desde que não seja menor
			// que a que está já na mesa
			// Vamos tentar fazer com a maior ou pelo menos amarrar
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s))
				return C[0];
			return C[2];
		}
		if (qualidadeMaiorMesa(s) <= AS) {
			// frases
			if (mandaBala(5)) {
				String[] m = new String[] {
						"Essa rodada vai ficar interessante!", "Hummmm....",
						"Vamos ver o que vai acontecer daqui para a frente..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			// Checa se temos zap e três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == TRES)
				// Jogar o três
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outro três),
			// mesmo assim vamos jogar o três
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == TRES)
				// Jogar o três
				return C[2];
			// Checa se temos zap e dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == DOIS)
				// Jogar o dois
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outros
			// três/dois),
			// mesmo assim vamos jogar o dois
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == DOIS)
				// Jogar o dois
				return C[2];
			// Checa se temos zap e ás
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == AS)
				// Jogar o ás
				return C[1];
			// Vai que temos uma manilha também (C[1]) (ou mesmo outros
			// três/dois/ás),
			// mesmo assim vamos jogar o ás
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& qualidadeCarta(s.cartasJogador[C[2]], s) == AS)
				// Jogar o ás
				return C[2];
			// Aqui vamos jogar a menor manilha
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == PICAFUMO)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == PICAFUMO)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == PICAFUMO)
				return C[2];
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESPADILHA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESPADILHA)
				return C[1];
			if (qualidadeCarta(s.cartasJogador[C[2]], s) == ESPADILHA)
				return C[0]; // jogar o zap para tentar ludibriar os
								// adversários...
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ESCOPETA)
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[1]], s) == ESCOPETA)
				if (mandaBala(50))
					return C[0]; // hummmm... podemos jogar o zap para tentar
									// ludibriar os adversários...
				else
					return C[2]; // ou fazer "cama"...
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP)
				return C[1]; // carta intermediária para tentar fazer a
								// primeira...
			// Aqui jogamos a melhor que temos então desde que não seja menor
			// que a que está já na mesa
			// Vamos tentar fazer com a maior ou pelo menos amarrar
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s))
				return C[0];
			return C[2];
		}
		return C[0]; // para satisfazer o corretor do método (Eclipse)...
	}

	private int joga_primeira_rodada_pe(SituacaoJogo s) {
		// Aqui é fácil, tudo que o pé tem a fazer é garantir a primeira,
		// ou pelo menos tentar amarrar.
		// Se já estiver amarrado, manter assim se tiver o zap.

		if (maiorCartaEDoParceiro(s)) {
			// frases
			if (mandaBala(10)) {
				String[] m = new String[] { "Boa parceiro!",
						"&Eacute; assim que se faz, parceiro!",
						"Agora pode relaxar e deixar a segunda comigo, parceiro!" };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return C[2];
		}
		if (taAmarrado(s) && qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP)
			return C[2];
		// emoção ao jogo
		if (s.pontosEquipe[adversario1(s) % 2] < 6
				&& s.pontosEquipe[eu(s) % 2] < 6
				&& podeEValeAPenaAumentar(s, 100, 100, 100, 100)) {
			if (gameLevel != 0 && mandaBala(5)) {
				return -1;
			}
		}
		return menorCartaParaMatarOuAmarrar(s);
	}

	private int joga_segunda_rodada_mao(SituacaoJogo s) {
		// Fiz a primeira
		// Se estiver com a partida ganha, vou encobrir a menor!
		// Se estiver com duas cartas acima ou igual a um três, jogar a menor
		// aumentando a aposta (com alta probabilidade).
		// Se estiver com duas cartas acima ou igual a um ás, jogar a menor.
		// Se estiver com uma carta acima ou igual a um ás e um lixo, jogo o
		// lixo encoberto (com média-baixa probabilidade).
		// Se estiver com dois lixos, jogo o lixo mais lixo encoberto (com baixa
		// probabilidade).

		// frases
		if (mandaBala(5)) {
			String[] m = new String[] { "Quem pode mais chora menos...",
					"Agora acabaram minhas cartas...",
					"Gastei tudo na primeira rsrsrs..." };
			int i = Math.abs(rand.nextInt()) % 2 + 1;
			chatMsg = m[i];
		}
		// Se a 1a amarrou, jogar a maior
		if (rodadaAmarrou(s, 0)) {
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& podeEValeAPenaAumentar(s, 100, 100, 100, 100))
				return -1;
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= PICAFUMO
					&& podeEValeAPenaAumentar(s, 30, 10, 3, 1))
				return -1;
			return C[0];
		}

		if (partidaGanha(s)) {
			// "cama": jogar encoberto para não fechar o adversário
			if (qualidadeCarta(s.cartasJogador[C[1]], s) >= AS)
				return C[1] + 10;
			// para não ficar muito evidente toda hora a mesma jogada:
			if (mandaBala(50))
				return C[1] + 10;
			return C[1];
		}

		if (qualidadeCarta(s.cartasJogador[C[0]], s) >= TRES
				&& qualidadeCarta(s.cartasJogador[C[1]], s) >= TRES) {
			if (podeEValeAPenaAumentar(s, 80, 10, 1, 0))
				return -1;
			return C[1];
		}

		if (qualidadeCarta(s.cartasJogador[C[0]], s) >= AS
				&& qualidadeCarta(s.cartasJogador[C[1]], s) >= AS) {
			return C[1];
		}

		if (qualidadeCarta(s.cartasJogador[C[0]], s) >= AS
				&& qualidadeCarta(s.cartasJogador[C[1]], s) == LIXO) {
			if (mandaBala(25))
				return C[1] + 10;
			return C[1];
		}

		if (qualidadeCarta(s.cartasJogador[C[0]], s) == LIXO
				&& qualidadeCarta(s.cartasJogador[C[1]], s) == LIXO) {
			if (mandaBala(10))
				return C[1] + 10;
			return C[1];
		}
		return C[0]; // para satisfazer o corretor do método (Eclipse)...
	}

	private int joga_segunda_rodada_posmao(SituacaoJogo s) {
		// A primeira é deles
		// Assim fica difícil
		// Vou deixar para o parceiro

		// Se a 1a amarrou, jogar a maior
		if (rodadaAmarrou(s, 0)) {
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& podeEValeAPenaAumentar(s, 100, 100, 100, 100))
				return -1;
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= PICAFUMO
					&& qualidadeCarta(s.cartasJogador[C[0]], s) > qualidadeMaiorMesa(s)
					&& podeEValeAPenaAumentar(s, 50, 15, 5, 1))
				return -1;
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s))
				return C[0];
			return C[1];
		}

		// frases
		if (mandaBala(5)) {
			String[] m = new String[] {
					"Parceiro, &eacute; com voc&ecirc; agora...",
					"A gente n&atilde;o conseguiu fazer a primeira, assim fica dif&iacute;cil...",
					"Firme parceiro, ainda h&aacute; esperan&ccedil;a..." };
			int i = Math.abs(rand.nextInt()) % 2 + 1;
			chatMsg = m[i];
		}
		return C[1];
	}

	private int joga_segunda_rodada_parape(SituacaoJogo s) {
		// O parceiro fez a primeira
		// Se estiver com a partida ganha, vou jogar quieto a menor!
		// Caso contrário, vou sempre jogar a maior no pé.
		// A não ser que esteja vindo carta boa do parceiro...
		// Se estiver com uma carta acima ou igual a um ás, jogar aumentando a
		// aposta (com alta probabilidade).
		// Se estiver com lixos, jogar aumentando a aposta (com baixa
		// probabilidade - facão).

		// Se a 1a amarrou, jogar a maior
		if (rodadaAmarrou(s, 0)) {
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
					&& podeEValeAPenaAumentar(s, 100, 100, 100, 100))
				return -1;
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= PICAFUMO
					&& qualidadeCarta(s.cartasJogador[C[0]], s) > qualidadeMaiorMesa(s)
					&& podeEValeAPenaAumentar(s, 50, 15, 5, 1))
				return -1;
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s)
					&& parceiro(s) != maiorCartaMesa(s))
				return C[0];
			if (qualidadeCarta(s.cartasJogador[C[0]], s) > qualidadeMaiorMesa(s)
					&& parceiro(s) == maiorCartaMesa(s))
				return C[0];
			return C[1];
		}

		if (partidaGanha(s)) {
			// "cama": jogar encoberto para não fechar o pé
			if (qualidadeCarta(s.cartasJogador[C[1]], s) >= AS)
				return C[1] + 10;
			// para não ficar muito evidente toda hora a mesma jogada:
			if (mandaBala(50))
				return C[1] + 10;
			return C[1];
		}

		if (maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) >= TRES)
			return C[1] + 10;

		if (qualidadeCarta(s.cartasJogador[C[0]], s) >= TRES
				&& qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s)) {
			if (podeEValeAPenaAumentar(s, 90, 30, 1, 0)
					&& !partidaGanhaParaAdversario(s)) {
				// frases
				if (mandaBala(15)) {
					String[] m = new String[] { "E agora José?",
							"Fechando o pé mais uma vez...",
							"O pé não é de nada!" };
					int i = Math.abs(rand.nextInt()) % 2 + 1;
					chatMsg = m[i];
				}

				return -1;
			}
			return C[0];
		}
		if (maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) == DOIS)
			return C[1] + 10;
		if (qualidadeCarta(s.cartasJogador[C[0]], s) == DOIS
				&& qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s)) {
			if (podeEValeAPenaAumentar(s, 75, 20, 1, 0)
					&& !partidaGanhaParaAdversario(s))
				return -1;
			return C[0];
		}
		if (maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) == AS)
			return C[1] + 10;
		if (qualidadeCarta(s.cartasJogador[C[0]], s) == AS
				&& qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s)) {
			if (podeEValeAPenaAumentar(s, 60, 10, 1, 0)
					&& !partidaGanhaParaAdversario(s))
				return -1;
			return C[0];
		}

		if (qualidadeCarta(s.cartasJogador[C[0]], s) == LIXO) {
			// facão
			if (podeEValeAPenaAumentar(s, 20, 5, 1, 0)
					&& !partidaGanhaParaAdversario(s)) {
				// frases
				if (mandaBala(20)) {
					String[] m = new String[] { "E agora Jos&eacute;?",
							"Fechando o p&eacute; mais uma vez...",
							"O p&eacute n&atilde;o &eacute; de nada, posso sentir!" };
					int i = Math.abs(rand.nextInt()) % 2 + 1;
					chatMsg = m[i];
				}

				return -1;
			}
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s))
				return C[0];
			return C[1];
		}
		// vamos fechar (se der...)
		if (qualidadeCarta(s.cartasJogador[C[0]], s) > qualidadeMaiorMesa(s))
			return C[0];
		if (qualidadeCarta(s.cartasJogador[C[0]], s) == qualidadeMaiorMesa(s)
				&& !maiorCartaEDoParceiro(s))
			return C[0];

		// não dá para fazer nada, vamos jogar a menor e guardar a maior para a
		// última
		return C[1];
	}

	private int joga_segunda_rodada_pe(SituacaoJogo s) {
		// A primeira é deles
		// Temos que fazer de qualquer jeito

		// Se a 1a amarrou, jogar a maior
		if (rodadaAmarrou(s, 0)) {
			if (qualidadeCarta(s.cartasJogador[C[0]], s) > qualidadeMaiorMesa(s)
					&& parceiro(s) != maiorCartaMesa(s)
					&& podeEValeAPenaAumentar(s, 100, 100, 100, 100))
				return -1;
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s)
					&& parceiro(s) != maiorCartaMesa(s))
				return C[0];
			return C[1];
		}

		if (maiorCartaEDoParceiro(s))
			return C[1] + 10; // é isso aí parceiro! Retornar encoberto...
		// frases
		if (mandaBala(15)) {
			String[] m = new String[] { "Boa.", ":-)", "Hummmmmmm...." };
			int i = Math.abs(rand.nextInt()) % 2 + 1;
			chatMsg = m[i];
		}
		return menorCartaParaMatarNaSegunda(s); // deixa comigo
	}

	private int joga_terceira_rodada_mao(SituacaoJogo s) {
		// Eles fizeram a primeira, eu fiz a segunda
		// Se estiver com a partida ganha, arregaçar os caras!
		// Se estiver com manilha, jogar aumentando a aposta (com média
		// probabilidade).
		// Se estiver com três ou menos, jogar quieto

		if (rodadaAmarrou(s, 1))
			return C[0];

		if (partidaGanha(s) && podeEValeAPenaAumentar(s, 100, 100, 100, 100)) {
			// frases
			if (mandaBala(15)) {
				String[] m = new String[] {
						"N&atilde;o tenho mais nada rsrsrs...",
						"Gastei tudo na segunda rsrsrs...",
						"Vamos ter que tirar leite de pedra rsrsrs..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}

		if (qualidadeCarta(s.cartasJogador[C[0]], s) > TRES
				&& podeEValeAPenaAumentar(s, 45, 5, 1, 0)) {
			// frases
			if (mandaBala(20)) {
				String[] m = new String[] {
						"N&atilde;o tenho mais nada rsrsrs...",
						"Gastei tudo na segunda rsrsrs...",
						"Vamos ter que tirar leite de pedra rsrsrs..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}

		return C[0];
	}

	private int joga_terceira_rodada_posmao(SituacaoJogo s) {
		// Nós fizemos a primeira, eles fizeram a segunda
		// Se estiver com a partida ganha, arregaçar os caras!
		// Se não puder com a que estiver vindo, jogar encoberta.
		// Se estiver com três ou manilha, jogar aumentando a aposta (com média
		// probabilidade).
		// Se estiver com dois ou menos, jogar quieto.

		if (rodadaAmarrou(s, 1))
			return C[0];

		if (partidaGanha(s) && podeEValeAPenaAumentar(s, 100, 100, 100, 100)) {
			// frases
			if (mandaBala(20)) {
				String[] m = new String[] { "Jogo psicol&oacute;gico?...",
						"Bom, vamos ver... a primeira foi nossa...",
						"Voc%ecirc;s deveriam correr, estou avisando..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}

		if (qualidadeCarta(s.cartasJogador[C[0]], s) < qualidadeMaiorMesa(s))
			return C[0] + 10;

		if (qualidadeCarta(s.cartasJogador[C[0]], s) >= TRES
				&& podeEValeAPenaAumentar(s, 45, 10, 1, 0)
				&& !partidaGanhaParaAdversario(s)) {
			// frases
			if (mandaBala(20)) {
				String[] m = new String[] { "Jogo psicol&oacute;gico?...",
						"Bom, vamos ver... a primeira foi nossa...",
						"Voc%ecirc;s deveriam correr, estou avisando..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}

		return C[0];
	}

	private int joga_terceira_rodada_parape(SituacaoJogo s) {
		// Eles fizeram a primeira, meu parceiro fez a segunda
		// Agora é a hora da verdade...
		// Se estiver com a partida ganha, arregaçar os caras!
		// Se a maior for do parceiro, e for manilha ou três, deixar passar (c/
		// alta prob.) (já que ele não trucou não vou trucar também)
		// Se não puder com a que estiver vindo, jogar aumentando a aposta no
		// facão (com baixa probabilidade).
		// Se puder com a que estiver vindo, e estiver com manilha, jogar
		// aumentando a aposta (com média-alta probabilidade).
		// Se puder com a que estiver vindo, e estiver com três, jogar
		// aumentando a aposta (com média-baixa probabilidade)
		// Se estiver com dois ou menos, jogar quieto.
		// Se adversário estiver com 10 e nós com 5 ou menos, aumentar aposta!
		// (ia ficar difícil ganhar mesmo...)

		if (rodadaAmarrou(s, 1))
			return C[0];

		if (partidaGanha(s) && podeEValeAPenaAumentar(s, 100, 100, 100, 100)) {
			// frases
			if (mandaBala(20)) {
				String[] m = new String[] {
						"A jurupoca est&aacute; piando!...",
						"Agora &eacute; a hora da verdade!",
						"Essa partida j&aacute; &eacute; nossa..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}

		if (maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) >= TRES) {
			if (podeEValeAPenaAumentar(s, 10, 2, 1, 0))
				return -1;
		} else {
			if (qualidadeCarta(s.cartasJogador[C[0]], s) <= qualidadeMaiorMesa(s)) {
				if (podeEValeAPenaAumentar(s, 20, 5, 1, 0)
						&& qualidadeMaiorMesa(s) != ZAP
						&& !partidaGanhaParaAdversario(s))
					return -1;
			} else {
				if (qualidadeCarta(s.cartasJogador[C[0]], s) > TRES
						&& podeEValeAPenaAumentar(s, 50, 5, 1, 0))
					return -1;

				if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES
						&& podeEValeAPenaAumentar(s, 25, 8, 1, 0))
					return -1;
			}
		}

		if (s.pontosEquipe[adversario1(s) % 2] == 10
				&& s.pontosEquipe[eu(s) % 2] <= 5
				&& !partidaGanhaParaAdversario(s)
				&& podeEValeAPenaAumentar(s, 100, 100, 100, 100)) {
			// frases
			if (mandaBala(20)) {
				String[] m = new String[] {
						"A jurupoca est&aacute; piando!...",
						"Agora &eacute; a hora da verdade!",
						"Essa partida j&aacute; &eacute; nossa..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}

		return C[0];
	}

	private int joga_terceira_rodada_pe(SituacaoJogo s) {
		// Nós fizemos a primeira, eles fizeram a segunda
		// Se estiver com a partida ganha, não tem o que pensar!
		// Se não puder com a que estiver vindo, jogar aumentando a aposta no
		// facão (com média probabilidade).
		// Se puder com a que estiver vindo (pelo menos amarrar), arregaçar os
		// caras!
		// Se adversário estiver com 10 e nós com 5 ou menos, aumentar aposta!
		// (ia ficar difícil ganhar mesmo...)

		if (rodadaAmarrou(s, 1) && matoAdversario(s, false)
				&& podeEValeAPenaAumentar(s, 100, 100, 100, 100)) {
			// frases
			if (mandaBala(15)) {
				String[] m = new String[] {
						"Rsrsrsrsrs...",
						"A força cibern&eacute;tica derrota a for&ccedil;a humana mais uma vez...",
						"HAL n&atilde;o brinca em servi&ccedil;o!..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}

		if (qualidadeCarta(s.cartasJogador[C[0]], s) < qualidadeMaiorMesa(s)
				&& podeEValeAPenaAumentar(s, 15, 3, 1, 0)
				&& !partidaGanhaParaAdversario(s)
				&& parceiro(s) != maiorCartaMesa(s)) {
			// frases
			if (mandaBala(20)) {
				String[] m = new String[] {
						"Rsrsrsrsrs...",
						"A força cibern&eacute;tica derrota a for&ccedil;a humana mais uma vez...",
						"HAL n&atilde;o brinca em servi&ccedil;o!..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}// esse é o espírito do jogo!!!!!

		if (qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s)
				&& podeEValeAPenaAumentar(s, 100, 100, 100, 100)
				&& parceiro(s) != maiorCartaMesa(s)) {
			// frases
			if (mandaBala(20)) {
				String[] m = new String[] {
						"Rsrsrsrsrs...",
						"A força cibern&eacute;tica derrota a for&ccedil;a humana mais uma vez...",
						"HAL n&atilde;o brinca em servi&ccedil;o!..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}

		if (s.pontosEquipe[adversario1(s) % 2] == 10
				&& s.pontosEquipe[eu(s) % 2] <= 5
				&& !partidaGanhaParaAdversario(s)
				&& podeEValeAPenaAumentar(s, 100, 100, 100, 100)) {
			// frases
			if (mandaBala(20)) {
				String[] m = new String[] {
						"Rsrsrsrsrs...",
						"A força cibern&eacute;tica derrota a for&ccedil;a humana mais uma vez...",
						"HAL n&atilde;o brinca em servi&ccedil;o!..." };
				int i = Math.abs(rand.nextInt()) % 2 + 1;
				chatMsg = m[i];
			}
			return -1;
		}

		return C[0];
	}

	// Somente para facilitar organização do código
	private int joga_primeira_rodada(SituacaoJogo s) {

		// se temos casal maior, vamos fazer "cama"
		if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
				&& qualidadeCarta(s.cartasJogador[C[1]], s) == ESCOPETA)
			return C[2];

		switch (minhaPosicao(s)) {
		// Mão
		case 0:
			return this.joga_primeira_rodada_mao(s);
			// Pós mão
		case 1:
			return this.joga_primeira_rodada_posmao(s);
			// Para pé
		case 2:
			return this.joga_primeira_rodada_parape(s);
			// Pé
		case 3:
			return this.joga_primeira_rodada_pe(s);
		}
		return C[0]; // para satisfazer o corretor do método (Eclipse)...
	}

	private int joga_segunda_rodada(SituacaoJogo s) {

		switch (minhaPosicao(s)) {
		// Mão
		case 0:
			return this.joga_segunda_rodada_mao(s);
			// Pós mão
		case 1:
			return this.joga_segunda_rodada_posmao(s);
			// Para pé
		case 2:
			return this.joga_segunda_rodada_parape(s);
			// Pé
		case 3:
			return this.joga_segunda_rodada_pe(s);
		}
		return C[0]; // para satisfazer o corretor do método (Eclipse)...
	}

	private int joga_terceira_rodada(SituacaoJogo s) {

		switch (minhaPosicao(s)) {
		// Mão
		case 0:
			return this.joga_terceira_rodada_mao(s);
			// Pós mão
		case 1:
			return this.joga_terceira_rodada_posmao(s);
			// Para pé
		case 2:
			return this.joga_terceira_rodada_parape(s);
			// Pé
		case 3:
			return this.joga_terceira_rodada_pe(s);
		}
		return C[0]; // para satisfazer o corretor do método (Eclipse)...
	}

	/**
	 * Executa uma jogada.
	 * <p>
	 * Observe que, ao pedir aumento, o sistema irá interagir com a outra dupla.
	 * Se a partida seguir, o método será chamado novamente para efetivar a real
	 * jogada.
	 * <p>
	 * A estratégia é responsável por checar se o valor da próxima aposta é
	 * diferente de 0 e só pedir aumento nesta situação.
	 * <p>
	 * 
	 * @param s
	 *            Situação do jogo no momento
	 * @return posição da carta na mão a jogar (em letrasCartasJogador), ou -1
	 *         para pedir truco
	 */
	public int joga(SituacaoJogo s) {
		// Classifica as cartas que tenho na mão, da maior para a menor,
		// de maneira que vamos ter:
		// C[0] = maior carta (1a, 2a e 3a mão);
		// C[1] = carta intermediária (1a mão) OU
		// C[1] = menor carta (2a mão);
		// C[2] = menor carta (1a mão)
		classificaCartas(s);

		switch (s.numRodadaAtual) {
		// Primeira rodada
		case 1:

			// Debug
			// System.out.println("Cartas: " + s.cartasJogador[C[0]].toString()
			// + ">" + s.cartasJogador[C[0]].getValorTruco(s.manilha) + ">" +
			// qualidadeCarta(s.cartasJogador[C[0]],s) + " " +
			// s.cartasJogador[C[1]].toString() + ">" +
			// s.cartasJogador[C[1]].getValorTruco(s.manilha ) + ">" +
			// qualidadeCarta(s.cartasJogador[C[1]],s) + " " +
			// s.cartasJogador[C[2]].toString() + ">" +
			// s.cartasJogador[C[2]].getValorTruco(s.manilha) + ">" +
			// qualidadeCarta(s.cartasJogador [C[2]],s) + "\n");

			return this.joga_primeira_rodada(s);
			// Segunda rodada
		case 2:
			// A checagem se está amarrado é feita depois
			return this.joga_segunda_rodada(s);
			// Terceira rodada
		case 3:
			// Primeira análise de amarração, mais depois nas rotinas
			// individuais
			if (rodadaAmarrou(s, 1)) {
				if ((partidaGanha(s) && podeEValeAPenaAumentar(s, 100, 100,
						100, 100))
						|| (matoAdversario(s, false)
								&& (qualidadeMinhaMaior(s) == PICAFUMO
										|| qualidadeMinhaMaior(s) == ESPADILHA || qualidadeMinhaMaior(s) == ESCOPETA) && podeEValeAPenaAumentar(
									s, 50, 10, 1, 0)))
					return -1; // Aumentar aposta
			}
			return this.joga_terceira_rodada(s);
		}
		return 0; // para satisfazer o corretor do método (Eclipse)...
	}

	/**
	 * Retorna se eu aceito o aumento da aposta dos adversários ou não. Uma
	 * melhoria seria checar se estão pedindo truco, seis, ... e tomar as
	 * decisões de acordo
	 */
	public boolean aceitaTruco(SituacaoJogo s) {
		classificaCartas(s);

		// não vai nem ter graça...
		if (partidaGanha(s))
			return true;

		switch (s.numRodadaAtual) {

		// primeira rodada
		case 1:

			// se eu tenho uma manilha e na mesa já estiver pelo menos um 3
			// nosso, aceito
			if (qualidadeCarta(s.cartasJogador[C[0]], s) >= TRES
					&& maiorCartaENossa(s) && qualidadeMaiorMesa(s) >= TRES)
				return true;
			// se eu tenho um três e na mesa já estiver pelo menos um 3 nosso,
			// aceito
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES
					&& maiorCartaENossa(s) && qualidadeMaiorMesa(s) >= TRES)
				return true;
			// se na mesa já estiver vindo uma manilha nossa, e eu tenho um 2
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == DOIS
					&& maiorCartaENossa(s) && qualidadeMaiorMesa(s) > TRES)
				return true;
			// se eu tenho manilha e três pelo menos, aceito sem pensar
			if (qualidadeCarta(s.cartasJogador[C[0]], s) > TRES
					&& qualidadeCarta(s.cartasJogador[C[1]], s) >= TRES)
				return true;
			// se na mesa já estiver vindo uma manilha nossa, aceito com
			// media para alta prob.
			if (maiorCartaENossa(s) && qualidadeMaiorMesa(s) > TRES
					&& valeAPenaAceitar(s, 70))
				return true;
			// se na mesa já estiver vindo um tres nosso, aceito com
			// media para alta prob.
			if (maiorCartaENossa(s) && qualidadeMaiorMesa(s) >= TRES
					&& valeAPenaAceitar(s, 75))
				return true;
			// se eu tenho uma manilha seca, aceito (alta prob.)
			if (qualidadeCarta(s.cartasJogador[C[0]], s) > TRES
					&& valeAPenaAceitar(s, 90))
				return true;
			// se eu tenho dois três, aceito (alta prob.)
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES
					&& qualidadeCarta(s.cartasJogador[C[1]], s) == TRES
					&& valeAPenaAceitar(s, 80))
				return true;
			// se eu tenho um três, aceito (média p/ alta prob.)
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES
					&& valeAPenaAceitar(s, 60))
				return true;
			// se eu tenho um dois, aceito (baixa prob.)
			if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES
					&& valeAPenaAceitar(s, 20))
				return true;
			if (valeAPenaAceitar(s, 0))
				return true; // last check if there are any relevant cases we
								// should call before refuse it
			return false;

			// segunda rodada
		case 2:

			// se a primeira for nossa:
			if (primeiraENossa(s)) {
				// partida ganha?
				if (partidaGanha(s))
					return true;

				// se na mesa já estiver pelo menos um 3 nosso, aceito
				if (maiorCartaENossaPodendoEstarAmarrado(s)
						&& qualidadeMaiorMesa(s) >= TRES)
					return true;
				// se na mesa já estiver um 2 nosso, aceito com média-alta
				// probabilidade
				if (maiorCartaENossaPodendoEstarAmarrado(s)
						&& qualidadeMaiorMesa(s) == DOIS
						&& valeAPenaAceitar(s, 75))
					return true;
				// se eu tiver pelo menos um dois, aceito
				if (qualidadeCarta(s.cartasJogador[C[0]], s) >= DOIS)
					return true;
			} else {
				// se a primeira NÃO for nossa:
				// se já está tudo decidido, somente estão trucando de bobeira
				if (partidaGanhaParaAdversario(s))
					return false;
				// Se a 1a amarrou e tenho zap, aceito
				if (rodadaAmarrou(s, 0)
						&& qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP)
					return true;
				// se eu já joguei, e meu parceiro ainda não, vou deixar para
				// ele decidir
				if (minhaVez(s) == 1 && vezTrucador(s) == 2)
					return false;
				// Se a 1a amarrou e tenho manilha/três, aceito
				if (rodadaAmarrou(s, 0)
						&& qualidadeCarta(s.cartasJogador[C[0]], s) >= TRES
						&& qualidadeCarta(s.cartasJogador[C[0]], s) >= qualidadeMaiorMesa(s)
						&& valeAPenaAceitar(s, 70))
					return true;
				// Se a 1a amarrou e na mesa já está vindo pelo menos um três
				// nosso, aceito
				if (rodadaAmarrou(s, 0) && maiorCartaENossa(s)
						&& qualidadeMaiorMesa(s) >= TRES
						&& valeAPenaAceitar(s, 50))
					return true;

				// se eu tenho uma manilha e na mesa já estiver pelo menos um 3
				// nosso, aceito
				if (qualidadeCarta(s.cartasJogador[C[0]], s) >= TRES
						&& maiorCartaENossa(s) && qualidadeMaiorMesa(s) >= TRES)
					return true;
				// se eu tenho manilha e três pelo menos, aceito
				if (qualidadeCarta(s.cartasJogador[C[0]], s) > TRES
						&& qualidadeCarta(s.cartasJogador[C[1]], s) >= TRES
						&& !partidaGanhaParaAdversario(s))
					return true;
				// se eu tenho o zap, aceito (média prob.)
				if (qualidadeCarta(s.cartasJogador[C[0]], s) == ZAP
						&& valeAPenaAceitar(s, 80))
					return true;
				// se eu tenho uma manilha seca, aceito (média prob.)
				if (qualidadeCarta(s.cartasJogador[C[0]], s) > TRES
						&& !partidaGanhaParaAdversario(s)
						&& valeAPenaAceitar(s, 40))
					return true;
				// se eu tenho uma três seco, aceito (baixa prob.)
				if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES
						&& !partidaGanhaParaAdversario(s)
						&& valeAPenaAceitar(s, 10))
					return true;
			}
			if (valeAPenaAceitar(s, 0))
				return true; // last check if there are any relevant cases we
								// should call before refuse it
			return false;

			// terceira rodada
		case 3:

			// se já está tudo decidido, somente estão trucando de bobeira
			if (partidaGanhaParaAdversario(s))
				return false;

			// já está tudo decidido a nosso favor
			if (partidaGanha(s))
				return true;

			// se eu já joguei, e meu parceiro ainda não, vou deixar para ele
			// decidir
			if ((minhaVez(s) == 0 && vezTrucador(s) == 1)
					|| (minhaVez(s) == 1 && vezTrucador(s) == 2))
				return false;

			// se a primeira for nossa:
			if (primeiraENossa(s)) {
				// se na mesa já estiver uma manilha nossa, aceito com alta
				// prob.
				if (maiorCartaENossaPodendoEstarAmarrado(s)
						&& qualidadeMaiorMesa(s) > TRES
						&& valeAPenaAceitar(s, 80))
					return true;
				// se na mesa já estiver um 3 nosso, aceito com média-alta prob.
				if (maiorCartaENossaPodendoEstarAmarrado(s)
						&& qualidadeMaiorMesa(s) == TRES
						&& valeAPenaAceitar(s, 60))
					return true;
				// se na mesa já estiver um 2 nosso, aceito com média-baixa
				// prob.
				if (maiorCartaENossaPodendoEstarAmarrado(s)
						&& qualidadeMaiorMesa(s) == DOIS
						&& valeAPenaAceitar(s, 40))
					return true;
				// checar se ainda tenho cartas na mão...
				if (minhaVez(s) > vezTrucador(s)
						&& !partidaGanhaParaAdversario(s)) {
					// se eu tenho uma manilha, aceito com alta prob.
					if (qualidadeCarta(s.cartasJogador[C[0]], s) > TRES
							&& valeAPenaAceitar(s, 80))
						return true;
					// se eu tenho um três, aceito com média-alta prob.
					if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES
							&& valeAPenaAceitar(s, 60))
						return true;
					// se eu tenho um dois, aceito com média-baixa prob.
					if (qualidadeCarta(s.cartasJogador[C[0]], s) == DOIS
							&& valeAPenaAceitar(s, 30))
						return true;
				}
			} else {
				// se a primeira NÃO for nossa:
				// Se a 1a e a 2a amarraram, aceito qualquer trucada!
				if (rodadaAmarrou(s, 1) && maiorCartaENossa(s))
					return true;
				// se na mesa já estiver uma manilha nossa, aceito com alta
				// prob.
				if (maiorCartaENossa(s) && qualidadeMaiorMesa(s) > TRES
						&& valeAPenaAceitar(s, 75))
					return true;
				// se na mesa já estiver um 3 nosso, aceito com média prob.
				if (maiorCartaENossa(s) && qualidadeMaiorMesa(s) == TRES
						&& valeAPenaAceitar(s, 50))
					return true;
				// se na mesa já estiver um 2 nosso, aceito com média-baixa
				// prob.
				if (maiorCartaENossa(s) && qualidadeMaiorMesa(s) == DOIS
						&& valeAPenaAceitar(s, 20))
					return true;
				// se na mesa estiver um lixo nosso, aceito com baixa prob. -
				// devem estar blefando...
				if (maiorCartaENossa(s) && qualidadeMaiorMesa(s) <= AS
						&& valeAPenaAceitar(s, 8))
					return true;
				// checar se ainda tenho cartas na mão...
				if (minhaVez(s) > vezTrucador(s)
						&& !partidaGanhaParaAdversario(s)) {
					// se eu tenho uma manilha, aceito com alta prob.
					if (qualidadeCarta(s.cartasJogador[C[0]], s) > TRES
							&& valeAPenaAceitar(s, 70))
						return true;
					// se eu tenho um três, aceito com média-alta prob.
					if (qualidadeCarta(s.cartasJogador[C[0]], s) == TRES
							&& valeAPenaAceitar(s, 50))
						return true;
					// se eu tenho um dois, aceito com média-baixa prob.
					if (qualidadeCarta(s.cartasJogador[C[0]], s) == DOIS
							&& valeAPenaAceitar(s, 20))
						return true;
				}
			}
			if (valeAPenaAceitar(s, 0))
				return true; // last check if there are any relevant cases we
								// should call before refuse it
			return false;

		}
		return false;
	}

	/**
	 * Retorna se eu aceito jogar ou não esta mão de 11.
	 */
	public boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s) {
		// Não vamos pensar muito...

		int q3 = 0, q2 = 0, qManilhas = 0;

		for (int i = 0; i <= 2; i++) {
			// quantidade de manilhas
			if (qualidadeCarta(s.cartasJogador[i], s) > TRES)
				qManilhas++;
			// quantidade de 3
			if (qualidadeCarta(s.cartasJogador[i], s) == TRES)
				q3++;
			// quantidade de 2
			if (qualidadeCarta(s.cartasJogador[i], s) == DOIS)
				q2++;
			// quantidade de manilhas do parceiro
			if (qualidadeCarta(cartasParceiro[i], s) > TRES)
				qManilhas++;
			// quantidade de 3 do parceiro
			if (qualidadeCarta(cartasParceiro[i], s) == TRES)
				q3++;
			// quantidade de 2 do parceiro
			if (qualidadeCarta(cartasParceiro[i], s) == DOIS)
				q2++;
		}

		// vamos analisar!
		if (qManilhas >= 2 || q3 >= 2 || (q3 >= 1 && q2 >= 2)
				|| (qManilhas >= 1 && q3 >= 1) || (qManilhas >= 1 && q2 >= 1))
			return true;
		if (s.pontosEquipe[adversario1(s) % 2] > 6
				&& s.pontosEquipe[adversario1(s) % 2] < 9
				&& (qManilhas >= 1 && q2 >= 1) || q3 >= 2) {
			return true;
		}
		if (s.pontosEquipe[adversario1(s) % 2] <= 6
				&& (qManilhas >= 1 || q3 >= 1 || q2 >= 2)) {
			return true;
		}
		return false;
	}

	public void inicioPartida() {
	}

	public void inicioMao() {
	}

	public void pediuAumentoAposta(int posJogador, int valor) {
	}

	public void aceitouAumentoAposta(int posJogador, int valor) {
	}

	public void recusouAumentoAposta(int posJogador) {
	}

	public void setGameLevel(int gameLevel) {
		this.gameLevel = gameLevel;
	}

	public String getChatMsg() {
		String s = this.chatMsg;
		this.chatMsg = ""; // erase this now to avoid keep repeating the same
							// message over and over
		return s;
	}
}