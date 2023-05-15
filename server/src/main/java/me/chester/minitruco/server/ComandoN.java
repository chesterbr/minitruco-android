package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Atribui um nome ao jogador.
 * <p>
 * Parâmetro: Nome a atribuir
 * <p>
 * O nome deve ser único, e conter apenas letras, números e os caracteres em
 * CARACTERES_PERMITIDOS. Também não pode começar com "Robo_".
 * <p>
 * O servidor guarda o upper/lowercase, mas o nome tem que ser único de forma case-insensitive.
 * Ex.: se o "Roberto" entrou, o "roberto" ou o "ROBERTO" não pdoem entrar.
 */
public class ComandoN extends Comando {

	private static final String CARACTERES_PERMITIDOS = "!@()-_.";

	// TODO sanitizar nomes reservados, case insensitive: bot, chester, chesterbr, minitruco
	// (aqui ou em Sala?)

	@Override
	public void executa(String[] args, JogadorConectado j) {
		String nome;
		// Valida o apelido
		try {
			if (args == null || args[1].length() < 1 || args[1].length() > 50 || args[1].equals("unnamed")) {
				j.println("X NI");
				return;
			}
		} catch (Exception e) {
			j.println("X NI");
			return;
		}
		nome = args[1];
		for (int i = 0; i < nome.length(); i++) {
			char c = nome.charAt(i);
			if (!(Character.isLetterOrDigit(c) || CARACTERES_PERMITIDOS
					.indexOf(c) != -1)) {
				j.println("X NI");
				return;
			}
		}
		if (JogadorConectado.isNomeEmUso(nome)) {
			j.println("X NE");
			return;
		}
		j.setNome(nome);
		j.println("N " + nome);
	}
}
