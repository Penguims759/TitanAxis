package com.titanaxis.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    private static final EntityManagerFactory entityManagerFactory;

    static {
        try {
            // O nome "TitanAxisPU" deve ser o mesmo do persistence.xml
            entityManagerFactory = Persistence.createEntityManagerFactory("TitanAxisPU");
        } catch (Throwable ex) {
            System.err.println("Falha ao criar o EntityManagerFactory." + ex);
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