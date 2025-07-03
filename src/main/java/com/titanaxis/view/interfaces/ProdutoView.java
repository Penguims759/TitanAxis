package com.titanaxis.view.interfaces;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import java.util.List;

public interface ProdutoView {
    void setProdutosNaTabela(List<Produto> produtos);
    void setLotesNaTabela(List<Lote> lotes);
    void setBotoesDeAcaoEnabled(boolean enabled);
    void setTextoBotaoStatus(String texto);
    void limparPainelDeDetalhes();
    void limparSelecaoDaTabelaDeProdutos();
    boolean isMostrarInativos();
    int getSelectedLoteId();
    void mostrarMensagem(String titulo, String mensagem, boolean isErro);
    boolean mostrarConfirmacao(String titulo, String mensagem);
    void mostrarDialogoDeProduto(Produto produto);
    void mostrarDialogoDeLote(Produto produtoPai, Lote lote);

    interface ProdutoViewListener {
        void aoCarregarProdutos();
        void aoSelecionarProduto(int produtoId);
        void aoClicarNovoProduto();
        void aoClicarEditarProduto();
        void aoAlternarStatusDoProduto();
        void aoClicarAdicionarLote();
        void aoClicarEditarLote();
        void aoClicarRemoverLote();

        /**
         * Evento acionado após um lote ser salvo com sucesso,
         * passando o lote persistido para o presenter.
         * @param lote O lote que foi salvo ou atualizado.
         */
        void aoLoteSalvo(Lote lote);
    }

    void setListener(ProdutoViewListener listener);
}