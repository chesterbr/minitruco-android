package me.chester.minitruco.core;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2011 Guilherme Caram <gcaram@gmail.com> */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Encapsula diferenças de pontuação entre as diferentes modalidades
 * de truco (paulista, mineiro, baralho limpo)
 */
public interface Modo {

    static Modo fromString(String modoStr) {
        switch (modoStr) {
            case "M":
                return new ModoMineiro();
            case "P":
                return new ModoPaulista();
            case "V":
                return new ModoManilhaVelha();
            case "L":
                return new ModoBaralhoLimpo();
            default:
                throw new IllegalArgumentException("Modo deve ser M, P, V ou L");
        }
    }

    int pontuacaoParaMaoDeX();

    int valorInicialDaMao();

    /**
     * @return quantos pontos a equipe adversária leva se a equipe
     *         beneficiária aceitar a mão de X
     */
    int valorDaMaoDeX();

    /**
     * @return valor para o qual um partida cujo valor seja valorMao vai se houver um aumento,
     *         ou 0 se estivermos num valor que não permite aumento (ex.: 12 no truco paulista)
     */
    int valorSeHouverAumento(int valorMao);

    boolean isBaralhoLimpo();

    /**
     * @return True para manilhas fixas (sem "vira")
     */
    boolean isManilhaVelha();

}
