package me.chester.minitruco.server;

import me.chester.minitruco.core.Carta;

/*
 * Copyright © 2006-2007 Carlos Duarte do Nascimento (Chester)
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
 * Joga uma carta na mesa.
 * <p>
 * Parâmetro: Carta a ser jogada, no formato Ln (Letra/Naipe).<br>
 * Parâmetro 2 (opcional, default F): Se T, joga a carta fechada
 * <p>
 * Se a jogada for válida, será informada para todos os jogadores (incluindo o
 * que jogou). Se não for, nenhuma mensagem é devolvida.
 * 
 * @author Chester
 * @see Carta#toString()
 * 
 */
public class ComandoJ extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		// Verifica se estamos em jogo e se recebeu argumento
		if ((!j.jogando) || (args.length<2))
			return;
		// Encontra a carta solicitada (na mão do jogador)
		Carta[] cartas = j.getCartas();
		for (int i = 0; i < cartas.length; i++) {
			if (cartas[i] != null && cartas[i].toString().equals(args[1])) {
				// Joga a carta. Se der certo o evento vai notificar a todos.
				cartas[i].setFechada(args.length > 2 && args[2].equals("T"));
				j.getSala().getJogo().jogaCarta(j, cartas[i]);
			}
		}
	}
}
