package com.titanaxis.view.interfaces;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.view.dialogs.LoteDialog;
import com.titanaxis.view.dialogs.ProdutoDialog;

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

    // ALTERADO: Agora retorna a instância do diálogo para o presenter.
    ProdutoDialog mostrarDialogoDeProduto(Produto produto);
    LoteDialog mostrarDialogoDeLote(Produto produtoPai, Lote lote);

    // O listener agora não precisa mais do método aoLoteSalvo.
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