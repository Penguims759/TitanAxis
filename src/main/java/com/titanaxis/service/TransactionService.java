// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/service/TransactionService.java
package com.titanaxis.service;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransactionService {

    /**
     * Executa uma ação dentro de uma transação e retorna um resultado.
     * Captura exceções de persistência e as encapsula em uma PersistenciaException.
     *
     * @param action A função a ser executada.
     * @param <T> O tipo do resultado da função.
     * @return O resultado da função.
     * @throws PersistenciaException se ocorrer um erro na base de dados ou outra exceção durante a transação.
     */
    public <T> T executeInTransactionWithResult(Function<EntityManager, T> action) throws PersistenciaException {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            T result = action.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenciaException(I18n.getString("service.transaction.error.communication"), e); // ALTERADO
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenciaException(I18n.getString("service.transaction.error.generic", e.getMessage()), e); // ALTERADO
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Executa uma ação sem retorno dentro de uma transação.
     *
     * @param action A ação a ser executada.
     * @throws PersistenciaException se ocorrer um erro na base de dados.
     */
    public void executeInTransaction(Consumer<EntityManager> action) throws PersistenciaException {
        executeInTransactionWithResult(em -> {
            action.accept(em);
            return null;
        });
    }
}