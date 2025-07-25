package com.titanaxis.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class I18n {

    private static final String SYSTEM_BUNDLE_BASE_NAME = "i18n.messages";
    private static final String AI_BUNDLE_BASE_NAME = "i18n.messages_ai"; // NOVO
    private static ResourceBundle systemBundle;
    private static ResourceBundle aiBundle; // NOVO
    private static Locale currentLocale;
    private static final Logger logger = AppLogger.getLogger();

    static {
        setLocale(new Locale("pt", "BR"));
    }

    private I18n() {}

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        try {
            systemBundle = ResourceBundle.getBundle(SYSTEM_BUNDLE_BASE_NAME, currentLocale, Thread.currentThread().getContextClassLoader());
            aiBundle = ResourceBundle.getBundle(AI_BUNDLE_BASE_NAME, currentLocale, Thread.currentThread().getContextClassLoader()); // NOVO
            logger.info("ResourceBundles para o locale '" + locale + "' carregados com sucesso.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Não foi possível carregar os ResourceBundles para o locale " + locale, e);
            // Fallback para bundles vazios para evitar NullPointerException
            systemBundle = createEmptyBundle();
            aiBundle = createEmptyBundle();
        }
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static String getString(String key) {
        try {
            // Procura primeiro no bundle da IA
            if (aiBundle.containsKey(key)) {
                return aiBundle.getString(key);
            }
            // Se não encontrar, procura no bundle do sistema
            return systemBundle.getString(key);
        } catch (MissingResourceException e) {
            logger.warning("Chave de internacionalização não encontrada em nenhum bundle: " + key);
            return "!" + key + "!";
        }
    }

    public static String getString(String key, Object... args) {
        return String.format(getString(key), args);
    }

    private static ResourceBundle createEmptyBundle() {
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return "!" + key + "!";
            }
            @Override
            public java.util.Enumeration<String> getKeys() {
                return java.util.Collections.emptyEnumeration();
            }
        };
    }
}