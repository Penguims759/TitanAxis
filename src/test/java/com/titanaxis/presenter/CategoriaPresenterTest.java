package com.titanaxis.presenter;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.CategoriaService;
import com.titanaxis.view.interfaces.CategoriaView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoriaPresenterTest {

    @Mock
    private CategoriaView view;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CategoriaPresenter presenter;

    @BeforeEach
    void setUp() {
        Usuario admin = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        when(authService.getUsuarioLogado()).thenReturn(Optional.of(admin));
        when(categoriaService.listarTodasCategorias()).thenReturn(Collections.emptyList());
        presenter = new CategoriaPresenter(view, categoriaService, authService);
    }

    @Test
    void aoSalvar_deveCriarNovaCategoria_quandoValida() throws Exception {
        // Arrange
        when(view.getId()).thenReturn("");
        when(view.getNome()).thenReturn("Nova Categoria");

        // Act
        presenter.aoSalvar();

        // Assert
        verify(categoriaService).salvar(any(Categoria.class), any(Usuario.class));
        verify(view).mostrarMensagem(eq("Sucesso"), contains("adicionada"), eq(false));
        verify(view).setId("");
        verify(view).setNome("");
        verify(view).clearTableSelection();
    }

    @Test
    void aoSalvar_deveMostrarErro_quandoServicoLancaExcecao() throws Exception {
        // Arrange
        when(view.getId()).thenReturn("");
        when(view.getNome()).thenReturn("Categoria Existente");
        doThrow(new Exception("Já existe uma categoria com este nome."))
                .when(categoriaService).salvar(any(Categoria.class), any(Usuario.class));

        // Act
        presenter.aoSalvar();

        // Assert
        verify(view).mostrarMensagem("Erro", "Erro ao salvar categoria: Já existe uma categoria com este nome.", true);
    }

    @Test
    void aoApagar_deveChamarServicoDeletar_quandoConfirmado() {
        // Arrange
        when(view.getId()).thenReturn("5");
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(true);

        // Act
        presenter.aoApagar();

        // Assert
        // **AQUI ESTÁ A CORREÇÃO**
        // Usamos eq(5) para o valor específico e any() para o objeto do utilizador.
        verify(categoriaService).deletar(eq(5), any(Usuario.class));

        verify(view).mostrarMensagem(eq("Sucesso"), contains("eliminada"), eq(false));
    }
}