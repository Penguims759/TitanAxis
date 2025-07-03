package com.titanaxis.service;

import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransactionService {

    public <T> T executeInTransactionWithResult(Function<EntityManager, T> action) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            T result = action.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Falha na transação: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void executeInTransaction(Consumer<EntityManager> action) {
        executeInTransactionWithResult(em -> {
            action.accept(em);
            return null;
        });
    }
}