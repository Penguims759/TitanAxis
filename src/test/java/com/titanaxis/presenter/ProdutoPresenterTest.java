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

import java.util.Collections;
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

    // @InjectMocks já cria a instância do presenter e injeta os mocks
    @InjectMocks
    private ProdutoPresenter presenter;

    private Produto produtoAtivo;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        // Apenas configuramos os mocks. Não recriamos o presenter aqui.
        admin = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        produtoAtivo = new Produto(1, "Produto Ativo", "Desc", 10.0, 1, "Geral", true);
        when(authService.getUsuarioLogado()).thenReturn(Optional.of(admin));
    }

    @Test
    void aoSelecionarProduto_deveCarregarSeusLotes_eHabilitarBotoes() {
        // Arrange
        List<Lote> lotes = List.of(new Lote(1, 1, "LOTE01", 10, null));
        when(produtoService.buscarProdutoPorId(1)).thenReturn(Optional.of(produtoAtivo));
        when(produtoService.buscarLotesPorProdutoId(1)).thenReturn(lotes);

        // Act
        presenter.aoSelecionarProduto(1);

        // Assert
        verify(view).setLotesNaTabela(lotes);
        verify(view).setBotoesDeAcaoEnabled(true);
        verify(view).setTextoBotaoStatus("Inativar Produto");
    }

    @Test
    void aoAlternarStatusDoProduto_deveInativarProdutoAtivo_eLimparView() throws Exception {
        // Arrange
        when(produtoService.buscarProdutoPorId(1)).thenReturn(Optional.of(produtoAtivo));
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(true);
        presenter.aoSelecionarProduto(1);

        // Limpamos o histórico de chamadas aos mocks para focar no que acontece a seguir.
        Mockito.clearInvocations(view, produtoService);

        // Act
        presenter.aoAlternarStatusDoProduto();

        // Assert
        verify(produtoService).alterarStatusProduto(1, false, admin);
        verify(view, times(1)).limparPainelDeDetalhes();
        verify(view, times(1)).limparSelecaoDaTabelaDeProdutos();
        // Verifica se os produtos são recarregados após a alteração
        verify(produtoService, times(1)).listarProdutos(anyBoolean());
    }

    @Test
    void aoClicarAdicionarLote_deveAbrirDialogo() {
        // Arrange
        when(produtoService.buscarProdutoPorId(1)).thenReturn(Optional.of(produtoAtivo));
        presenter.aoSelecionarProduto(1);

        // Act
        presenter.aoClicarAdicionarLote();

        // Assert
        verify(view).mostrarDialogoDeLote(eq(produtoAtivo), isNull());
    }

    @Test
    void aoClicarRemoverLote_naoDeveFazerNada_seNenhumLoteSelecionado() throws Exception {
        // Arrange
        when(view.getSelectedLoteId()).thenReturn(-1);
        when(produtoService.buscarProdutoPorId(1)).thenReturn(Optional.of(produtoAtivo));

        // Act
        presenter.aoSelecionarProduto(1);
        presenter.aoClicarRemoverLote();

        // Assert
        verify(produtoService, never()).removerLote(anyInt(), any(Usuario.class));
    }
}