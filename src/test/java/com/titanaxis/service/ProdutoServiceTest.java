package com.titanaxis.service;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// AQUI ESTÁ A CORREÇÃO: Importa todos os métodos de asserção do JUnit
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void salvarProduto_deveChamarRepositorio_quandoAtorValido() throws Exception {
        Produto produto = new Produto("Teste", "Desc", 10.0, 1);
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        produtoService.salvarProduto(produto, ator);

        verify(produtoRepository, times(1)).save(produto, ator);
    }

    @Test
    void salvarProduto_deveLancarExcecao_quandoAtorForNulo() {
        Produto produto = new Produto("Teste", "Desc", 10.0, 1);

        Exception exception = assertThrows(Exception.class, () -> {
            produtoService.salvarProduto(produto, null);
        });

        assertEquals("Nenhum utilizador autenticado para realizar esta operação.", exception.getMessage());
        verify(produtoRepository, never()).save(any(), any());
    }

    @Test
    void alterarStatusProduto_deveChamarRepositorio_quandoAtorValido() throws Exception {
        int produtoId = 123;
        boolean novoStatus = false;
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        produtoService.alterarStatusProduto(produtoId, novoStatus, ator);

        verify(produtoRepository, times(1)).updateStatusAtivo(produtoId, novoStatus, ator);
    }

    @Test
    void alterarStatusProduto_deveLancarExcecao_quandoAtorForNulo() {
        int produtoId = 123;
        boolean novoStatus = false;

        Exception exception = assertThrows(Exception.class, () -> {
            produtoService.alterarStatusProduto(produtoId, novoStatus, null);
        });

        assertEquals("Nenhum utilizador autenticado para realizar esta operação.", exception.getMessage());
        verify(produtoRepository, never()).updateStatusAtivo(anyInt(), anyBoolean(), any());
    }
}