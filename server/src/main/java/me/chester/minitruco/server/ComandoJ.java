package me.chester.minitruco.server;

import me.chester.minitruco.core.Carta;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Joga uma carta na mesa.
 * <p>
 * Parâmetro: Carta a ser jogada, no formato Ln (Letra/Naipe).<br>
 * Parâmetro 2 (opcional, default F): Se T, joga a carta fechada
 * <p>
 * Se a jogada for válida, será informada para todos os jogadores (incluindo o
 * que jogou). Se não for, nenhuma mensagem é devolvida.
 *
 *
 * @see Carta#toString()
 */
public class ComandoJ extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		// Verifica se estamos em jogo e se recebeu argumento
		if ((!j.jogando) || (args.length<2))
			return;
		// Encontra a carta solicitada (na mão do jogador)
		for (Carta carta : j.getCartas()) {
			if (carta != null && carta.toString().equals(args[1])) {
				// Joga a carta. Se der certo o evento vai notificar a todos.
				carta.setFechada(args.length > 2 && args[2].equals("T"));
				j.getSala().getJogo().jogaCarta(j, carta);
			}
		}
	}
}
