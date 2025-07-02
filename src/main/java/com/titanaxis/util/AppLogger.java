// src/main/java/com/titanaxis/util/AppLogger.java
package com.titanaxis.util; // ALTERADO

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Classe utilitária para configuração e acesso ao logger da aplicação.
 */
public class AppLogger {
    private static final Logger logger = Logger.getLogger(AppLogger.class.getName());

    static {
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
    }

    /**
     * Retorna a instância do logger da aplicação.
     * @return A instância do Logger.
     */
    public static Logger getLogger() {
        return logger;
    }
}