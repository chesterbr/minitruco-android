package me.chester;

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
 * Base para as estratégias "plugáveis" que um jogador CPU pode utilizar.
 * <p>
 * Uma estratégia é uma classe que implementa os métodos <code>joga</code>,
 * <code>aceitaTruco</code> e <code>aceitaMao11</code>, que são chamados
 * pelo jogo quando for a vez do jogador, quando pedirem aumento para a dupla
 * dele e quando a dupla for decidir uma mão de 11, respectivamente.
 * <p>
 * Eses métodos recebem uma "fotografia" do jogo (SituacaoJogo) no momento em
 * que a ação deles é demandada. Esta fotografia inclui todo o histórico da
 * mão/rodada, placares, etc. Se for desejado guardar estado, o tempo de vida de
 * uma estratégia é o mesmo de <code>Jogo</code>, ou seja, o estado (não-<code>static</code>)
 * persistirá ao longo de uma partida, mas não entre partidas.
 * <p>
 * Para que a estratégia apareça no jogo, adicione uma instância dela ao array
 * ESTRATEGIAS da classe Jogador.
 * <p>
 * O modo Confronto de Estratégias permite testar a sua estratégia contra as já
 * existentes. Quando ela estiver boa, você pode contribui-la para o jogo (desde
 * que concorde em licenciá-la através da GPLv3). Você será creditado e manterá
 * seus direitos autorais.
 * 
 * @author Chester
 * @see Jogador#ESTRATEGIAS
 */
public interface Estrategia {

	/**
	 * Retorna o nome "copmpleto" da Estrategia
	 */
	public abstract String getNomeEstrategia();
	
	/**
	 * Retorna informações de copyright e afins
	 */
	public abstract String getInfoEstrategia();

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
	public abstract int joga(SituacaoJogo s);

	/**
	 * Decide se aceita um pedido de aumento.
	 * <p>
	 * O valor do aumento pode ser determinado verificando o valor atual da
	 * partida (que ainda não foi aumentado)
	 * 
	 * @param s
	 *            Situação do jogo no momento
	 * @return true para aceitar, false para desistir
	 */
	public abstract boolean aceitaTruco(SituacaoJogo s);

	/**
	 * Decide se aceita iniciar uma "mão de 11"
	 * 
	 * @param cartasParceiro
	 *            cartas que o parceiro possui
	 * @return true para iniciar valendo 3 pontos, false para desistir e perder
	 *         1 ponto
	 */
	public abstract boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s);

	/**
	 * Notifica que uma partida está começando.
	 */
	public abstract void inicioPartida();

	/**
	 * Notifica que uma mão está começando
	 */
	public abstract void inicioMao();

	/**
	 * Informa que um jogador pediu aumento de aposta (truco, seis, etc.).
	 * 
	 * @param posJogador
	 *            Jogador que pediu o aumento
	 * @param valor
	 *            Quanto a rodada passará a valar se algum adversário aceitar
	 */
	public abstract void pediuAumentoAposta(int posJogador, int valor);

	/**
	 * Informa que o jogador aceitou um pedido de aumento de aposta.
	 * 
	 * @param posJogador
	 *            Jogador que aceitou o aumento
	 * @param valor
	 *            Quanto a rodada está valendo agora
	 */
	public abstract void aceitouAumentoAposta(int posJogador, int valor);

	/**
	 * Informa que o jogador recusou um pedido de aumento de aposta.
	 * <p>
	 * Obs.: isso não impede que o outro jogador da dupla aceite o pedido, é
	 * apenas para notificação visual. Se o segundo jogdor recusar o pedido, a
	 * mensagem de derrota da dupla será enviada logo em seguida.
	 * 
	 * @param posJogador
	 *            Jogador que recusou o pedido.
	 */
	public abstract void recusouAumentoAposta(int posJogador);

}
