package com.titanaxis.service;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ProdutoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ProdutoService produtoService;

    private Usuario ator;
    private Produto produto;

    @BeforeEach
    void setUp() {
        ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        Categoria categoria = new Categoria(1, "Geral");
        produto = new Produto("Teste", "Desc", 10.0, categoria);

        // Configuração do mock do TransactionService
        doAnswer(invocation -> {
            Function<EntityManager, Object> action = invocation.getArgument(0);
            return action.apply(mock(EntityManager.class));
        }).when(transactionService).executeInTransactionWithResult(any());

        doAnswer(invocation -> {
            Consumer<EntityManager> action = invocation.getArgument(0);
            action.accept(mock(EntityManager.class));
            return null;
        }).when(transactionService).executeInTransaction(any(Consumer.class));
    }

    @Test
    void salvarProduto_deveChamarRepositorioDentroDeUmaTransacao() throws Exception {
        produtoService.salvarProduto(produto, ator);

        verify(transactionService).executeInTransactionWithResult(any());
        verify(produtoRepository).save(eq(produto), eq(ator), any(EntityManager.class));
    }

    @Test
    void salvarProduto_deveLancarExcecao_quandoAtorForNulo() {
        Exception exception = assertThrows(Exception.class, () -> produtoService.salvarProduto(produto, null));
        assertEquals("Nenhum utilizador autenticado para realizar esta operação.", exception.getMessage());

        verify(transactionService, never()).executeInTransactionWithResult(any());
    }

    @Test
    void alterarStatusProduto_deveChamarRepositorioDentroDeUmaTransacao() throws Exception {
        produtoService.alterarStatusProduto(123, false, ator);

        verify(transactionService).executeInTransaction(any(Consumer.class));
        verify(produtoRepository).updateStatusAtivo(eq(123), eq(false), eq(ator), any(EntityManager.class));
    }
}