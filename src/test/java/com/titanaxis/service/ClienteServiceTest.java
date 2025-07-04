package com.titanaxis.service;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ClienteRepository;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Permite stubs não utilizados
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ClienteService clienteService;

    // ALTERADO: Configuração do mock do TransactionService
    @BeforeEach
    void setUp() {
        // Simula a execução de uma transação que retorna um valor
        doAnswer(invocation -> {
            Function<EntityManager, Object> action = invocation.getArgument(0);
            return action.apply(mock(EntityManager.class));
        }).when(transactionService).executeInTransactionWithResult(any());

        // Simula a execução de uma transação que não retorna valor (void)
        doAnswer(invocation -> {
            Consumer<EntityManager> action = invocation.getArgument(0);
            action.accept(mock(EntityManager.class));
            return null;
        }).when(transactionService).executeInTransaction(any(Consumer.class));
    }

    @Test
    void salvar_deveChamarRepositorioSave_dentroDeUmaTransacao() throws Exception {
        Cliente cliente = new Cliente("Cliente Teste", "contato", "endereco");
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        clienteService.salvar(cliente, ator);

        // Verifica que o serviço de transação foi chamado
        verify(transactionService).executeInTransactionWithResult(any());
        // Verifica que, DENTRO da transação, o repositório foi chamado com os argumentos corretos
        verify(clienteRepository).save(eq(cliente), eq(ator), any(EntityManager.class));
    }

    @Test
    void salvar_deveLancarExcecao_quandoAtorForNulo() {
        Cliente cliente = new Cliente("Cliente Teste", "contato", "endereco");

        assertThrows(Exception.class, () -> clienteService.salvar(cliente, null));

        // Garante que nenhuma transação foi iniciada
        verify(transactionService, never()).executeInTransactionWithResult(any());
    }

    @Test
    void deletar_deveChamarRepositorioDeleteById_dentroDeUmaTransacao() throws Exception {
        int clienteId = 123;
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        clienteService.deletar(clienteId, ator);

        // Verifica que o serviço de transação foi chamado
        verify(transactionService).executeInTransaction(any(Consumer.class));
        // Verifica que, DENTRO da transação, o repositório foi chamado com os argumentos corretos
        verify(clienteRepository).deleteById(eq(clienteId), eq(ator), any(EntityManager.class));
    }

    @Test
    void deletar_deveLancarExcecao_quandoAtorForNulo() {
        int clienteId = 123;

        assertThrows(Exception.class, () -> clienteService.deletar(clienteId, null));

        // Garante que nenhuma transação foi iniciada
        verify(transactionService, never()).executeInTransaction(any(Consumer.class));
    }
}