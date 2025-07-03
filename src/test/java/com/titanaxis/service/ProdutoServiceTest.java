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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Permite stubs de setup não usados em todos os testes
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private TransactionService transactionService; // O serviço agora depende disto

    @InjectMocks
    private ProdutoService produtoService;

    private Usuario ator;
    private Produto produto;

    @BeforeEach
    void setUp() {
        ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        Categoria categoria = new Categoria(1, "Geral");
        produto = new Produto("Teste", "Desc", 10.0, categoria);

        // Configuração do mock do TransactionService para executar a ação que lhe é passada
        doAnswer(invocation -> {
            var action = invocation.getArgument(0, java.util.function.Function.class);
            return action.apply(mock(EntityManager.class));
        }).when(transactionService).executeInTransaction(any(java.util.function.Function.class));
    }

    @Test
    void salvarProduto_deveChamarRepositorio_quandoAtorValido() throws Exception {
        produtoService.salvarProduto(produto, ator);
        verify(produtoRepository, times(1)).save(eq(produto), eq(ator), any(EntityManager.class));
    }

    @Test
    void salvarProduto_deveLancarExcecao_quandoAtorForNulo() {
        Exception exception = assertThrows(Exception.class, () -> {
            produtoService.salvarProduto(produto, null);
        });
        assertEquals("Nenhum utilizador autenticado para realizar esta operação.", exception.getMessage());
        verify(produtoRepository, never()).save(any(), any(), any());
    }

    @Test
    void alterarStatusProduto_deveChamarRepositorio_quandoAtorValido() throws Exception {
        int produtoId = 123;
        boolean novoStatus = false;

        produtoService.alterarStatusProduto(produtoId, novoStatus, ator);

        // CORREÇÃO: Verificamos a chamada ao método com a assinatura correta (4 argumentos)
        verify(produtoRepository, times(1)).updateStatusAtivo(eq(produtoId), eq(novoStatus), eq(ator), any(EntityManager.class));
    }

    @Test
    void alterarStatusProduto_deveLancarExcecao_quandoAtorForNulo() {
        Exception exception = assertThrows(Exception.class, () -> {
            produtoService.alterarStatusProduto(123, false, null);
        });
        assertEquals("Nenhum utilizador autenticado para realizar esta operação.", exception.getMessage());
        verify(produtoRepository, never()).updateStatusAtivo(anyInt(), anyBoolean(), any(), any());
    }
}