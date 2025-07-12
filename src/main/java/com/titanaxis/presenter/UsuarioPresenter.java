// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/presenter/UsuarioPresenter.java
package com.titanaxis.presenter;

import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.PasswordUtil;
import com.titanaxis.view.interfaces.UsuarioView;

import java.util.List;

public class UsuarioPresenter implements UsuarioView.UsuarioViewListener {

    private final UsuarioView view;
    private final AuthService authService;

    public UsuarioPresenter(UsuarioView view, AuthService authService) {
        this.view = view;
        this.authService = authService;
        this.view.setListener(this);
        aoCarregarDadosIniciais();
    }

    @Override
    public void aoCarregarDadosIniciais() {
        try {
            view.setUsuariosNaTabela(authService.listarUsuarios());
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.user.error.load", e.getMessage()), true); // ALTERADO
        }
    }

    @Override
    public void aoSalvar() {
        String username = view.getUsername().trim();
        String password = view.getPassword();
        NivelAcesso nivel = view.getNivelAcesso();
        boolean isUpdate = !view.getId().isEmpty();

        if (username.isEmpty() || nivel == null || (!isUpdate && password.isEmpty())) {
            view.mostrarMensagem(I18n.getString("error.validation.title"), I18n.getString("presenter.user.error.requiredFields"), true); // ALTERADO
            return;
        }

        try {
            Usuario ator = authService.getUsuarioLogado().orElse(null);
            if (isUpdate) {
                int id = Integer.parseInt(view.getId());
                Usuario userOriginal = authService.listarUsuarios().stream().filter(u -> u.getId() == id).findFirst()
                        .orElseThrow(() -> new PersistenciaException(I18n.getString("presenter.user.error.userNotFound"), null)); // ALTERADO

                String senhaHash = password.isEmpty() ? userOriginal.getSenhaHash() : PasswordUtil.hashPassword(password);
                Usuario usuarioAtualizado = new Usuario(id, username, senhaHash, nivel);

                authService.atualizarUsuario(usuarioAtualizado, ator);
                view.mostrarMensagem(I18n.getString("success.title"), I18n.getString("presenter.user.success.update"), false); // ALTERADO
            } else {
                authService.cadastrarUsuario(username, password, nivel, ator);
                view.mostrarMensagem(I18n.getString("success.title"), I18n.getString("presenter.user.success.add"), false); // ALTERADO
            }
            aoLimpar();
            aoCarregarDadosIniciais();
        } catch (NomeDuplicadoException e) {
            view.mostrarMensagem(I18n.getString("error.duplication.title"), e.getMessage(), true); // ALTERADO
        } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.title"), I18n.getString("presenter.user.error.save", e.getMessage()), true); // ALTERADO
        }
    }

    @Override
    public void aoApagar() {
        if (view.getId().isEmpty()) {
            view.mostrarMensagem(I18n.getString("warning.title"), I18n.getString("presenter.user.error.selectToDelete"), true); // ALTERADO
            return;
        }
        int idToDelete = Integer.parseInt(view.getId());
        if (authService.getUsuarioLogadoId() == idToDelete) {
            view.mostrarMensagem(I18n.getString("presenter.user.error.deleteSelf.title"), I18n.getString("presenter.user.error.deleteSelf.message"), true); // ALTERADO
            return;
        }

        String nomeUtilizador = view.getUsername();
        String mensagem = I18n.getString("presenter.user.confirm.delete", nomeUtilizador); // ALTERADO

        if (!view.mostrarConfirmacao(I18n.getString("presenter.user.confirm.delete.title"), mensagem)) { // ALTERADO
            return;
        }

        try {
            Usuario ator = authService.getUsuarioLogado().orElse(null);
            authService.deletarUsuario(idToDelete, ator);
            view.mostrarMensagem(I18n.getString("success.title"), I18n.getString("presenter.user.success.delete"), false); // ALTERADO
            aoLimpar();
            aoCarregarDadosIniciais();
        } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.title"), I18n.getString("presenter.user.error.delete", e.getMessage()), true); // ALTERADO
        }
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
        try {
            String termo = view.getTermoBusca();
            if (termo != null && !termo.trim().isEmpty()) {
                view.setUsuariosNaTabela(authService.buscarUsuariosPorNomeContendo(termo));
            } else {
                aoCarregarDadosIniciais();
            }
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.user.error.search", e.getMessage()), true); // ALTERADO
        }
    }

    @Override
    public void aoLimparBusca() {
        view.setTermoBusca("");
        aoCarregarDadosIniciais();
    }

    @Override
    public void aoSelecionarUsuario(Usuario usuario) {
        if (usuario != null) {
            view.setId(String.valueOf(usuario.getId()));
            view.setUsername(usuario.getNomeUsuario());
            view.setNivelAcesso(usuario.getNivelAcesso());
            view.setPassword("");
        }
    }
}