package com.titanaxis.presenter;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ClienteService;
import com.titanaxis.view.interfaces.ClienteView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClientePresenterTest {

    @Mock
    private ClienteView view;

    @Mock
    private ClienteService clienteService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ClientePresenter presenter;

    @BeforeEach
    void setUp() {
        Usuario admin = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        when(authService.getUsuarioLogado()).thenReturn(Optional.of(admin));
        when(clienteService.listarTodos()).thenReturn(Collections.emptyList());
        presenter = new ClientePresenter(view, clienteService, authService);
    }

    @Test
    void aoSalvar_deveCriarNovoCliente_eLimparCampos() throws Exception {
        // Arrange
        when(view.getId()).thenReturn("");
        when(view.getNome()).thenReturn("Cliente Teste");
        when(view.getContato()).thenReturn("");
        when(view.getEndereco()).thenReturn("");

        // Act
        presenter.aoSalvar();

        // Assert
        verify(clienteService).salvar(any(Cliente.class), any(Usuario.class));
        verify(view).mostrarMensagem(eq("Sucesso"), contains("adicionado"), eq(false));

        // **AQUI ESTÁ A CORREÇÃO**
        // Verificamos as chamadas individuais que o presenter.aoLimpar() executa na view.
        verify(view).setId("");
        verify(view).setNome("");
        verify(view).setContato("");
        verify(view).setEndereco("");
        verify(view).clearTableSelection();
    }

    @Test
    void aoApagar_deveApagarCliente_eLimparCampos() throws Exception {
        // Arrange
        when(view.getId()).thenReturn("1");
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(true);

        // Act
        presenter.aoApagar();

        // Assert
        verify(clienteService).deletar(eq(1), any(Usuario.class));
        verify(view).mostrarMensagem("Sucesso", "Cliente eliminado com sucesso!", false);

        // **AQUI ESTÁ A CORREÇÃO**
        // Verificamos as chamadas individuais que o presenter.aoLimpar() executa na view.
        verify(view).setId("");
        verify(view).setNome("");
        verify(view).setContato("");
        verify(view).setEndereco("");
        verify(view).clearTableSelection();
    }
}