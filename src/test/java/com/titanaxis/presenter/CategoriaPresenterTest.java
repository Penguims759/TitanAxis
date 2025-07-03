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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings; // IMPORTAR ESTA LINHA
import org.mockito.quality.Strictness;             // IMPORTAR ESTA LINHA

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// CORREÇÃO: Adicionamos esta anotação para dizer ao Mockito para não se preocupar com preparações não utilizadas.
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CategoriaPresenterTest {

    @Mock
    private CategoriaView view;
    @Mock
    private CategoriaService categoriaService;
    @Mock
    private AuthService authService;

    @InjectMocks
    private CategoriaPresenter presenter;

    private Usuario admin;

    @BeforeEach
    void setUp() {
        admin = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        // Esta preparação agora é permitida mesmo que nem todos os testes a usem.
        when(authService.getUsuarioLogado()).thenReturn(Optional.of(admin));
    }

    @Test
    void aoSalvar_deveCriarNovaCategoria_quandoIdEstiverVazio() throws Exception {
        when(view.getId()).thenReturn("");
        when(view.getNome()).thenReturn("Nova Categoria");

        presenter.aoSalvar();

        ArgumentCaptor<Categoria> categoriaCaptor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaService).salvar(categoriaCaptor.capture(), eq(admin));

        assertEquals("Nova Categoria", categoriaCaptor.getValue().getNome());

        verify(view).mostrarMensagem(eq("Sucesso"), contains("adicionada"), eq(false));
        verify(view).setId(eq(""));
        verify(view).setNome(eq(""));
        verify(view).clearTableSelection();
    }

    @Test
    void aoSalvar_deveAtualizarCategoriaExistente_quandoIdEstiverPreenchido() throws Exception {
        when(view.getId()).thenReturn("5");
        when(view.getNome()).thenReturn("Nome Atualizado");

        presenter.aoSalvar();

        ArgumentCaptor<Categoria> categoriaCaptor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaService).salvar(categoriaCaptor.capture(), eq(admin));

        assertEquals(5, categoriaCaptor.getValue().getId());
        assertEquals("Nome Atualizado", categoriaCaptor.getValue().getNome());

        verify(view).mostrarMensagem(eq("Sucesso"), contains("atualizada"), eq(false));
    }

    @Test
    void aoSalvar_deveMostrarErro_quandoNomeEstiverVazio() throws Exception {
        when(view.getNome()).thenReturn("  ");

        presenter.aoSalvar();

        verify(categoriaService, never()).salvar(any(), any());
        verify(view).mostrarMensagem(eq("Erro de Validação"), anyString(), eq(true));
    }


    @Test
    void aoApagar_deveChamarServicoDeletar_quandoConfirmado() throws Exception {
        when(view.getId()).thenReturn("5");
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(true);

        presenter.aoApagar();

        verify(categoriaService).deletar(eq(5), eq(admin));
        verify(view).mostrarMensagem(eq("Sucesso"), contains("eliminada"), eq(false));

        verify(view).setId(eq(""));
        verify(view).setNome(eq(""));
        verify(view).clearTableSelection();
    }

    @Test
    void aoApagar_naoDeveFazerNada_quandoNaoConfirmado() throws Exception {
        when(view.getId()).thenReturn("5");
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(false);

        presenter.aoApagar();

        verify(categoriaService, never()).deletar(anyInt(), any());
    }
}