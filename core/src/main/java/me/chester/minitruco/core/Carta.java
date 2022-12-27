package me.chester.minitruco.core;

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

/**
 * Representa uma carta do truco
 * 
 * 
 */
public class Carta {

	/**
	 * Cria uma carta com letra e naipe definidos
	 * 
	 * @param letra
	 * @param naipe
	 */
	public Carta(char letra, int naipe) {
		setLetra(letra);
		setNaipe(naipe);
	}

	/**
	 * Cria uma carta baseado em sua representação string
	 * 
	 * @param sCarta
	 *            letra e naipe da carta, conforme retornado por
	 *            <code>toString()</code>
	 * @see Carta#toString()
	 */
	public Carta(String sCarta) {
		this(sCarta.charAt(0), "coepx".indexOf(sCarta.charAt(1)));
	}

	/**
	 * Constante que representa o naipe de copas
	 */
	public static final int NAIPE_COPAS = 0;

	/**
	 * Constante que representa o naipe de ouros
	 */
	public static final int NAIPE_OUROS = 1;

	/**
	 * Constante que representa o naipe de espadas
	 */
	public static final int NAIPE_ESPADAS = 2;

	/**
	 * Constante que representa o naipe de paus
	 */
	public static final int NAIPE_PAUS = 3;

	/**
	 * Lista ordenada dos naipes
	 */
	public static final int[] NAIPES = { NAIPE_COPAS, NAIPE_ESPADAS,
			NAIPE_OUROS, NAIPE_PAUS };

	/**
	 * Indica que o naipe da carta não foi escolhido
	 */
	public static final int NAIPE_NENHUM = 4;

	/**
	 * Indica que a letra da carta não foi escolhida
	 */
	public static final char LETRA_NENHUMA = 'X';

	private static final String LETRAS_VALIDAS = "A23456789JQK";

	private boolean cartaEmJogo = true;

	private char letra = LETRA_NENHUMA;

	private int naipe = NAIPE_NENHUM;

	private boolean fechada = false;

	/**
	 * Determina a letra (valor facial) da carta.
	 * <p>
	 * Letras válidas são as da constante LETRAS_VALIDAS. Se a letra for
	 * inválida, a propriedade não é alterda.
	 * 
	 * @param letra
	 */
	public void setLetra(char letra) {
		if (LETRAS_VALIDAS.indexOf(letra) != -1 || letra == LETRA_NENHUMA) {
			this.letra = letra;
		}
	}

	public char getLetra() {
		return letra;
	}

	/**
	 * Seta o naipe da carta.
	 * <p>
	 * Caso o naipe seja inválido, não é alterado
	 * 
	 * @param naipe
	 *            Naipe de acordo com as constantes
	 */
	public void setNaipe(int naipe) {
		if (naipe == NAIPE_COPAS || naipe == NAIPE_OUROS || naipe == NAIPE_PAUS
				|| naipe == NAIPE_ESPADAS || naipe == NAIPE_NENHUM) {
			this.naipe = naipe;
		}
	}

	public int getNaipe() {
		return naipe;
	}

	public int getValor() {
		return LETRAS_VALIDAS.indexOf(letra);
	}

	/**
	 * Determina que uma carta foi jogada como "fechada", e seu valor deve ser
	 * ignorado.
	 * 
	 * @param fechada
	 */
	public void setFechada(boolean fechada) {
		this.fechada = fechada;
	}

	public boolean isFechada() {
		return fechada;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object outroObjeto) {
		if ((outroObjeto != null) && (outroObjeto instanceof Carta)) {
			Carta outraCarta = (Carta) outroObjeto;
			return outraCarta.getNaipe() == this.getNaipe()
					&& outraCarta.getLetra() == this.getLetra();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getLetra() * 256 + getNaipe();
	}

	/**
	 * Retorna um valor de 1 a 14 para esta carta, considerando a manilha
	 * 
	 * @param letraManilha
	 *            letra da manilha desta rodada
	 * @return valor que permite comparar duas cartas
	 */
	public int getValorTruco(char letraManilha) {
		return Jogo.getValorTruco(this, letraManilha);
	}

	/**
	 * Representação em 2 caracteres da carta, formada por letra (em
	 * "A234567QJK") e naipe ([c]opas, [o]uro, [e]spadas,[p]aus ou [x] para
	 * nenhum).
	 * <p>
	 * Esta representação é usada na comunicação cliente-servidor, então não
	 * deve ser alterada (ou, se for, o construtor baseado em caractere deve ser
	 * alterado de acordo).
	 */
	public String toString() {
		return letra + "" + ("coepx").charAt(naipe);
	}

	/**
	 * Escurece/clareia uma carta para indicar que ela não está/está em jogo
	 * 
	 * @param cartaEmJogo
	 *            true para clarear, false para escurecer
	 */
	public void setCartaEmJogo(boolean cartaEmJogo) {
		this.cartaEmJogo = cartaEmJogo;
	}

	/**
	 * Indica se a carta está em jogo, e, portanto, deve ficar "clarinha" (as
	 * cartas de rodadas passadas são escurecidas
	 */
	public boolean isCartaEmJogo() {
		return cartaEmJogo;
	}

}
