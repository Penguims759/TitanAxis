package com.titanaxis.service;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Usuario ator;
    private Categoria categoria;
    private Produto produto;

    @BeforeEach
    void setUp() {
        // Preparamos os objetos de teste
        ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        categoria = new Categoria(1, "Geral");
        produto = new Produto("Teste", "Desc", 10.0, categoria);
    }

    @Test
    void salvarProduto_deveChamarRepositorio_quandoAtorValido() throws Exception {
        // --- Act ---
        produtoService.salvarProduto(produto, ator);

        // --- Assert ---
        // Verificamos se o método 'save' do repositório foi chamado exatamente uma vez com os objetos corretos.
        verify(produtoRepository, times(1)).save(produto, ator);
    }

    @Test
    void salvarProduto_deveLancarExcecao_quandoAtorForNulo() {
        // --- Act & Assert ---
        // Verificamos se uma exceção é lançada quando tentamos salvar com um ator nulo.
        Exception exception = assertThrows(Exception.class, () -> {
            produtoService.salvarProduto(produto, null);
        });

        // Verificamos a mensagem da exceção.
        assertEquals("Nenhum utilizador autenticado para realizar esta operação.", exception.getMessage());
        // Verificamos que o repositório NUNCA foi chamado.
        verify(produtoRepository, never()).save(any(), any());
    }

    @Test
    void alterarStatusProduto_deveChamarRepositorio_quandoAtorValido() throws Exception {
        // --- Arrange ---
        int produtoId = 123;
        boolean novoStatus = false;

        // --- Act ---
        produtoService.alterarStatusProduto(produtoId, novoStatus, ator);

        // --- Assert ---
        // Verificamos se o método de atualização de status do repositório foi chamado com os parâmetros corretos.
        verify(produtoRepository, times(1)).updateStatusAtivo(produtoId, novoStatus, ator);
    }

    @Test
    void alterarStatusProduto_deveLancarExcecao_quandoAtorForNulo() {
        // --- Act & Assert ---
        Exception exception = assertThrows(Exception.class, () -> {
            produtoService.alterarStatusProduto(123, false, null);
        });

        assertEquals("Nenhum utilizador autenticado para realizar esta operação.", exception.getMessage());
        verify(produtoRepository, never()).updateStatusAtivo(anyInt(), anyBoolean(), any());
    }
}