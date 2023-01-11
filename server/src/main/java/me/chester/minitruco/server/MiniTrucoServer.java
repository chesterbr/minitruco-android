package me.chester.minitruco.server;

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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MiniTrucoServer {

    public static final int PORTA_SERVIDOR = 6912;

    /**
     * Versão do servidor (3.0 é a primeira pós-J2ME)
     */
    public static final String VERSAO_SERVER = "3.0";

    public static DateFormat dfStartup;

    public static Date dataStartup;

    public static String strDataStartup;

    public static void main(String[] args) {

        try {

            // Guarda a data de início do servidor num formato apropriado para HTTP
            // vide JogadorContectado.serveArquivosApplet

            dfStartup = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",
                    Locale.US);
            dataStartup = new Date();
            strDataStartup = dfStartup.format(dataStartup);

            ServerLogger
                    .evento("Servidor Inicializado, pronto para escutar na porta "
                            + PORTA_SERVIDOR);

            try {
                ServerSocket s = new ServerSocket(PORTA_SERVIDOR);
                while (true) {
                    Socket sCliente = s.accept();
                    JogadorConectado j = new JogadorConectado(sCliente);
                    Thread t = new Thread(j);
                    t.start();
                }
            } catch (IOException e) {
                ServerLogger.evento(e, "Erro de I/O no ServerSocket, saindo do programa");
            }

        } finally {
            ServerLogger.evento("Servidor Finalizado");
        }

    }

}