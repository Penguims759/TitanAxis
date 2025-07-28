package com.titanaxis.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JpaUtil {
    private static final Logger logger = AppLogger.getLogger();
    private static final EntityManagerFactory entityManagerFactory;

    static {
        try {
            ConfigurationManager config = ConfigurationManager.getInstance();
            
            // Criar propriedades dinâmicas baseadas na configuração
            Map<String, Object> properties = new HashMap<>();
            
            // Configurações de conexão do banco
            properties.put("jakarta.persistence.jdbc.driver", config.getDatabaseDriver());
            properties.put("jakarta.persistence.jdbc.url", config.getDatabaseUrl());
            properties.put("jakarta.persistence.jdbc.user", config.getDatabaseUsername());
            properties.put("jakarta.persistence.jdbc.password", config.getDatabasePassword());
            
            // Configurações do HikariCP
            properties.put("hibernate.hikari.minimumIdle", String.valueOf(config.getHikariMinimumIdle()));
            properties.put("hibernate.hikari.maximumPoolSize", String.valueOf(config.getHikariMaximumPoolSize()));
            properties.put("hibernate.hikari.idleTimeout", String.valueOf(config.getHikariIdleTimeout()));
            properties.put("hibernate.hikari.connectionTimeout", String.valueOf(config.getHikariConnectionTimeout()));
            properties.put("hibernate.hikari.maxLifetime", String.valueOf(config.getHikariMaxLifetime()));
            properties.put("hibernate.hikari.poolName", config.getHikariPoolName());
            
            // Configurações do Hibernate
            properties.put("hibernate.show_sql", String.valueOf(config.isHibernateShowSql()));
            properties.put("hibernate.format_sql", String.valueOf(config.isHibernateFormatSql()));
            properties.put("hibernate.hbm2ddl.auto", config.getHibernateHbm2ddlAuto());
            
            // Adicionar dialeto se especificado
            String dialect = config.getHibernateDialect();
            if (dialect != null && !dialect.trim().isEmpty()) {
                properties.put("hibernate.dialect", dialect);
            }
            
            // Log das configurações (sem mostrar senha)
            logger.info("Inicializando EntityManagerFactory com as seguintes configurações:");
            logger.info("Database URL: " + config.getDatabaseUrl());
            logger.info("Database User: " + config.getDatabaseUsername());
            logger.info("HikariCP Pool Size: " + config.getHikariMaximumPoolSize());
            logger.info("Hibernate Show SQL: " + config.isHibernateShowSql());
            
            // O nome "TitanAxisPU" deve ser o mesmo do persistence.xml
            entityManagerFactory = Persistence.createEntityManagerFactory("TitanAxisPU", properties);
            
            logger.info("EntityManagerFactory inicializado com sucesso");
            
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Falha ao criar o EntityManagerFactory: " + ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Obtém uma nova instância do EntityManager.
     * IMPORTANTE: O EntityManager deve ser fechado após o uso.
     */
    public static EntityManager getEntityManager() {
        try {
            return entityManagerFactory.createEntityManager();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao criar EntityManager", e);
            throw e;
        }
    }

    /**
     * Verifica se o EntityManagerFactory está aberto e funcional.
     */
    public static boolean isOpen() {
        return entityManagerFactory != null && entityManagerFactory.isOpen();
    }

    /**
     * Fecha o EntityManagerFactory.
     * Deve ser chamado apenas durante o shutdown da aplicação.
     */
    public static void close() {
        try {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
                logger.info("EntityManagerFactory fechado com sucesso");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erro ao fechar EntityManagerFactory", e);
        }
    }

    /**
     * Obtém informações sobre o status da conexão para monitoramento.
     */
    public static String getConnectionInfo() {
        ConfigurationManager config = ConfigurationManager.getInstance();
        return String.format("Database: %s, Pool: %s (max: %d)", 
            config.getDatabaseUrl(), 
            config.getHikariPoolName(),
            config.getHikariMaximumPoolSize());
    }
}