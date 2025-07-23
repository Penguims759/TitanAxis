package com.titanaxis.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class UIPersonalizationService {

    private final Properties userPreferences = new Properties();
    private final String username;
    private final File configFile;

    public UIPersonalizationService(String username) {
        this.username = username;
        String safeUsername = username.replaceAll("[^a-zA-Z0-9.-]", "_");

        // ALTERADO: Define um diretório para as preferências
        File prefsDir = new File("user_prefs");
        // Cria o diretório se ele não existir
        if (!prefsDir.exists()) {
            prefsDir.mkdir();
        }

        this.configFile = new File(prefsDir, "prefs_" + safeUsername + ".properties");
        loadPreferences();
    }

    private void loadPreferences() {
        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                userPreferences.load(in);
            } catch (IOException e) {
                System.err.println("Erro ao carregar preferências do usuário: " + e.getMessage());
            }
        }
    }

    public void savePreference(String key, String value) {
        userPreferences.setProperty(key, value);
        try (FileOutputStream out = new FileOutputStream(configFile)) {
            userPreferences.store(out, "Preferências de UI para " + username);
        } catch (IOException e) {
            System.err.println("Erro ao salvar preferência do usuário: " + e.getMessage());
        }
    }

    public String getPreference(String key, String defaultValue) {
        return userPreferences.getProperty(key, defaultValue);
    }
}