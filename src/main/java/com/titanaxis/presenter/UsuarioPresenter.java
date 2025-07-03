package com.titanaxis.presenter;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.PasswordUtil;
import com.titanaxis.view.interfaces.UsuarioView;
import java.util.List;
import java.util.Optional;

public class UsuarioPresenter implements UsuarioView.UsuarioViewListener {

    private final UsuarioView view;
    private final AuthService authService;

    public UsuarioPresenter(UsuarioView view, AuthService authService) {
        this.view = view;
        this.authService = authService;
        this.view.setListener(this);
        carregarDadosIniciais();
    }

    private void carregarDadosIniciais() {
        view.setUsuariosNaTabela(authService.listarUsuarios());
    }

    @Override
    public void aoSalvar() {
        String username = view.getUsername().trim();
        String password = view.getPassword();
        NivelAcesso nivel = view.getNivelAcesso();
        boolean isUpdate = !view.getId().isEmpty();

        if (username.isEmpty() || nivel == null || (!isUpdate && password.isEmpty())) {
            view.mostrarMensagem("Erro", "Nome, senha (para novos utilizadores) e nível de acesso são obrigatórios.", true);
            return;
        }

        int id = isUpdate ? Integer.parseInt(view.getId()) : 0;
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        if (isUpdate) {
            Optional<Usuario> userOpt = authService.listarUsuarios().stream().filter(u -> u.getId() == id).findFirst();
            if (userOpt.isEmpty()) {
                view.mostrarMensagem("Erro", "Utilizador não encontrado.", true);
                return;
            }
            String senhaHash = password.isEmpty() ? userOpt.get().getSenhaHash() : PasswordUtil.hashPassword(password);
            Usuario usuarioAtualizado = new Usuario(id, username, senhaHash, nivel);
            if (authService.atualizarUsuario(usuarioAtualizado, ator)) {
                view.mostrarMensagem("Sucesso", "Utilizador atualizado!", false);
            } else {
                view.mostrarMensagem("Erro", "Erro ao atualizar utilizador.", true);
            }
        } else {
            if (authService.cadastrarUsuario(username, password, nivel, ator)) {
                view.mostrarMensagem("Sucesso", "Utilizador adicionado!", false);
            } else {
                view.mostrarMensagem("Erro", "Erro ao adicionar. O nome de utilizador pode já existir.", true);
            }
        }
        aoLimpar();
        carregarDadosIniciais();
    }

    @Override
    public void aoApagar() {
        if (view.getId().isEmpty()) {
            view.mostrarMensagem("Aviso", "Selecione um utilizador para eliminar.", true);
            return;
        }
        int idToDelete = Integer.parseInt(view.getId());
        if (authService.getUsuarioLogadoId() == idToDelete) {
            view.mostrarMensagem("Ação Inválida", "Não pode eliminar o seu próprio utilizador.", true);
            return;
        }
        if (!view.mostrarConfirmacao("Confirmar", "Tem certeza?")) return;

        Usuario ator = authService.getUsuarioLogado().orElse(null);
        authService.deletarUsuario(idToDelete, ator);
        view.mostrarMensagem("Sucesso", "Utilizador eliminado!", false);
        aoLimpar();
        carregarDadosIniciais();
    }

    @Override
    public void aoLimpar() {
        view.setId("");
        view.setUsername("");
        view.setPassword("");
        view.setNivelAcesso(NivelAcesso.PADRAO);
        view.clearTableSelection();
    }

    @Override
    public void aoBuscar() {
        String termo = view.getTermoBusca();
        if (termo != null && !termo.trim().isEmpty()) {
            view.setUsuariosNaTabela(authService.buscarUsuariosPorNomeContendo(termo));
        } else {
            carregarDadosIniciais();
        }
    }

    @Override
    public void aoLimparBusca() {
        view.setTermoBusca("");
        carregarDadosIniciais();
    }

    @Override
    public void aoSelecionarUsuario(Usuario usuario) {
        if (usuario != null) {
            view.setId(String.valueOf(usuario.getId()));
            view.setUsername(usuario.getNomeUsuario());
            view.setNivelAcesso(usuario.getNivelAcesso());
            view.setPassword(""); // Limpa o campo de senha por segurança
        }
    }
}