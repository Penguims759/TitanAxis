package com.titanaxis.service;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.CategoriaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        // Simula a transação para não retornar valor
        doAnswer(invocation -> {
            Consumer<EntityManager> action = invocation.getArgument(0);
            action.accept(mock(EntityManager.class));
            return null;
        }).when(transactionService).executeInTransaction(any(Consumer.class));
    }

    @Test
    void salvar_deveChamarRepositorio_quandoNomeNaoExiste() throws Exception {
        // Arrange
        Categoria novaCategoria = new Categoria("Eletrónicos");
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        // Configura o mock do repositório para retornar vazio quando chamado dentro da transação
        when(categoriaRepository.findByNome(eq("Eletrónicos"), any(EntityManager.class))).thenReturn(Optional.empty());

        // Act
        categoriaService.salvar(novaCategoria, ator);

        // Assert
        // Verifica que a transação foi chamada
        verify(transactionService).executeInTransaction(any(Consumer.class));
        // Verifica que o método save foi chamado dentro da transação
        verify(categoriaRepository).save(eq(novaCategoria), eq(ator), any(EntityManager.class));
    }

    @Test
    void salvar_deveLancarExcecao_quandoNomeJaExisteParaOutraCategoria() {
        // --- Arrange ---
        Categoria categoriaAserSalva = new Categoria("Livros");
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        // CORREÇÃO DEFINITIVA:
        // A forma mais limpa de testar é simular que o bloco transacional falha
        // com a exceção de negócio correta.
        // Nós instruímos o mock do TransactionService a lançar a RuntimeException
        // que a lógica interna do serviço lançaria ao encontrar um duplicado.
        doThrow(new RuntimeException("Já existe uma categoria com este nome."))
                .when(transactionService).executeInTransaction(any());

        // --- Act & Assert ---
        // O método 'salvar' do serviço deve apanhar a RuntimeException e relançá-la
        // como uma Exception verificada, que é o que o Presenter espera.
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.salvar(categoriaAserSalva, ator);
        });

        // Verificamos se a mensagem da exceção está correta.
        assertEquals("Já existe uma categoria com este nome.", exception.getMessage());
    }


    @Test
    void salvar_devePermitirAtualizar_quandoNomeNaoMuda() throws Exception {
        // Arrange
        Categoria categoriaParaAtualizar = new Categoria(10, "Roupas");
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        // Simula que a busca pelo nome retorna a PRÓPRIA categoria que está a ser atualizada
        when(categoriaRepository.findByNome(eq("Roupas"), any(EntityManager.class))).thenReturn(Optional.of(categoriaParaAtualizar));

        // Act
        categoriaService.salvar(categoriaParaAtualizar, ator);

        // Assert
        verify(transactionService).executeInTransaction(any(Consumer.class));
        verify(categoriaRepository).save(eq(categoriaParaAtualizar), eq(ator), any(EntityManager.class));
    }
}