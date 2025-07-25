package com.titanaxis.handler.actions;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;

import java.util.Map;

public class UpdateProductAction implements DashboardAction {
    private final AppContext appContext;
    private final DashboardFrame frame;

    public UpdateProductAction(AppContext appContext, DashboardFrame frame) {
        this.appContext = appContext;
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        try {
            ProdutoService produtoService = appContext.getProdutoService();
            Usuario ator = appContext.getAuthService().getUsuarioLogado().orElse(null);
            String productName = (String) params.get("productName");

            Produto produto = produtoService.buscarProdutoPorNome(productName)
                    .orElseThrow(() -> new IllegalArgumentException("Produto '" + productName + "' não encontrado."));

            if (params.containsKey("newPrice")) {
                produto.setPreco((Double) params.get("newPrice"));
                produtoService.salvarProduto(produto, ator);
                UIMessageUtil.showInfoMessage(frame, "Preço do produto '" + productName + "' atualizado com sucesso!", I18n.getString("dashboard.action.actionComplete"));
            }

            if (params.containsKey("active")) {
                produtoService.alterarStatusProduto(produto.getId(), (Boolean) params.get("active"), ator);
                UIMessageUtil.showInfoMessage(frame, "Status do produto '" + productName + "' atualizado com sucesso!", I18n.getString("dashboard.action.actionComplete"));
            }

            frame.refreshPanel("Produtos & Estoque");

        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(frame, I18n.getString("dashboard.action.errorExecuting", e.getMessage()), I18n.getString("dashboard.action.errorTitle"));
        }
    }
}