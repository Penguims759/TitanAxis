// FICHEIRO: src/main/java/com/titanaxis/view/interfaces/ClienteView.java
package com.titanaxis.view.interfaces;

import com.titanaxis.model.Cliente;
import java.util.List;

public interface ClienteView {
    // Métodos para o Presenter controlar os campos de texto
    String getId();
    String getNome();
    String getContato();
    String getEndereco();
    void setId(String id);
    void setNome(String nome);
    void setContato(String contato);
    void setEndereco(String endereco);

    // Método para o Presenter preencher a tabela
    void setClientesNaTabela(List<Cliente> clientes);

    // Métodos para o Presenter mostrar mensagens
    void mostrarMensagem(String titulo, String mensagem, boolean isErro);
    boolean mostrarConfirmacao(String titulo, String mensagem);

    // Método para o Presenter obter o termo de busca
    String getTermoBusca();
    void setTermoBusca(String termo);

    // Método para o Presenter limpar a seleção da tabela
    void clearTableSelection();

    // Interface para que o Presenter possa ouvir os eventos da View
    interface ClienteViewListener {
        void aoSalvar();
        void aoApagar();
        void aoLimpar();
        void aoBuscar();
        void aoLimparBusca();
        void aoSelecionarCliente(Cliente cliente);
    }

    void setListener(ClienteViewListener listener);
}