package com.titanaxis.presenter;

import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Fornecedor;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.FornecedorService;
import com.titanaxis.util.I18n;
import com.titanaxis.view.interfaces.FornecedorView;
import com.titanaxis.view.panels.FornecedorPanel;

public class FornecedorPresenter implements FornecedorView.FornecedorViewListener {

    private final FornecedorView view;
    private final FornecedorService fornecedorService;
    private final AuthService authService;

    public FornecedorPresenter(FornecedorView view, FornecedorService fornecedorService, AuthService authService) {
        this.view = view;
        this.fornecedorService = fornecedorService;
        this.authService = authService;
        this.view.setListener(this);
        aoCarregarDados();
    }

    @Override
    public void aoSalvar(Fornecedor fornecedor) {
        try {
            fornecedorService.salvar(fornecedor, authService.getUsuarioLogado().orElse(null));
            view.mostrarMensagem(I18n.getString("success.title"), I18n.getString("presenter.supplier.success.save"), false);
            aoCarregarDados();
            aoLimpar();
        } catch (UtilizadorNaoAutenticadoException | PersistenciaException | NomeDuplicadoException e) {
            view.mostrarMensagem(I18n.getString("error.title"), I18n.getString("presenter.supplier.error.save", e.getMessage()), true);
        }
    }

    @Override
    public void aoApagar(int id) {
        if (view.mostrarConfirmacao(I18n.getString("presenter.supplier.confirm.delete.title"), I18n.getString("presenter.supplier.confirm.delete.message"))) {
            try {
                fornecedorService.deletar(id, authService.getUsuarioLogado().orElse(null));
                view.mostrarMensagem(I18n.getString("success.title"), I18n.getString("presenter.supplier.success.delete"), false);
                aoCarregarDados();
                aoLimpar();
            } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
                view.mostrarMensagem(I18n.getString("error.title"), I18n.getString("presenter.supplier.error.delete", e.getMessage()), true);
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
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.supplier.error.load", e.getMessage()), true);
        }
    }

    @Override
    public void aoSelecionarFornecedor(int id) {
        try {
            fornecedorService.buscarPorId(id).ifPresent(fornecedor -> {
                if(view instanceof FornecedorPanel){
                    ((FornecedorPanel) view).preencherCamposPeloFornecedor(fornecedor);
                }
            });
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), "Erro ao buscar detalhes do fornecedor: " + e.getMessage(), true);
        }
    }
}