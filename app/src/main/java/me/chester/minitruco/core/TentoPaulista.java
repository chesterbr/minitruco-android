package me.chester.minitruco.core;

/*
 * Copyright © 2011 Guilherme Caram <gcaram@gmail.com>
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

import java.util.HashMap;
import java.util.Map;

/**
 * Forma de pontuação dos tentos
 */
public class TentoPaulista implements Tento {

    /**
     *
     */
    private static Map<Integer, Integer> valorTento = new HashMap<Integer, Integer>();

    /**
     *
     */
    private static Map<Integer, Integer> valorMao = new HashMap<Integer, Integer>();

    static {
        valorTento.put(1, 3);
        valorTento.put(3, 6);
        valorTento.put(6, 9);
        valorTento.put(9, 12);

        valorMao.put(1, 1);
        valorMao.put(3, 2);
        valorMao.put(6, 3);
        valorMao.put(9, 4);
    }

    public int calcValorTento(int valorT) {
        Integer result = valorTento.get(valorT);
        return (result == null ? 0 : result);
    }

    public int calcValorMao(int valorM) {
        Integer result = valorMao.get(valorM);
        return (result == null ? 0 : result);
    }

    public int inicializaMao() {
        return 1;
    }

    public int inicializaPenultimaMao() {
        return 3;
    }

    public int valorPenultimaMao() {
        return 11;
    }
}