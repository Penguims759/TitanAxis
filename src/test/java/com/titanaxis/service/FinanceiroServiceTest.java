package com.titanaxis.service;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ContasAReceber;
import com.titanaxis.repository.FinanceiroRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class FinanceiroServiceTest {

    @Test
    void listarContasAReceber_uses_repository() throws PersistenciaException {
        FinanceiroRepository repo = mock(FinanceiroRepository.class);
        TransactionService txService = mock(TransactionService.class);
        FinanceiroService service = new FinanceiroService(repo, txService);

        EntityManager em = mock(EntityManager.class);
        List<ContasAReceber> contas = List.of(new ContasAReceber());
        when(txService.executeInTransactionWithResult(any())).thenAnswer(inv -> {
            Function<EntityManager, List<ContasAReceber>> func = inv.getArgument(0);
            return func.apply(em);
        });
        when(repo.findContasAReceber(true, em)).thenReturn(contas);

        List<ContasAReceber> result = service.listarContasAReceber(true);

        assertEquals(contas, result);
        verify(repo).findContasAReceber(true, em);
    }

    @Test
    void registrarPagamento_updates_status_and_saves() throws PersistenciaException {
        FinanceiroRepository repo = mock(FinanceiroRepository.class);
        TransactionService txService = mock(TransactionService.class);
        FinanceiroService service = new FinanceiroService(repo, txService);

        EntityManager em = mock(EntityManager.class);
        ContasAReceber conta = new ContasAReceber();
        conta.setStatus("Pendente");
        when(repo.findContaAReceberById(5, em)).thenReturn(Optional.of(conta));
        doAnswer(inv -> { Consumer<EntityManager> c = inv.getArgument(0); c.accept(em); return null; })
                .when(txService).executeInTransaction(any());

        service.registrarPagamento(5);

        assertEquals("Pago", conta.getStatus());
        assertNotNull(conta.getDataPagamento());
        verify(repo).saveContaAReceber(conta, em);
    }
}
