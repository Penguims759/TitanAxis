package com.titanaxis.service;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ContasAReceber;
import com.titanaxis.model.MetaVenda;
import com.titanaxis.model.Venda;
import com.titanaxis.repository.FinanceiroRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

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

    @Test
    void gerarContasAReceberParaVenda_creates_entries_for_each_parcel() {
        FinanceiroRepository repo = mock(FinanceiroRepository.class);
        TransactionService txService = mock(TransactionService.class);
        FinanceiroService service = new FinanceiroService(repo, txService);

        EntityManager em = mock(EntityManager.class);
        Venda venda = new Venda();
        venda.setFormaPagamento("A Prazo");
        venda.setNumeroParcelas(3);
        venda.setValorTotal(300.0);

        service.gerarContasAReceberParaVenda(venda, em);

        ArgumentCaptor<ContasAReceber> captor = ArgumentCaptor.forClass(ContasAReceber.class);
        verify(repo, times(3)).saveContaAReceber(captor.capture(), eq(em));

        List<ContasAReceber> contas = captor.getAllValues();
        assertEquals(3, contas.size());
        for (int i = 0; i < contas.size(); i++) {
            ContasAReceber c = contas.get(i);
            assertEquals(venda, c.getVenda());
            assertEquals(i + 1, c.getNumeroParcela());
            assertEquals(100.0, c.getValorParcela());
            assertEquals("Pendente", c.getStatus());
        }
    }

    @Test
    void listarMetas_uses_repository() throws PersistenciaException {
        FinanceiroRepository repo = mock(FinanceiroRepository.class);
        TransactionService txService = mock(TransactionService.class);
        FinanceiroService service = new FinanceiroService(repo, txService);

        EntityManager em = mock(EntityManager.class);
        List<MetaVenda> metas = List.of(new MetaVenda());
        when(txService.executeInTransactionWithResult(any())).thenAnswer(inv -> {
            Function<EntityManager, List<MetaVenda>> func = inv.getArgument(0);
            return func.apply(em);
        });
        when(repo.findAllMetas(em)).thenReturn(metas);

        List<MetaVenda> result = service.listarMetas();
        assertEquals(metas, result);
        verify(repo).findAllMetas(em);
    }

    @Test
    void salvarMeta_calls_repository() throws PersistenciaException {
        FinanceiroRepository repo = mock(FinanceiroRepository.class);
        TransactionService txService = mock(TransactionService.class);
        FinanceiroService service = new FinanceiroService(repo, txService);

        EntityManager em = mock(EntityManager.class);
        MetaVenda meta = new MetaVenda();
        doAnswer(inv -> { Consumer<EntityManager> c = inv.getArgument(0); c.accept(em); return null; })
                .when(txService).executeInTransaction(any());

        service.salvarMeta(meta);

        verify(repo).saveMeta(meta, em);
    }

    @Test
    void deletarMeta_calls_repository() throws PersistenciaException {
        FinanceiroRepository repo = mock(FinanceiroRepository.class);
        TransactionService txService = mock(TransactionService.class);
        FinanceiroService service = new FinanceiroService(repo, txService);

        EntityManager em = mock(EntityManager.class);
        doAnswer(inv -> { Consumer<EntityManager> c = inv.getArgument(0); c.accept(em); return null; })
                .when(txService).executeInTransaction(any());

        service.deletarMeta(10);

        verify(repo).deleteMetaById(10, em);
    }
}
