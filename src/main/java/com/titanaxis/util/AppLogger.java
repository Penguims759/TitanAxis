package com.titanaxis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitária para configuração e acesso ao logger da aplicação.
 */
public class AppLogger {
    private static final Logger logger = LoggerFactory.getLogger(AppLogger.class);

    /**
     * Retorna a instância do logger da aplicação.
     * @return A instância do Logger.
     */
    public static Logger getLogger() {
        return logger;
    }
}