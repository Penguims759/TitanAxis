package com.titanaxis.service;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    @Test
    void executeInTransactionWithResult_commits_on_success() throws PersistenciaException {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);
        MockedStatic<JpaUtil> utilMock = mockStatic(JpaUtil.class);
        utilMock.when(JpaUtil::getEntityManager).thenReturn(em);
        TransactionService service = new TransactionService();

        Function<EntityManager, String> function = e -> "ok";
        String result = service.executeInTransactionWithResult(function);

        assertEquals("ok", result);
        verify(tx).begin();
        verify(tx).commit();
        verify(tx, never()).rollback();
        verify(em).close();
        utilMock.close();
    }

    @Test
    void executeInTransactionWithResult_rolls_back_on_persistence_exception() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);
        doThrow(new PersistenceException("fail"))
                .when(tx).commit();

        try (MockedStatic<JpaUtil> utilMock = mockStatic(JpaUtil.class)) {
            utilMock.when(JpaUtil::getEntityManager).thenReturn(em);
            TransactionService service = new TransactionService();

            assertThrows(PersistenciaException.class,
                    () -> service.executeInTransactionWithResult(e -> "value"));

            verify(tx).begin();
            verify(tx).rollback();
            verify(em).close();
        }
    }
}
