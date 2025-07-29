package com.titanaxis.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;

import com.titanaxis.util.AppLogger;

/** Utility class for managing the JPA EntityManagerFactory. */

public class JpaUtil {

    private static final Logger logger = AppLogger.getLogger();
    private static final EntityManagerFactory entityManagerFactory;

    static {
        try {
            // O nome "TitanAxisPU" deve ser o mesmo do persistence.xml
            entityManagerFactory = Persistence.createEntityManagerFactory("TitanAxisPU");
        } catch (Throwable ex) {
            logger.error("Falha ao criar o EntityManagerFactory.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public static void close() {
        entityManagerFactory.close();
    }
}