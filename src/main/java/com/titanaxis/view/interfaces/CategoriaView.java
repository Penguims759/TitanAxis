package com.titanaxis.view.interfaces;

import com.titanaxis.model.Categoria;
import java.util.List;

public interface CategoriaView {
    // Métodos para o Presenter controlar os campos
    String getId();
    String getNome();
    void setId(String id);
    void setNome(String nome);

    // Método para preencher a tabela
    void setCategoriasNaTabela(List<Categoria> categorias);

    // Métodos para mostrar feedback ao utilizador
    void mostrarMensagem(String titulo, String mensagem, boolean isErro);
    boolean mostrarConfirmacao(String titulo, String mensagem);

    // Métodos para a funcionalidade de busca
    String getTermoBusca();
    void setTermoBusca(String termo);

    // Método para limpar a seleção da tabela
    void clearTableSelection();

    // Interface interna para que o Presenter ouça os eventos da View
    interface CategoriaViewListener {
        void aoSalvar();
        void aoApagar();
        void aoLimpar();
        void aoBuscar();
        void aoLimparBusca();
        void aoSelecionarCategoria(Categoria categoria);
        void aoCarregarDadosIniciais(); 
    }

    // Método para a View se registar no Presenter
    void setListener(CategoriaViewListener listener);
}