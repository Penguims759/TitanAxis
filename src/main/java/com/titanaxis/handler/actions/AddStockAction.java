package com.titanaxis.handler.actions;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Usuario;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;

import java.util.Map;

public class AddStockAction implements DashboardAction {
    private final AppContext appContext;
    private final DashboardFrame frame;

    public AddStockAction(AppContext appContext, DashboardFrame frame) {
        this.appContext = appContext;
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        try {
            String prodName = (String) params.get("productName");
            String lotNumber = (String) params.get("lotNumber");
            int quantityToAdd = Integer.parseInt((String) params.get("quantity"));
            Usuario ator = appContext.getAuthService().getUsuarioLogado().orElse(null);

            appContext.getProdutoService().adicionarEstoqueLote(prodName, lotNumber, quantityToAdd, ator);
            UIMessageUtil.showInfoMessage(frame, quantityToAdd + " unidades adicionadas ao lote " + lotNumber + ".", I18n.getString("dashboard.action.actionComplete"));

            frame.refreshPanel("Produtos & Estoque");
            frame.refreshPanel("Histórico de Movimentos");
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(frame, I18n.getString("dashboard.action.errorExecuting", e.getMessage()), I18n.getString("dashboard.action.errorTitle"));
        }
    }
}