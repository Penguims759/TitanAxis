package com.titanaxis.handler.actions;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Usuario;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;

import java.util.Map;

public class AdjustStockAction implements DashboardAction {
    private final AppContext appContext;
    private final DashboardFrame frame;

    public AdjustStockAction(AppContext appContext, DashboardFrame frame) {
        this.appContext = appContext;
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        try {
            String prodName = (String) params.get("productName");
            String lotNumber = (String) params.get("lotNumber");
            int newQuantity = ((Number) params.get("quantity")).intValue();
            Usuario ator = appContext.getAuthService().getUsuarioLogado().orElse(null);

            appContext.getProdutoService().ajustarEstoqueLote(prodName, lotNumber, newQuantity, ator);
            UIMessageUtil.showInfoMessage(frame, I18n.getString("dashboard.action.stockAdjusted", lotNumber), I18n.getString("dashboard.action.actionComplete"));
            frame.refreshPanel("Produtos & Estoque");
            frame.refreshPanel("Histórico de Movimentos");
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        UIMessageUtil.showErrorMessage(frame, I18n.getString("dashboard.action.errorExecuting", e.getMessage()), I18n.getString("dashboard.action.errorTitle"));
    }
}