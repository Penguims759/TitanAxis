package com.titanaxis.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gerenciador de configurações da aplicação.
 * Carrega propriedades de arquivos e variáveis de ambiente.
 */
public class ConfigurationManager {
    private static final Logger logger = AppLogger.getLogger();
    private static final Properties properties = new Properties();
    private static final String DEFAULT_CONFIG_FILE = "application.properties";
    private static ConfigurationManager instance;

    private ConfigurationManager() {
        loadConfiguration();
    }

    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    private void loadConfiguration() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                logger.info("Configurações carregadas com sucesso de: " + DEFAULT_CONFIG_FILE);
            } else {
                logger.warning("Arquivo de configuração não encontrado: " + DEFAULT_CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao carregar configurações", e);
        }
    }

    /**
     * Obtém uma propriedade como String.
     * Suporta variáveis de ambiente no formato ${VAR_NAME:default_value}
     */
    public String getProperty(String key) {
        return getProperty(key, null);
    }

    /**
     * Obtém uma propriedade como String com valor padrão.
     */
    public String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        return resolveEnvironmentVariables(value);
    }

    /**
     * Obtém uma propriedade como Integer.
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warning("Valor inválido para propriedade " + key + ": " + value + ". Usando valor padrão: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Obtém uma propriedade como Boolean.
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Obtém uma propriedade como Long.
     */
    public long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            logger.warning("Valor inválido para propriedade " + key + ": " + value + ". Usando valor padrão: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Resolve variáveis de ambiente no formato ${VAR_NAME:default_value}
     */
    private String resolveEnvironmentVariables(String value) {
        if (value == null) {
            return null;
        }

        String result = value;
        int startIndex = result.indexOf("${");
        
        while (startIndex != -1) {
            int endIndex = result.indexOf("}", startIndex);
            if (endIndex == -1) {
                break;
            }

            String placeholder = result.substring(startIndex + 2, endIndex);
            String[] parts = placeholder.split(":", 2);
            String envVar = parts[0];
            String defaultVal = parts.length > 1 ? parts[1] : "";

            String envValue = System.getenv(envVar);
            if (envValue == null) {
                envValue = System.getProperty(envVar, defaultVal);
            }

            result = result.substring(0, startIndex) + envValue + result.substring(endIndex + 1);
            startIndex = result.indexOf("${");
        }

        return result;
    }

    // Métodos de conveniência para configurações específicas

    public String getDatabaseUrl() {
        return getProperty("database.url");
    }

    public String getDatabaseUsername() {
        return getProperty("database.username");
    }

    public String getDatabasePassword() {
        return getProperty("database.password");
    }

    public String getDatabaseDriver() {
        return getProperty("database.driver");
    }

    public int getHikariMinimumIdle() {
        return getIntProperty("hikari.minimum.idle", 5);
    }

    public int getHikariMaximumPoolSize() {
        return getIntProperty("hikari.maximum.pool.size", 20);
    }

    public long getHikariIdleTimeout() {
        return getLongProperty("hikari.idle.timeout", 30000);
    }

    public long getHikariConnectionTimeout() {
        return getLongProperty("hikari.connection.timeout", 30000);
    }

    public long getHikariMaxLifetime() {
        return getLongProperty("hikari.max.lifetime", 1800000);
    }

    public String getHikariPoolName() {
        return getProperty("hikari.pool.name");
    }

    public boolean isHibernateShowSql() {
        return getBooleanProperty("hibernate.show.sql", false);
    }

    public boolean isHibernateFormatSql() {
        return getBooleanProperty("hibernate.format.sql", true);
    }

    public String getHibernateHbm2ddlAuto() {
        return getProperty("hibernate.hbm2ddl.auto");
    }

    public String getHibernateDialect() {
        return getProperty("hibernate.dialect");
    }

    public String getAppName() {
        return getProperty("app.name");
    }

    public String getAppVersion() {
        return getProperty("app.version");
    }

    public String getDefaultLocale() {
        return getProperty("app.default.locale");
    }

    public String getDefaultTheme() {
        return getProperty("app.default.theme");
    }

    public String getAiModelsPath() {
        return getProperty("ai.models.path");
    }

    public String getAiNerModelPt() {
        return getProperty("ai.ner.model.pt");
    }

    public String getAiNerModelEn() {
        return getProperty("ai.ner.model.en");
    }

    public String getAiIntentModelPt() {
        return getProperty("ai.intent.model.pt");
    }

    public int getPasswordMinLength() {
        return getIntProperty("security.password.min.length", 8);
    }

    public long getSessionTimeout() {
        return getLongProperty("security.session.timeout", 3600000);
    }

    public String getReportsOutputPath() {
        return getProperty("reports.output.path");
    }

    public String getReportsTempPath() {
        return getProperty("reports.temp.path");
    }
}