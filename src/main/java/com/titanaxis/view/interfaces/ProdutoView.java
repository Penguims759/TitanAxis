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

    // O Contrato: Nenhum destes métodos pode lançar exceções verificadas.
    interface ProdutoViewListener {
        void aoCarregarProdutos();
        void aoSelecionarProduto(int produtoId);
        void aoClicarNovoProduto();
        void aoClicarEditarProduto();
        void aoAlternarStatusDoProduto(); // Não tem 'throws'
        void aoClicarAdicionarLote();
        void aoClicarEditarLote();
        void aoClicarRemoverLote();      // Não tem 'throws'
    }

    void setListener(ProdutoViewListener listener);
}