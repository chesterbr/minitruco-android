package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Assets e funções utilitárias comuns a todos os módulos (app android,
 * servidor Java, etc.)
 */
public class TrucoUtils {

    /**
     * String que deve ser substituída pela posição do jogador
     * @see TrucoUtils#montaNotificacaoI(String[], String)
     */
    public static final String POSICAO_PLACEHOLDER = "$POSICAO";

    /**
     * Monta a notificação de informação da sala (que será enviada para todos os
     * jogadores não-locais, sejam eles clientes bluetooth ou internet).
     * <p>
     * Chamadores devem substituir o valor em POSICAO_PLACEHOLDER pela posição
     * do jogdaor para o qual a informação será enviada.
     *
     * @param nomes array de Jogador ou de nomes (sanitizados)
     * @param modo modo de partida (ver `Partida` e subclasses)
     * @param sala string que diz o tipo e código da sala (ex.: "BLT" para
     *             bluetooth, "PRI-1234" para sala privada com código 1234)
     * @return String no formato "I ..." definido em protocolo.txt
     */
    public static String montaNotificacaoI(Object[] nomes, String modo, String sala) {
        StringBuilder sb = new StringBuilder("I ");
        for (int i = 0; i <= 3; i++) {
            String nome = nomes[i] instanceof Jogador ? ((Jogador) nomes[i])
                .getNome() : (String)nomes[i];
            if (nome == null || nome == "") {
                nome = "bot";
            }
            sb.append(i == 0 ? "" : '|');
            sb.append(nome);
        }
        sb.append(' ')
          .append(modo)
          .append(' ')
          .append(POSICAO_PLACEHOLDER)
          .append(' ')
          .append(sala);

        return sb.toString();
    }

}
