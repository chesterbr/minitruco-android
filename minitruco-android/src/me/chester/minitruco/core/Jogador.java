package me.chester.minitruco.core;

import java.util.Random;

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
 * Base para os diversos tipos de jogador que podem participar de um jogo.
 * <p>
 * Todo Jogador recebe notificações do jogo (por implementar Interessado)
 * <p>
 * A subclasse determina se o jogador é o usuário do celular, um jogador virtual
 * ou um jogador de outro celular conectado remotamente.
 * 
 * @author Chester
 * 
 */
public abstract class Jogador implements Interessado {

	// Variáveis / Métodos úteis

	protected static Random random = new Random();

	private int posicao = 0;

	private Carta[] cartas;

	/**
	 * Jogo que está sendo jogado por este jogador
	 */
	protected Jogo jogo;

	/**
	 * Processa o evento de entrada no jogo (guardando o jogo)
	 */
	public void entrouNoJogo(Interessado i, Jogo j) {
		if (i.equals(this)) {
			this.jogo = j;
		}
	}

	private String nome = "unnamed";
	private boolean isGuest = true;
	long loginTime = 0;

	/**
	 * Nome do jogador (em jogos multiplayer)
	 * 
	 * @return
	 */
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public boolean getIsGuest() {
		return isGuest;
	}

	public void setIsGuest(boolean isGuest) {
		this.isGuest = isGuest;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	/**
	 * List of voted people (control for multiplayer games using database)
	 * 
	 * @return
	 */

	private String[] votedCoolList = new String[100];
	private int votedCoolListPointer = 0;

	public String getVotedCoolList(int pos) {
		if (!(votedCoolList[pos] == null))
			return votedCoolList[pos];
		else
			return "";
	}

	public int getVotedCoolListLength() {
		return votedCoolList.length;
	}

	public void addToVotedCoolList(String votedUser) {
		this.votedCoolList[votedCoolListPointer] = votedUser;
		votedCoolListPointer++;
		if (votedCoolListPointer == 100)
			votedCoolListPointer = 0;
	}

	/**
	 * Recupera a posição do jogador no jogo
	 * 
	 * @return número de 1 a 4 (não necessariamente a posição dele na mesa)
	 */
	public int getPosicao() {
		return posicao;
	}

	public void setPosicao(int posicao) {
		this.posicao = posicao;
	}

	/**
	 * Recupera a equipe em que este jogador está (assumindo que ele já esteja
	 * aceito em um jogo)
	 * 
	 * @return 1 ou 2
	 */
	public int getEquipe() {
		return 1 + ((1 + posicao) % 2);
	}

	/**
	 * Recupera a posição do parceiro
	 * 
	 * @return número de 1 a 4
	 */
	public int getParceiro() {
		return 1 + ((posicao + 1) % 4);
	}

	public int getEquipeAdversaria() {
		return 1 + (posicao % 2);
	}

	public void setCartas(Carta[] cartas) {
		this.cartas = cartas;
	}

	public Carta[] getCartas() {
		return cartas;
	}

	public boolean possuiCarta(Carta c) {
		if (cartas == null) {
			return false;
		}
		for (int i = 0; i < cartas.length; i++) {
			if (cartas[i].equals(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Estratégias suportadas pelos jogadores automático (CPU e Bot)
	 */
	// TODO implementar alguma estrategia e adicionar aqui
	static Estrategia[] ESTRATEGIAS = { new EstrategiaGasparotto(), new EstrategiaSellani() };

	/**
	 * Lista de opções de estratégia para comboboxes (tem os nomes e a última
	 * opção é a de sorteio
	 */
	static String[] opcoesEstrategia = new String[ESTRATEGIAS.length + 1];

	static {
		// Preenche a lista de opções usando o array de estratégias
		// (o último elemento é preenchido depois de carregar o idioma,
		// pois é a frase "sortear estratégia")
		for (int i = 0; i < ESTRATEGIAS.length; i++)
			opcoesEstrategia[i] = ESTRATEGIAS[i].getNomeEstrategia();
	}

	/**
	 * Instancia uma estratégia (para uso em jogadores que precisam disso, como
	 * o <code>JogadorBot</code> ou o <code>JogadorCPU</code>).
	 * 
	 * @param nomeEstrategia
	 *            Nome da estratégia (ex.: "Willian"). Se nenhuma estratégia se
	 *            identificar por aquele nome, sorteia uma aleatória
	 * @return nova instância da estratégia
	 */
	public static Estrategia criaEstrategiaPeloNome(String nomeEstrategia) {
		// Procura uma classe de estratégia com aquele nome
		int numEstrategia = -1;
		for (int i = 0; i < ESTRATEGIAS.length; i++) {
			if (ESTRATEGIAS[i].getNomeEstrategia().equals(nomeEstrategia)) {
				numEstrategia = i;
				break;
			}
		}
		// Se não houver nenhuma, sorteia
		if (numEstrategia == -1) {
			numEstrategia = random.nextInt(ESTRATEGIAS.length);
		}

		// Cria uma nova instância
		try {
			Log.i("Jogador","Criando estrategia: "+ESTRATEGIAS[numEstrategia].getNomeEstrategia());
			return (Estrategia) ESTRATEGIAS[numEstrategia].getClass()
					.newInstance();
		} catch (InstantiationException e) {
			throw new Error(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new Error(e.getMessage());
		}
	}
}
