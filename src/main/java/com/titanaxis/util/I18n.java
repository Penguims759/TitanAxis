package com.titanaxis.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class I18n {

    private static final String BUNDLE_BASE_NAME = "i18n.messages";
    private static ResourceBundle bundle;
    private static Locale currentLocale;

    static {
        // Define o locale padrão na inicialização
        setLocale(new Locale("pt", "BR"));
    }

    private I18n() {}

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        try {
            // Garante que o ResourceBundle seja carregado usando o ClassLoader da thread atual, o que é mais seguro em aplicações Swing.
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, currentLocale, Thread.currentThread().getContextClassLoader());
            AppLogger.getLogger().info("ResourceBundle para o locale '" + locale + "' carregado com sucesso.");
        } catch (Exception e) {
            AppLogger.getLogger().log(Level.SEVERE, "Não foi possível carregar o ResourceBundle: " + BUNDLE_BASE_NAME + " para o locale " + locale, e);
            // Fallback para um ResourceBundle vazio para evitar NullPointerException
            bundle = new ResourceBundle() {
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

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (java.util.MissingResourceException e) {
            AppLogger.getLogger().warning("Chave de internacionalização não encontrada: " + key);
            return "!" + key + "!";
        }
    }

    public static String getString(String key, Object... args) {
        return String.format(getString(key), args);
    }
}