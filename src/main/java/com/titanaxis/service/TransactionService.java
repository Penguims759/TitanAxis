package com.titanaxis.service;

import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Serviço responsável por gerir o ciclo de vida das transações JPA.
 * Implementa o padrão "Unit of Work", garantindo que um conjunto de operações
 * seja executado como uma única unidade atómica.
 */
public class TransactionService {

    /**
     * Executa uma operação que retorna um valor dentro de uma única transação.
     * Inicia a transação, executa a ação, faz commit se tudo correr bem,
     * ou faz rollback em caso de qualquer exceção.
     * @param action A função a ser executada, que recebe um EntityManager e retorna um resultado.
     * @param <T> O tipo do valor de retorno.
     * @return O resultado da operação.
     * @throws RuntimeException se a transação falhar.
     */
    public <T> T executeInTransaction(Function<EntityManager, T> action) {
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
            // Re-lança a exceção para que a camada superior (ex: Presenter) possa tratá-la
            // e mostrar uma mensagem de erro ao utilizador.
            throw new RuntimeException("Falha na transação: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Executa uma operação que não retorna um valor (void) dentro de uma única transação.
     * @param action A ação a ser executada, que recebe um EntityManager.
     */
    public void executeInTransaction(Consumer<EntityManager> action) {
        // Reutiliza a lógica do método acima, tratando a ação como uma função que retorna null.
        executeInTransaction(em -> {
            action.accept(em);
            return null;
        });
    }
}