package com.titanaxis.presenter;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Permite stubs não utilizados no setup
class ProdutoPresenterTest {

    @Mock
    private ProdutoView view;
    @Mock
    private ProdutoService produtoService;
    @Mock
    private AuthService authService;

    @InjectMocks
    private ProdutoPresenter presenter;

    private Usuario admin;
    private Produto produtoAtivo;
    private Produto produtoInativo;

    @BeforeEach
    void setUp() {
        admin = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        produtoAtivo = new Produto();
        produtoAtivo.setId(1);
        produtoAtivo.setNome("Produto Ativo");
        produtoAtivo.setAtivo(true);
        produtoAtivo.setLotes(new ArrayList<>());

        produtoInativo = new Produto();
        produtoInativo.setId(2);
        produtoInativo.setNome("Produto Inativo");
        produtoInativo.setAtivo(false);
        produtoInativo.setLotes(new ArrayList<>());

        when(authService.getUsuarioLogado()).thenReturn(Optional.of(admin));
    }

    @Test
    void aoInicializar_deveCarregarProdutosAutomaticamente() {
        // CORREÇÃO: Este teste agora verifica o comportamento do construtor.
        // O presenter já foi inicializado no @BeforeEach, que chamou aoCarregarProdutos().
        // Precisamos apenas de verificar se essa chamada inicial aconteceu.
        verify(produtoService, times(1)).listarProdutos(anyBoolean());
        verify(view, times(1)).setProdutosNaTabela(anyList());
    }

    @Test
    void aoSelecionarProduto_deveBuscarProduto_ePreencherDetalhesNaView() {
        when(produtoService.buscarProdutoPorId(1)).thenReturn(Optional.of(produtoAtivo));
        presenter.aoSelecionarProduto(1);
        verify(produtoService).buscarProdutoPorId(1);
        verify(view).setLotesNaTabela(produtoAtivo.getLotes());
        verify(view).setBotoesDeAcaoEnabled(true);
        verify(view).setTextoBotaoStatus("Inativar Produto");
    }

    @Test
    void aoAlternarStatusDeProdutoAtivo_deveChamarServicoParaInativar_eLimparView() throws Exception {
        when(produtoService.buscarProdutoPorId(1)).thenReturn(Optional.of(produtoAtivo));
        presenter.aoSelecionarProduto(1);
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(true);

        presenter.aoAlternarStatusDoProduto();

        verify(produtoService).alterarStatusProduto(1, false, admin);
        verify(view).limparPainelDeDetalhes();
        verify(view).limparSelecaoDaTabelaDeProdutos();

        // CORREÇÃO: Verificamos a segunda chamada a `listarProdutos`. A primeira foi no construtor.
        verify(produtoService, times(2)).listarProdutos(anyBoolean());
    }

    @Test
    void aoAlternarStatusDeProdutoInativo_deveChamarServicoParaReativar() throws Exception {
        when(produtoService.buscarProdutoPorId(2)).thenReturn(Optional.of(produtoInativo));
        presenter.aoSelecionarProduto(2);
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(true);

        presenter.aoAlternarStatusDoProduto();

        verify(produtoService).alterarStatusProduto(2, true, admin);
    }

    @Test
    void aoClicarRemoverLote_deveChamarServico_quandoConfirmado() throws Exception {
        when(produtoService.buscarProdutoPorId(1)).thenReturn(Optional.of(produtoAtivo));
        presenter.aoSelecionarProduto(1);
        when(view.getSelectedLoteId()).thenReturn(10);
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(true);

        presenter.aoClicarRemoverLote();

        verify(produtoService).removerLote(10, admin);
        // CORREÇÃO: Verificamos a segunda chamada. A primeira foi no construtor.
        verify(produtoService, times(2)).listarProdutos(anyBoolean());
    }
}