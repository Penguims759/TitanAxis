package com.titanaxis.presenter;

import com.titanaxis.model.Lote;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.view.interfaces.ProdutoView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProdutoPresenterTest {

    @Mock
    private ProdutoView view;
    @Mock
    private ProdutoService produtoService;
    @Mock
    private AuthService authService;
    @InjectMocks
    private ProdutoPresenter presenter;

    private Produto produtoAtivo;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        admin = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        produtoAtivo = new Produto(1, "Produto Ativo", "Desc", 10.0, 1, "Geral", true);
        when(authService.getUsuarioLogado()).thenReturn(Optional.of(admin));
    }

    @Test
    void aoAlternarStatusDoProduto_deveChamarServico_eLimparView() {
        // Arrange
        when(produtoService.buscarProdutoPorId(1)).thenReturn(Optional.of(produtoAtivo));
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(true);
        presenter.aoSelecionarProduto(1);
        Mockito.clearInvocations(view, produtoService);

        // Act
        presenter.aoAlternarStatusDoProduto();

        // Assert
        try {
            verify(produtoService).alterarStatusProduto(1, false, admin);
        } catch (Exception e) {
            // Teste ignora a exceção porque o presenter deve tratá-la
        }
        verify(view, times(1)).limparPainelDeDetalhes();
        verify(view, times(1)).limparSelecaoDaTabelaDeProdutos();
    }
}