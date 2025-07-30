package com.titanaxis.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JpaUtil {

    private static final Properties config = new Properties();
    private static EntityManagerFactory entityManagerFactory;

    static {
        try (InputStream in = JpaUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                config.load(in);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler config.properties: " + e.getMessage());
        }

        // EntityManagerFactory will be initialized lazily
        entityManagerFactory = null;
    }

    private static String getEnvOrProperty(String env, String propKey) {
        String val = System.getenv(env);
        return val != null ? val : config.getProperty(propKey);
    }

    private static synchronized void initFactory() {
        if (entityManagerFactory == null) {
            try {
                Properties props = new Properties();
                props.setProperty("jakarta.persistence.jdbc.url", getEnvOrProperty("DATABASE_URL", "database.url"));
                props.setProperty("jakarta.persistence.jdbc.user", getEnvOrProperty("DATABASE_USER", "database.user"));
                props.setProperty("jakarta.persistence.jdbc.password", getEnvOrProperty("DATABASE_PASSWORD", "database.password"));

                entityManagerFactory = Persistence.createEntityManagerFactory("TitanAxisPU", props);
            } catch (Throwable ex) {
                System.err.println("Falha ao criar o EntityManagerFactory." + ex);
                throw new ExceptionInInitializerError(ex);
            }
        }
    }

    public static EntityManager getEntityManager() {
        initFactory();
        return entityManagerFactory.createEntityManager();
    }

    public static void close() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
}