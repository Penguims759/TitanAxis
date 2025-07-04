package com.titanaxis.view.interfaces;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import java.util.List;

public interface UsuarioView {
    String getId();
    String getUsername();
    String getPassword(); // Em texto simples, o Presenter irá fazer o hash
    NivelAcesso getNivelAcesso();
    void setId(String id);
    void setUsername(String username);
    void setPassword(String password);
    void setNivelAcesso(NivelAcesso nivel);
    void setUsuariosNaTabela(List<Usuario> usuarios);
    void mostrarMensagem(String titulo, String mensagem, boolean isErro);
    boolean mostrarConfirmacao(String titulo, String mensagem);
    String getTermoBusca();
    void setTermoBusca(String termo);
    void clearTableSelection();

    interface UsuarioViewListener {
        void aoSalvar();
        void aoApagar();
        void aoLimpar();
        void aoBuscar();
        void aoLimparBusca();
        void aoSelecionarUsuario(Usuario usuario);
        void aoCarregarDadosIniciais(); // NOVO: Método para o Presenter carregar os dados iniciais
    }

    void setListener(UsuarioViewListener listener);
}