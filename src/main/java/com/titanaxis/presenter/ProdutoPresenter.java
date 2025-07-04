package com.titanaxis.presenter;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.view.dialogs.LoteDialog;
import com.titanaxis.view.dialogs.ProdutoDialog;
import com.titanaxis.view.interfaces.ProdutoView;

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
    public void aoCarregarProdutos() {
        view.setProdutosNaTabela(produtoService.listarProdutos(view.isMostrarInativos()));
    }

    @Override
    public void aoSelecionarProduto(int produtoId) {
        Optional<Produto> produtoOpt = produtoService.buscarProdutoPorId(produtoId);
        if (produtoOpt.isPresent()) {
            this.produtoSelecionado = produtoOpt.get();
            // Ordena os lotes antes de exibi-los
            this.produtoSelecionado.getLotes().sort(Comparator.comparing(Lote::getDataValidade, Comparator.nullsLast(Comparator.naturalOrder())));
            view.setLotesNaTabela(this.produtoSelecionado.getLotes());
            view.setBotoesDeAcaoEnabled(true);
            view.setTextoBotaoStatus(produtoSelecionado.isAtivo() ? "Inativar Produto" : "Reativar Produto");
        } else {
            this.produtoSelecionado = null;
            view.limparPainelDeDetalhes();
        }
    }

    @Override
    public void aoClicarNovoProduto() {
        // O presenter pede o diálogo à view e o exibe
        ProdutoDialog dialog = view.mostrarDialogoDeProduto(null);
        dialog.setVisible(true); // Bloqueia a execução até o diálogo ser fechado

        // Após o diálogo fechar, o presenter verifica o resultado
        if (dialog.isSaved()) {
            aoCarregarProdutos(); // Se foi salvo, atualiza a lista de produtos
        }
    }

    @Override
    public void aoClicarEditarProduto() {
        if (produtoSelecionado != null) {
            ProdutoDialog dialog = view.mostrarDialogoDeProduto(produtoSelecionado);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                aoCarregarProdutos();
                aoSelecionarProduto(produtoSelecionado.getId()); // Recarrega os detalhes
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
            } catch (Exception e) {
                view.mostrarMensagem("Erro", "Erro ao alterar o estado do produto: " + e.getMessage(), true);
            }
        }
    }

    @Override
    public void aoClicarAdicionarLote() {
        if (produtoSelecionado != null) {
            LoteDialog dialog = view.mostrarDialogoDeLote(produtoSelecionado, null);
            dialog.setVisible(true);

            // Processa o resultado retornado pelo diálogo
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

        produtoService.buscarLotePorId(loteId).ifPresent(lote -> {
            LoteDialog dialog = view.mostrarDialogoDeLote(produtoSelecionado, lote);
            dialog.setVisible(true);
            dialog.getLoteSalvo().ifPresent(this::processarLoteSalvo);
        });
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
                // Força o recarregamento do produto para atualizar a lista de lotes e contagens
                aoSelecionarProduto(produtoSelecionado.getId());
                aoCarregarProdutos();
            } catch (Exception e) {
                view.mostrarMensagem("Erro", "Erro ao remover o lote: " + e.getMessage(), true);
            }
        }
    }

    /**
     * Centraliza a lógica de atualização após um lote ser salvo.
     * @param loteSalvo O lote que foi persistido.
     */
    private void processarLoteSalvo(Lote loteSalvo) {
        if (produtoSelecionado == null) return;

        // Atualiza a lista de lotes no objeto Produto em memória
        produtoSelecionado.getLotes().removeIf(l -> l.getId() == loteSalvo.getId());
        produtoSelecionado.getLotes().add(loteSalvo);

        // Reordena e atualiza a view
        aoSelecionarProduto(produtoSelecionado.getId());
        aoCarregarProdutos();
    }
}