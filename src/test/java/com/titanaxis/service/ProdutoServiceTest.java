package com.titanaxis.service;

import com.titanaxis.exception.EntityNotFoundException;
import com.titanaxis.exception.ValidationException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ProdutoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Produto Service Tests")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ProdutoService produtoService;

    private Usuario usuarioTeste;
    private Categoria categoriaTeste;
    private Produto produtoTeste;

    @BeforeEach
    void setUp() {
        usuarioTeste = new Usuario();
        usuarioTeste.setId(1);
        usuarioTeste.setNomeUsuario("teste");

        categoriaTeste = new Categoria();
        categoriaTeste.setId(1);
        categoriaTeste.setNome("Categoria Teste");

        produtoTeste = new Produto();
        produtoTeste.setId(1);
        produtoTeste.setNome("Produto Teste");
        produtoTeste.setDescricao("Descrição do produto teste");
        produtoTeste.setPreco(10.50);
        produtoTeste.setCategoria(categoriaTeste);
        produtoTeste.setAtivo(true);
    }

    @Test
    @DisplayName("Deve criar produto com sucesso")
    void deveCriarProdutoComSucesso() throws Exception {
        // Arrange
        when(transactionService.executeInTransaction(any())).thenAnswer(invocation -> {
            TransactionService.TransactionCallback<Produto> callback = invocation.getArgument(0);
            return callback.execute(entityManager);
        });
        when(produtoRepository.save(any(Produto.class), eq(usuarioTeste), eq(entityManager)))
                .thenReturn(produtoTeste);

        // Act
        Produto resultado = produtoService.criarProduto(produtoTeste, usuarioTeste);

        // Assert
        assertNotNull(resultado);
        assertEquals("Produto Teste", resultado.getNome());
        assertEquals(10.50, resultado.getPreco());
        assertTrue(resultado.isAtivo());
        
        verify(produtoRepository).save(produtoTeste, usuarioTeste, entityManager);
        verify(transactionService).executeInTransaction(any());
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao criar produto com nome vazio")
    void deveLancarValidationExceptionAoCriarProdutoComNomeVazio() {
        // Arrange
        Produto produtoInvalido = new Produto();
        produtoInvalido.setNome(""); // Nome vazio
        produtoInvalido.setPreco(10.50);
        produtoInvalido.setCategoria(categoriaTeste);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            produtoService.criarProduto(produtoInvalido, usuarioTeste);
        });

        assertTrue(exception.getFormattedMessage().contains("Nome do produto é obrigatório"));
        verify(produtoRepository, never()).save(any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao criar produto com preço inválido")
    void deveLancarValidationExceptionAoCriarProdutoComPrecoInvalido() {
        // Arrange
        Produto produtoInvalido = new Produto();
        produtoInvalido.setNome("Produto Teste");
        produtoInvalido.setPreco(-5.0); // Preço negativo
        produtoInvalido.setCategoria(categoriaTeste);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            produtoService.criarProduto(produtoInvalido, usuarioTeste);
        });

        assertTrue(exception.getFormattedMessage().contains("Preço deve ser maior que zero"));
        verify(produtoRepository, never()).save(any(), any(), any());
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void deveBuscarProdutoPorIdComSucesso() throws Exception {
        // Arrange
        when(transactionService.executeInTransaction(any())).thenAnswer(invocation -> {
            TransactionService.TransactionCallback<Optional<Produto>> callback = invocation.getArgument(0);
            return callback.execute(entityManager);
        });
        when(produtoRepository.findById(1, entityManager)).thenReturn(Optional.of(produtoTeste));

        // Act
        Optional<Produto> resultado = produtoService.buscarPorId(1);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Produto Teste", resultado.get().getNome());
        verify(produtoRepository).findById(1, entityManager);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar produto inexistente")
    void deveRetornarOptionalVazioAoBuscarProdutoInexistente() throws Exception {
        // Arrange
        when(transactionService.executeInTransaction(any())).thenAnswer(invocation -> {
            TransactionService.TransactionCallback<Optional<Produto>> callback = invocation.getArgument(0);
            return callback.execute(entityManager);
        });
        when(produtoRepository.findById(999, entityManager)).thenReturn(Optional.empty());

        // Act
        Optional<Produto> resultado = produtoService.buscarPorId(999);

        // Assert
        assertFalse(resultado.isPresent());
        verify(produtoRepository).findById(999, entityManager);
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void deveListarTodosProdutos() throws Exception {
        // Arrange
        Produto produto2 = new Produto();
        produto2.setId(2);
        produto2.setNome("Produto 2");
        produto2.setPreco(20.0);
        produto2.setCategoria(categoriaTeste);

        List<Produto> produtosEsperados = Arrays.asList(produtoTeste, produto2);

        when(transactionService.executeInTransaction(any())).thenAnswer(invocation -> {
            TransactionService.TransactionCallback<List<Produto>> callback = invocation.getArgument(0);
            return callback.execute(entityManager);
        });
        when(produtoRepository.findAll(entityManager)).thenReturn(produtosEsperados);

        // Act
        List<Produto> resultado = produtoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Produto Teste", resultado.get(0).getNome());
        assertEquals("Produto 2", resultado.get(1).getNome());
        verify(produtoRepository).findAll(entityManager);
    }

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void deveAtualizarProdutoComSucesso() throws Exception {
        // Arrange
        produtoTeste.setNome("Produto Atualizado");
        produtoTeste.setPreco(15.75);

        when(transactionService.executeInTransaction(any())).thenAnswer(invocation -> {
            TransactionService.TransactionCallback<Produto> callback = invocation.getArgument(0);
            return callback.execute(entityManager);
        });
        when(produtoRepository.save(any(Produto.class), eq(usuarioTeste), eq(entityManager)))
                .thenReturn(produtoTeste);

        // Act
        Produto resultado = produtoService.atualizarProduto(produtoTeste, usuarioTeste);

        // Assert
        assertNotNull(resultado);
        assertEquals("Produto Atualizado", resultado.getNome());
        assertEquals(15.75, resultado.getPreco());
        verify(produtoRepository).save(produtoTeste, usuarioTeste, entityManager);
    }

    @Test
    @DisplayName("Deve desativar produto com sucesso")
    void deveDesativarProdutoComSucesso() throws Exception {
        // Arrange
        when(transactionService.executeInTransaction(any())).thenAnswer(invocation -> {
            TransactionService.TransactionCallback<Void> callback = invocation.getArgument(0);
            return callback.execute(entityManager);
        });
        when(produtoRepository.findById(1, entityManager)).thenReturn(Optional.of(produtoTeste));

        // Act
        produtoService.desativarProduto(1, usuarioTeste);

        // Assert
        assertFalse(produtoTeste.isAtivo());
        verify(produtoRepository).findById(1, entityManager);
        verify(produtoRepository).save(produtoTeste, usuarioTeste, entityManager);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao desativar produto inexistente")
    void deveLancarEntityNotFoundExceptionAoDesativarProdutoInexistente() throws Exception {
        // Arrange
        when(transactionService.executeInTransaction(any())).thenAnswer(invocation -> {
            TransactionService.TransactionCallback<Void> callback = invocation.getArgument(0);
            return callback.execute(entityManager);
        });
        when(produtoRepository.findById(999, entityManager)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            produtoService.desativarProduto(999, usuarioTeste);
        });

        assertEquals("Produto com ID 999 não encontrado", exception.getMessage());
        verify(produtoRepository).findById(999, entityManager);
        verify(produtoRepository, never()).save(any(), any(), any());
    }
}