// src/main/java/com/titanaxis/util/I18n.java
package com.titanaxis.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * Classe utilitária para a Internacionalização (i18n) da aplicação.
 * Carrega os textos a partir dos ficheiros de propriedades (ResourceBundle).
 */
public final class I18n {

    private static final String BUNDLE_BASE_NAME = "i18n.messages";
    private static ResourceBundle bundle;

    static {
        // Define o Locale padrão. No futuro, isto pode vir de uma configuração do utilizador.
        // Por agora, vamos fixar em pt_BR.
        Locale defaultLocale = new Locale("pt", "BR");
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, defaultLocale);
        } catch (Exception e) {
            AppLogger.getLogger().log(Level.SEVERE, "Não foi possível carregar o ResourceBundle: " + BUNDLE_BASE_NAME, e);
            // Em caso de falha, cria um ResourceBundle vazio para evitar NullPointerException
            bundle = new ResourceBundle() {
                @Override
                protected Object handleGetObject(String key) {
                    return "!" + key + "!"; // Retorna a chave com marcadores para indicar que a tradução falhou
                }
                @Override
                public java.util.Enumeration<String> getKeys() {
                    return java.util.Collections.emptyEnumeration();
                }
            };
        }
    }

    /**
     * Impede a instanciação da classe utilitária.
     */
    private I18n() {}

    /**
     * Obtém uma string de tradução com base na chave fornecida.
     *
     * @param key A chave da string no ficheiro de propriedades.
     * @return A string traduzida, ou a chave entre '!' se não for encontrada.
     */
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (java.util.MissingResourceException e) {
            AppLogger.getLogger().warning("Chave de internacionalização não encontrada: " + key);
            return "!" + key + "!";
        }
    }

    /**
     * Obtém uma string de tradução formatada com argumentos.
     *
     * @param key A chave da string formatada no ficheiro de propriedades.
     * @param args Os argumentos para preencher na string.
     * @return A string traduzida e formatada.
     */
    public static String getString(String key, Object... args) {
        return String.format(getString(key), args);
    }
}