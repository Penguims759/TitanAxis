package com.titanaxis.service;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    // Mockamos a dependência (o repositório)
    @Mock
    private ClienteRepository clienteRepository;

    // Injetamos o mock no serviço que queremos testar
    @InjectMocks
    private ClienteService clienteService;

    @Test
    void salvar_deveChamarRepositorioSave_quandoAtorNaoForNulo() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("Cliente Teste", "contato", "endereco");
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        // Act
        clienteService.salvar(cliente, ator);

        // Assert
        // Verificamos se o método 'save' do repositório foi chamado exatamente uma vez
        // com os objetos corretos.
        verify(clienteRepository, times(1)).save(cliente, ator);
    }

    @Test
    void salvar_deveLancarExcecao_quandoAtorForNulo() {
        // Arrange
        Cliente cliente = new Cliente("Cliente Teste", "contato", "endereco");
        // O ator é nulo

        // Act & Assert
        // Verificamos se uma exceção é lançada quando tentamos salvar com um ator nulo.
        // Isto testa a sua lógica de validação no serviço.
        assertThrows(Exception.class, () -> {
            clienteService.salvar(cliente, null);
        });

        // Garantimos que, se a exceção for lançada, o repositório NUNCA é chamado.
        verify(clienteRepository, never()).save(any(), any());
    }

    @Test
    void deletar_deveChamarRepositorioDeleteById_quandoAtorNaoForNulo() throws Exception {
        // Arrange
        int clienteId = 123;
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        // Act
        clienteService.deletar(clienteId, ator);

        // Assert
        verify(clienteRepository, times(1)).deleteById(clienteId, ator);
    }

    @Test
    void deletar_deveLancarExcecao_quandoAtorForNulo() {
        // Arrange
        int clienteId = 123;

        // Act & Assert
        assertThrows(Exception.class, () -> {
            clienteService.deletar(clienteId, null);
        });

        verify(clienteRepository, never()).deleteById(anyInt(), any());
    }
}