package me.chester.minitruco.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Formata os logs tanto do core, quanto do servidor
 */
public class LogFormatter extends Formatter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        return new StringBuilder()
            .append(DATE_FORMAT.format(new Date(record.getMillis())))
            .append(" [")
            .append(record.getLevel().getName())
            .append("] ")
            .append(nomeDaClasse(record))
            .append(".")
            .append(record.getSourceMethodName())
            .append(": ")
            .append(formatMessage(record))
            .append("\n")
            .toString();
    }

    private String nomeDaClasse(LogRecord record) {
        String nomeLongo = record.getSourceClassName();
        if (nomeLongo == null) {
            // Difícil, mas em último caso pega o nome do logger
            return record.getLoggerName();
        }
        int i = nomeLongo.lastIndexOf('.');
        if (i == -1) {
            return nomeLongo;
        } else {
            return nomeLongo.substring(i + 1);
        }
    }
}
