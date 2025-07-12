// src/main/java/com/titanaxis/presenter/FornecedorPresenter.java
package com.titanaxis.presenter;

import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Fornecedor;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.FornecedorService;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.view.interfaces.FornecedorView;

public class FornecedorPresenter implements FornecedorView.FornecedorViewListener {

    private final FornecedorView view;
    private final FornecedorService fornecedorService;
    private final AuthService authService;

    public FornecedorPresenter(FornecedorView view, FornecedorService fornecedorService, AuthService authService) {
        this.view = view;
        this.fornecedorService = fornecedorService;
        this.authService = authService;
        this.view.setListener(this);
    }

    @Override
    public void aoSalvar(Fornecedor fornecedor) {
        try {
            fornecedorService.salvar(fornecedor, authService.getUsuarioLogado().orElse(null));
            view.mostrarMensagem(I18n.getString("success.title"), I18n.getString("presenter.supplier.success.save"), false); // ALTERADO
            aoCarregarDados();
            aoLimpar();
        } catch (UtilizadorNaoAutenticadoException | PersistenciaException | NomeDuplicadoException e) {
            view.mostrarMensagem(I18n.getString("error.title"), I18n.getString("presenter.supplier.error.save", e.getMessage()), true); // ALTERADO
        }
    }

    @Override
    public void aoApagar(int id) {
        if (view.mostrarConfirmacao(I18n.getString("presenter.supplier.confirm.delete.title"), I18n.getString("presenter.supplier.confirm.delete.message"))) { // ALTERADO
            try {
                fornecedorService.deletar(id, authService.getUsuarioLogado().orElse(null));
                view.mostrarMensagem(I18n.getString("success.title"), I18n.getString("presenter.supplier.success.delete"), false); // ALTERADO
                aoCarregarDados();
                aoLimpar();
            } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
                view.mostrarMensagem(I18n.getString("error.title"), I18n.getString("presenter.supplier.error.delete", e.getMessage()), true); // ALTERADO
            }
        }
    }

    @Override
    public void aoLimpar() {
        view.limparCampos();
    }

    @Override
    public void aoCarregarDados() {
        try {
            view.setFornecedoresNaTabela(fornecedorService.listarTodos());
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.supplier.error.load", e.getMessage()), true); // ALTERADO
        }
    }

    @Override
    public void aoSelecionarFornecedor(int id) {
        // A lógica de preencher os campos já está no FornecedorPanel,
        // mas este método poderia ser usado para carregar dados adicionais se necessário.
    }
}