package com.titanaxis.presenter;

import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
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
            view.mostrarMensagem("Erro de Base de Dados", "Falha ao carregar utilizadores: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoSalvar() {
        String username = view.getUsername().trim();
        String password = view.getPassword();
        NivelAcesso nivel = view.getNivelAcesso();
        boolean isUpdate = !view.getId().isEmpty();

        if (username.isEmpty() || nivel == null || (!isUpdate && password.isEmpty())) {
            view.mostrarMensagem("Erro de Validação", "Nome, senha (para novos) e nível de acesso são obrigatórios.", true);
            return;
        }

        try {
            Usuario ator = authService.getUsuarioLogado().orElse(null);
            if (isUpdate) {
                int id = Integer.parseInt(view.getId());
                Usuario userOriginal = authService.listarUsuarios().stream().filter(u -> u.getId() == id).findFirst()
                        .orElseThrow(() -> new PersistenciaException("Utilizador a ser atualizado não encontrado.", null));

                String senhaHash = password.isEmpty() ? userOriginal.getSenhaHash() : PasswordUtil.hashPassword(password);
                Usuario usuarioAtualizado = new Usuario(id, username, senhaHash, nivel);

                authService.atualizarUsuario(usuarioAtualizado, ator);
                view.mostrarMensagem("Sucesso", "Utilizador atualizado!", false);
            } else {
                authService.cadastrarUsuario(username, password, nivel, ator);
                view.mostrarMensagem("Sucesso", "Utilizador adicionado!", false);
            }
            aoLimpar();
            aoCarregarDadosIniciais();
        } catch (NomeDuplicadoException e) {
            view.mostrarMensagem("Erro de Duplicação", e.getMessage(), true);
        } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
            view.mostrarMensagem("Erro", "Erro ao salvar utilizador: " + e.getMessage(), true);
        }
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

        String nomeUtilizador = view.getUsername();
        String mensagem = String.format("Tem a certeza que deseja eliminar o utilizador '%s'?", nomeUtilizador);

        if (!view.mostrarConfirmacao("Confirmar", mensagem)) return;

        try {
            Usuario ator = authService.getUsuarioLogado().orElse(null);
            authService.deletarUsuario(idToDelete, ator);
            view.mostrarMensagem("Sucesso", "Utilizador eliminado!", false);
            aoLimpar();
            aoCarregarDadosIniciais();
        } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
            view.mostrarMensagem("Erro", "Erro ao eliminar utilizador: " + e.getMessage(), true);
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
            view.mostrarMensagem("Erro de Base de Dados", "Falha ao buscar utilizadores: " + e.getMessage(), true);
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