package com.titanaxis.presenter;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.util.I18n;
import com.titanaxis.view.dialogs.LoteDialog;
import com.titanaxis.view.dialogs.ProdutoDialog;
import com.titanaxis.view.interfaces.ProdutoView;

import javax.swing.*;
import java.io.File;
import java.util.Comparator;
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
    public void aoClicarImportarCsv() {
        String infoMessage = I18n.getString("presenter.product.import.csvInfo");
        view.mostrarMensagem(I18n.getString("presenter.product.import.csvInfo.title"), infoMessage, false);

        File ficheiro = view.mostrarSeletorDeFicheiroCsv();
        if (ficheiro == null) {
            return;
        }

        view.mostrarMensagem(I18n.getString("presenter.product.import.wait.title"), I18n.getString("presenter.product.import.wait.message"), false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                Usuario ator = authService.getUsuarioLogado().orElse(null);
                return produtoService.importarProdutosDeCsv(ficheiro, ator);
            }

            @Override
            protected void done() {
                try {
                    String resultado = get();
                    view.mostrarMensagem(I18n.getString("presenter.product.import.complete.title"), resultado, false);
                    aoCarregarProdutos();
                } catch (Exception e) {
                    view.mostrarMensagem(I18n.getString("presenter.product.import.error.title"), I18n.getString("presenter.product.import.error.message", e.getMessage()), true);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void aoClicarImportarPdf() {
        File ficheiro = view.mostrarSeletorDeFicheiroPdf();
        if (ficheiro != null) {
            view.mostrarMensagem(I18n.getString("presenter.product.import.pdf.title"), I18n.getString("presenter.product.import.pdf.message"), true);
        }
    }

    @Override
    public void aoFiltrarTexto(String texto) {
        view.aplicarFiltroNaTabela(texto);
    }

    @Override
    public void aoCarregarProdutos() {
        try {
            view.setProdutosNaTabela(produtoService.listarProdutos(view.isMostrarInativos()));
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.product.error.load", e.getMessage()), true);
        }
    }

    @Override
    public void aoSelecionarProduto(int produtoId) {
        try {
            Optional<Produto> produtoOpt = produtoService.buscarProdutoPorId(produtoId);
            if (produtoOpt.isPresent()) {
                this.produtoSelecionado = produtoOpt.get();
                this.produtoSelecionado.getLotes().sort(Comparator.comparing(Lote::getDataValidade, Comparator.nullsLast(Comparator.naturalOrder())));
                view.setLotesNaTabela(this.produtoSelecionado.getLotes());
                view.setBotoesDeAcaoEnabled(true);
                view.setTextoBotaoStatus(produtoSelecionado.isAtivo() ? I18n.getString("presenter.product.button.deactivate") : I18n.getString("presenter.product.button.reactivate"));
            } else {
                this.produtoSelecionado = null;
                view.limparPainelDeDetalhes();
            }
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.product.error.fetchDetails", e.getMessage()), true);
        }
    }

    @Override
    public void aoClicarNovoProduto() {
        ProdutoDialog dialog = view.mostrarDialogoDeProduto(null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            aoCarregarProdutos();
        }
    }

    @Override
    public void aoClicarEditarProduto() {
        if (produtoSelecionado != null) {
            ProdutoDialog dialog = view.mostrarDialogoDeProduto(produtoSelecionado);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                aoCarregarProdutos();
                aoSelecionarProduto(produtoSelecionado.getId());
            }
        }
    }

    @Override
    public void aoAlternarStatusDoProduto() {
        if (produtoSelecionado == null) return;
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        boolean novoStatus = !produtoSelecionado.isAtivo();
        String acao = novoStatus ? I18n.getString("action.reactivate") : I18n.getString("action.deactivate");
        String mensagem = I18n.getString("presenter.product.confirm.toggleStatus", acao, produtoSelecionado.getNome());
        if (view.mostrarConfirmacao(I18n.getString("presenter.product.confirm.toggleStatus.title"), mensagem)) {
            try {
                produtoService.alterarStatusProduto(produtoSelecionado.getId(), novoStatus, ator);
                aoCarregarProdutos();
                view.limparPainelDeDetalhes();
                view.limparSelecaoDaTabelaDeProdutos();
            } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
                view.mostrarMensagem(I18n.getString("error.title"), I18n.getString("presenter.product.error.toggleStatus", e.getMessage()), true);
            }
        }
    }

    @Override
    public void aoClicarAdicionarLote() {
        if (produtoSelecionado != null) {
            LoteDialog dialog = view.mostrarDialogoDeLote(produtoSelecionado, null);
            dialog.setVisible(true);
            dialog.getLoteSalvo().ifPresent(this::processarLoteSalvo);
        }
    }

    @Override
    public void aoClicarEditarLote() {
        if (produtoSelecionado == null) return;
        int loteId = view.getSelectedLoteId();
        if (loteId == -1) {
            view.mostrarMensagem(I18n.getString("warning.title"), I18n.getString("presenter.product.error.selectBatchToEdit"), false);
            return;
        }
        try {
            produtoService.buscarLotePorId(loteId).ifPresent(lote -> {
                LoteDialog dialog = view.mostrarDialogoDeLote(produtoSelecionado, lote);
                dialog.setVisible(true);
                dialog.getLoteSalvo().ifPresent(this::processarLoteSalvo);
            });
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.title"), I18n.getString("presenter.product.error.fetchBatch", e.getMessage()), true);
        }
    }

    @Override
    public void aoClicarRemoverLote() {
        if (produtoSelecionado == null) return;
        int loteId = view.getSelectedLoteId();
        if (loteId == -1) {
            view.mostrarMensagem(I18n.getString("warning.title"), I18n.getString("presenter.product.error.selectBatchToRemove"), false);
            return;
        }
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        if (view.mostrarConfirmacao(I18n.getString("presenter.product.confirm.removeBatch.title"), I18n.getString("presenter.product.confirm.removeBatch.message"))) {
            try {
                produtoService.removerLote(loteId, ator);
                aoSelecionarProduto(produtoSelecionado.getId());
                aoCarregarProdutos();
            } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
                view.mostrarMensagem(I18n.getString("error.title"), I18n.getString("presenter.product.error.removeBatch", e.getMessage()), true);
            }
        }
    }

    private void processarLoteSalvo(Lote loteSalvo) {
        if (produtoSelecionado == null) return;
        produtoSelecionado.getLotes().removeIf(l -> l.getId() == loteSalvo.getId());
        produtoSelecionado.getLotes().add(loteSalvo);
        aoSelecionarProduto(produtoSelecionado.getId());
        aoCarregarProdutos();
    }
}