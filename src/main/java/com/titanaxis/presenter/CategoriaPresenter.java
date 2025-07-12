// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/presenter/CategoriaPresenter.java
package com.titanaxis.presenter;

import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.CategoriaService;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.view.interfaces.CategoriaView;

import java.util.List;

public class CategoriaPresenter implements CategoriaView.CategoriaViewListener {

    private final CategoriaView view;
    private final CategoriaService categoriaService;
    private final AuthService authService;

    public CategoriaPresenter(CategoriaView view, CategoriaService categoriaService, AuthService authService) {
        this.view = view;
        this.categoriaService = categoriaService;
        this.authService = authService;
        this.view.setListener(this);
        aoCarregarDadosIniciais();
    }

    @Override
    public void aoCarregarDadosIniciais() {
        try {
            List<Categoria> categorias = categoriaService.listarTodasCategorias();
            view.setCategoriasNaTabela(categorias);
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.category.error.load", e.getMessage()), true); // ALTERADO
        }
    }

    @Override
    public void aoSalvar() {
        String nome = view.getNome().trim();
        if (nome.isEmpty()) {
            view.mostrarMensagem(I18n.getString("error.validation.title"), I18n.getString("presenter.category.error.nameRequired"), true); // ALTERADO
            return;
        }

        boolean isUpdate = !view.getId().isEmpty();
        int id = isUpdate ? Integer.parseInt(view.getId()) : 0;
        Categoria categoria = new Categoria(id, nome);
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        try {
            categoriaService.salvar(categoria, ator);
            String successMessage = isUpdate ? I18n.getString("presenter.category.success.update") : I18n.getString("presenter.category.success.add"); // ALTERADO
            view.mostrarMensagem(I18n.getString("success.title"), successMessage, false); // ALTERADO
            aoLimpar();
            aoCarregarDadosIniciais();
        } catch (NomeDuplicadoException e) {
            view.mostrarMensagem(I18n.getString("error.duplication.title"), e.getMessage(), true); // ALTERADO
        } catch (UtilizadorNaoAutenticadoException e) {
            view.mostrarMensagem(I18n.getString("error.auth.title"), e.getMessage(), true); // ALTERADO
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.category.error.save", e.getMessage()), true); // ALTERADO
        }
    }

    @Override
    public void aoApagar() {
        if (view.getId().isEmpty()) {
            view.mostrarMensagem(I18n.getString("warning.title"), I18n.getString("presenter.category.error.selectToDelete"), true); // ALTERADO
            return;
        }

        String nomeCategoria = view.getNome();
        String mensagem = I18n.getString("presenter.category.confirm.delete", nomeCategoria); // ALTERADO

        if (!view.mostrarConfirmacao(I18n.getString("presenter.category.confirm.delete.title"), mensagem)) { // ALTERADO
            return;
        }

        int id = Integer.parseInt(view.getId());
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        try {
            categoriaService.deletar(id, ator);
            view.mostrarMensagem(I18n.getString("success.title"), I18n.getString("presenter.category.success.delete"), false); // ALTERADO
            aoLimpar();
            aoCarregarDadosIniciais();
        } catch (UtilizadorNaoAutenticadoException e) {
            view.mostrarMensagem(I18n.getString("error.auth.title"), e.getMessage(), true); // ALTERADO
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.category.error.delete", e.getMessage()), true); // ALTERADO
        }
    }

    @Override
    public void aoLimpar() {
        view.setId("");
        view.setNome("");
        view.clearTableSelection();
    }

    @Override
    public void aoBuscar() {
        try {
            String termo = view.getTermoBusca();
            if (termo != null && !termo.trim().isEmpty()) {
                view.setCategoriasNaTabela(categoriaService.buscarCategoriasPorNome(termo));
            } else {
                aoCarregarDadosIniciais();
            }
        }
        catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.category.error.search", e.getMessage()), true); // ALTERADO
        }
    }

    @Override
    public void aoLimparBusca() {
        view.setTermoBusca("");
        aoCarregarDadosIniciais();
    }

    @Override
    public void aoSelecionarCategoria(Categoria categoria) {
        if (categoria != null) {
            view.setId(String.valueOf(categoria.getId()));
            view.setNome(categoria.getNome());
        }
    }
}