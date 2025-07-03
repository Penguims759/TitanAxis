package com.titanaxis.view.interfaces;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import java.util.List;

public interface ProdutoView {
    // Métodos para o Presenter atualizar a UI
    void setProdutosNaTabela(List<Produto> produtos);
    void setLotesNaTabela(List<Lote> lotes);
    void setBotoesDeAcaoEnabled(boolean enabled);
    void setTextoBotaoStatus(String texto);
    void limparPainelDeDetalhes();
    void limparSelecaoDaTabelaDeProdutos();

    // Métodos para o Presenter obter estado da UI
    boolean isMostrarInativos();
    int getSelectedLoteId();

    // Métodos de feedback e interação
    void mostrarMensagem(String titulo, String mensagem, boolean isErro);
    boolean mostrarConfirmacao(String titulo, String mensagem);
    void mostrarDialogoDeProduto(Produto produto);
    void mostrarDialogoDeLote(Produto produtoPai, Lote lote);

    // Interface para o Presenter ouvir os eventos da View
    interface ProdutoViewListener {
        void aoCarregarProdutos();
        void aoSelecionarProduto(int produtoId);
        void aoClicarNovoProduto();
        void aoClicarEditarProduto();
        void aoAlternarStatusDoProduto();
        void aoClicarAdicionarLote();
        void aoClicarEditarLote();
        void aoClicarRemoverLote();
    }

    void setListener(ProdutoViewListener listener);
}