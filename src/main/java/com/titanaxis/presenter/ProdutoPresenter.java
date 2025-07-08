package com.titanaxis.presenter;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ProdutoService;
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
        String infoMessage = "Selecione um ficheiro CSV com as colunas na seguinte ordem:\n" +
                "nome,descricao,preco,nome_da_categoria\n\n" +
                "A primeira linha (cabeçalho) será ignorada. O separador deve ser ponto e vírgula (;).";
        view.mostrarMensagem("Formato do Ficheiro CSV", infoMessage, false);

        File ficheiro = view.mostrarSeletorDeFicheiroCsv();
        if (ficheiro == null) {
            return;
        }

        view.mostrarMensagem("Aguarde", "A processar o ficheiro CSV em segundo plano...", false);

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
                    view.mostrarMensagem("Importação Concluída", resultado, false);
                    aoCarregarProdutos();
                } catch (Exception e) {
                    view.mostrarMensagem("Erro na Importação", "Ocorreu um erro ao importar o ficheiro: " + e.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    // NOVO MÉTODO
    @Override
    public void aoClicarImportarPdf() {
        // Lógica para PDF (atualmente um placeholder)
        File ficheiro = view.mostrarSeletorDeFicheiroPdf();
        if (ficheiro != null) {
            view.mostrarMensagem("Funcionalidade Futura", "A importação de produtos a partir de PDF será implementada numa versão futura.", true);
        }
    }

    // NOVO MÉTODO
    @Override
    public void aoFiltrarTexto(String texto) {
        view.aplicarFiltroNaTabela(texto);
    }

    @Override
    public void aoCarregarProdutos() {
        try {
            view.setProdutosNaTabela(produtoService.listarProdutos(view.isMostrarInativos()));
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Falha ao carregar produtos: " + e.getMessage(), true);
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
                view.setTextoBotaoStatus(produtoSelecionado.isAtivo() ? "Inativar Produto" : "Reativar Produto");
            } else {
                this.produtoSelecionado = null;
                view.limparPainelDeDetalhes();
            }
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Falha ao buscar detalhes do produto: " + e.getMessage(), true);
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
        String acao = novoStatus ? "reativar" : "inativar";
        String mensagem = String.format("Tem certeza que deseja %s o produto '%s'?", acao, produtoSelecionado.getNome());
        if (view.mostrarConfirmacao("Confirmar Alteração", mensagem)) {
            try {
                produtoService.alterarStatusProduto(produtoSelecionado.getId(), novoStatus, ator);
                aoCarregarProdutos();
                view.limparPainelDeDetalhes();
                view.limparSelecaoDaTabelaDeProdutos();
            } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
                view.mostrarMensagem("Erro", "Erro ao alterar o estado do produto: " + e.getMessage(), true);
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
            view.mostrarMensagem("Aviso", "Selecione um lote para editar.", false);
            return;
        }
        try {
            produtoService.buscarLotePorId(loteId).ifPresent(lote -> {
                LoteDialog dialog = view.mostrarDialogoDeLote(produtoSelecionado, lote);
                dialog.setVisible(true);
                dialog.getLoteSalvo().ifPresent(this::processarLoteSalvo);
            });
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro", "Erro ao buscar o lote para edição: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoClicarRemoverLote() {
        if (produtoSelecionado == null) return;
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
            } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
                view.mostrarMensagem("Erro", "Erro ao remover o lote: " + e.getMessage(), true);
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