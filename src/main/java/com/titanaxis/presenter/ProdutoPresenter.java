package com.titanaxis.presenter;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.view.interfaces.ProdutoView;

import java.util.Optional;

public class ProdutoPresenter implements ProdutoView.ProdutoViewListener {
    private final ProdutoView view;
    private final ProdutoService produtoService;
    private final AuthService authService;
    private Produto produtoSelecionado;

    public ProdutoPresenter(ProdutoView view, ProdutoService produtoService, AuthService authService) {
        this.view = view;
        this.produtoService = produtoService;
        this.authService = authService;
        this.view.setListener(this);
        aoCarregarProdutos();
    }

    @Override
    public void aoCarregarProdutos() {
        view.setProdutosNaTabela(produtoService.listarProdutos(view.isMostrarInativos()));
    }

    @Override
    public void aoSelecionarProduto(int produtoId) {
        Optional<Produto> produtoOpt = produtoService.buscarProdutoPorId(produtoId);
        if (produtoOpt.isPresent()) {
            this.produtoSelecionado = produtoOpt.get();
            view.setLotesNaTabela(produtoService.buscarLotesPorProdutoId(produtoId));
            view.setBotoesDeAcaoEnabled(true);
            view.setTextoBotaoStatus(produtoSelecionado.isAtivo() ? "Inativar Produto" : "Reativar Produto");
        } else {
            this.produtoSelecionado = null;
            view.limparPainelDeDetalhes();
        }
    }

    @Override
    public void aoClicarNovoProduto() { view.mostrarDialogoDeProduto(null); }

    @Override
    public void aoClicarEditarProduto() {
        if (produtoSelecionado != null) { view.mostrarDialogoDeProduto(produtoSelecionado); }
    }

    @Override
    public void aoAlternarStatusDoProduto() {
        if (produtoSelecionado == null) return;
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        boolean novoStatus = !produtoSelecionado.isAtivo();
        String acao = novoStatus ? "reativar" : "inativar";
        if (view.mostrarConfirmacao("Confirmar Alteração", "Tem certeza que deseja " + acao + " o produto '" + produtoSelecionado.getNome() + "'?")) {
            try {
                produtoService.alterarStatusProduto(produtoSelecionado.getId(), novoStatus, ator);
                aoCarregarProdutos();
                view.limparPainelDeDetalhes();
                view.limparSelecaoDaTabelaDeProdutos();
            } catch (Exception e) {
                view.mostrarMensagem("Erro", "Erro ao alterar o estado do produto: " + e.getMessage(), true);
            }
        }
    }

    @Override
    public void aoClicarAdicionarLote() {
        if (produtoSelecionado != null) { view.mostrarDialogoDeLote(produtoSelecionado, null); }
    }

    @Override
    public void aoClicarEditarLote() {
        int loteId = view.getSelectedLoteId();
        if (loteId == -1) {
            view.mostrarMensagem("Aviso", "Selecione um lote para editar.", false);
            return;
        }
        produtoService.buscarLotePorId(loteId).ifPresent(lote -> view.mostrarDialogoDeLote(produtoSelecionado, lote));
    }

    @Override
    public void aoClicarRemoverLote() {
        int loteId = view.getSelectedLoteId();
        if (loteId == -1) {
            view.mostrarMensagem("Aviso", "Selecione um lote para remover.", false);
            return;
        }
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        if (view.mostrarConfirmacao("Confirmar Remoção", "Tem certeza que deseja remover este lote?")) {
            try {
                produtoService.removerLote(loteId, ator);
                aoSelecionarProduto(produtoSelecionado.getId());
                aoCarregarProdutos();
            } catch (Exception e) {
                view.mostrarMensagem("Erro", "Erro ao remover o lote: " + e.getMessage(), true);
            }
        }
    }
}