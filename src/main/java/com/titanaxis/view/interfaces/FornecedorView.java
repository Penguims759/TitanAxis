package com.titanaxis.view.interfaces;

import com.titanaxis.model.Fornecedor;
import java.util.List;

public interface FornecedorView {
    void setFornecedoresNaTabela(List<Fornecedor> fornecedores);
    void mostrarMensagem(String titulo, String mensagem, boolean isErro);
    boolean mostrarConfirmacao(String titulo, String mensagem);
    void limparCampos();

    interface FornecedorViewListener {
        void aoSalvar(Fornecedor fornecedor);
        void aoApagar(int id);
        void aoLimpar();
        void aoCarregarDados();
        void aoSelecionarFornecedor(int id);
    }

    void setListener(FornecedorViewListener listener);
}