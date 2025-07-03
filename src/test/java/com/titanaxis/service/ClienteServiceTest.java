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

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void salvar_deveChamarRepositorioSave_quandoAtorNaoForNulo() throws Exception {
        // --- Arrange ---
        Cliente cliente = new Cliente("Cliente Teste", "contato", "endereco");
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        // --- Act ---
        clienteService.salvar(cliente, ator);

        // --- Assert ---
        verify(clienteRepository, times(1)).save(cliente, ator);
    }

    @Test
    void salvar_deveLancarExcecao_quandoAtorForNulo() {
        // --- Arrange ---
        Cliente cliente = new Cliente("Cliente Teste", "contato", "endereco");

        // --- Act & Assert ---
        assertThrows(Exception.class, () -> {
            clienteService.salvar(cliente, null);
        });

        verify(clienteRepository, never()).save(any(), any());
    }

    @Test
    void deletar_deveChamarRepositorioDeleteById_quandoAtorNaoForNulo() throws Exception {
        // --- Arrange ---
        int clienteId = 123;
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        // --- Act ---
        clienteService.deletar(clienteId, ator);

        // --- Assert ---
        verify(clienteRepository, times(1)).deleteById(clienteId, ator);
    }

    @Test
    void deletar_deveLancarExcecao_quandoAtorForNulo() {
        // --- Arrange ---
        int clienteId = 123;

        // --- Act & Assert ---
        assertThrows(Exception.class, () -> {
            clienteService.deletar(clienteId, null);
        });

        verify(clienteRepository, never()).deleteById(anyInt(), any());
    }
}