package me.chester.minitruco.core;

import android.graphics.Canvas;


/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
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
 * Representa uma carta do truco
 * 
 * @author Chester
 * 
 */
public class Carta {

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

	private int top;

	private int left;

	private boolean virada = false;

	private boolean fechada = false;

	/**
	 * Cria uma carta com letra e naipe definidos, já "virada" (visível)
	 * 
	 * @param letra
	 * @param naipe
	 */
	public Carta(char letra, int naipe) {
		setLetra(letra);
		setNaipe(naipe);
		setVirada(true);
	}

	/**
	 * Cria uma carta baseado em sua representação string
	 * 
	 * @param sCarta
	 *            letra e naipe da carta, conforme retornado por <code>toString()</code>
	 * @see Carta#toString()
	 */
	public Carta(String sCarta) {
		this(sCarta.charAt(0), "coepx".indexOf(sCarta.charAt(1)));
	}

	/**
	 * Cria uma carta vazia na posição especificada
	 * <p>
	 * Obs.: a carta aparece desvirada, e não pode ser virada enquanto não forem
	 * atribuídos naipe e letra
	 */
	public Carta(int left, int top) {
		setTop(top);
		setLeft(left);
	}

	/**
	 * Cria uma carta vazia, posicionada em 0,0
	 * 
	 * @see Carta#Carta(int, int)
	 * 
	 */
	public Carta() {

	}

	
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

	public void setTop(int top) {
		this.top = top;
	}

	public int getTop() {
		return top;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getLeft() {
		return left;
	}

	public int getValor() {
		return LETRAS_VALIDAS.indexOf(letra);
	}

	/**
	 * Determina se uma carta está virdada (mostrando o valor) ou não (mostrando
	 * o desenho).
	 * <p>
	 * Obs.: cartas têm que ter uma letra e naipe para serem viradas, e cartas
	 * fechadas não podem ser viradas
	 * 
	 * @param virada
	 */
	public void setVirada(boolean virada) {
		this.virada = virada && (!fechada) && (letra != LETRA_NENHUMA)
				&& (naipe != NAIPE_NENHUM);
	}

	/**
	 * Determina se uma carta está virada (mostrando o valor)
	 * 
	 * @return True se a carta está mostrando o valor, false caso contrário
	 */
	public boolean isVirada() {
		return virada;
	}

	/**
	 * Determina que uma carta foi jogada como "fechada", e seu valor deve ser
	 * ignorado.
	 * 
	 * @param fechada
	 */
	public void setFechada(boolean fechada) {
		this.fechada = fechada;
		// Se uma carta for fechada, não pode estar virada (visível)
		if (fechada)
			this.virada = false;
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
