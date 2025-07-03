package com.titanaxis.presenter;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.view.interfaces.UsuarioView;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioPresenterTest {

    @Mock
    private UsuarioView view;
    @Mock
    private AuthService authService;
    @InjectMocks
    private UsuarioPresenter presenter;

    @BeforeEach
    void setUp() {
        Usuario admin = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        when(authService.getUsuarioLogado()).thenReturn(Optional.of(admin));
        when(authService.getUsuarioLogadoId()).thenReturn(1);
        when(authService.listarUsuarios()).thenReturn(Collections.emptyList());
        presenter = new UsuarioPresenter(view, authService);
    }

    @Test
    void aoSalvar_deveCadastrarNovoUsuario() {
        when(view.getId()).thenReturn("");
        when(view.getUsername()).thenReturn("novo_user");
        when(view.getPassword()).thenReturn("senha123");
        when(view.getNivelAcesso()).thenReturn(NivelAcesso.PADRAO);
        when(authService.cadastrarUsuario(anyString(), anyString(), any(), any())).thenReturn(true);

        presenter.aoSalvar();

        verify(authService).cadastrarUsuario(eq("novo_user"), eq("senha123"), eq(NivelAcesso.PADRAO), any(Usuario.class));
        verify(view).mostrarMensagem(eq("Sucesso"), contains("adicionado"), eq(false));
    }

    @Test
    void aoApagar_naoDevePermitirApagarProprioUsuario() {
        when(view.getId()).thenReturn("1");
        presenter.aoApagar();
        verify(authService, never()).deletarUsuario(anyInt(), any(Usuario.class));
        verify(view).mostrarMensagem("Ação Inválida", "Não pode eliminar o seu próprio utilizador.", true);
    }
}