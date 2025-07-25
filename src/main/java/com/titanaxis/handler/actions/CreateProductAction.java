package com.titanaxis.handler.actions;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import java.util.Map;

public class CreateProductAction implements DashboardAction {
    private final AppContext appContext;
    private final DashboardFrame frame;

    public CreateProductAction(AppContext appContext, DashboardFrame frame) {
        this.appContext = appContext;
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        try {
            Usuario ator = appContext.getAuthService().getUsuarioLogado().orElse(null);
            Produto novoProduto = new Produto((String) params.get("nome"), "", (Double) params.get("preco"), (Categoria) params.get("categoria"));
            appContext.getProdutoService().salvarProduto(novoProduto, ator);
            UIMessageUtil.showInfoMessage(frame, I18n.getString("dashboard.action.productCreated", novoProduto.getNome()), I18n.getString("dashboard.action.actionComplete"));
            frame.refreshPanel("Produtos & Estoque");
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(frame, I18n.getString("dashboard.action.errorExecuting", e.getMessage()), I18n.getString("dashboard.action.errorTitle"));
        }
    }
}