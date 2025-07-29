package com.titanaxis.service;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Categoria;
import com.titanaxis.repository.CategoriaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CategoriaServiceTest {

    @Test
    void listarTodasCategorias_uses_repository() throws PersistenciaException {
        CategoriaRepository repo = mock(CategoriaRepository.class);
        TransactionService txService = mock(TransactionService.class);
        CategoriaService service = new CategoriaService(repo, txService);

        EntityManager em = mock(EntityManager.class);
        List<Categoria> categorias = List.of(new Categoria(1, "teste"));
        when(repo.findAllWithProductCount(em)).thenReturn(categorias);
        when(txService.executeInTransactionWithResult(any())).thenAnswer(invocation -> {
            Function<EntityManager, List<Categoria>> func = invocation.getArgument(0);
            return func.apply(em);
        });

        List<Categoria> result = service.listarTodasCategorias();
        assertEquals(categorias, result);
        verify(repo).findAllWithProductCount(em);
    }
}
