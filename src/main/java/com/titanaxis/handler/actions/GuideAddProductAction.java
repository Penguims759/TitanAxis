package com.titanaxis.handler.actions;

import com.titanaxis.util.I18n;
import com.titanaxis.util.UIGuide;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.panels.ProdutoPanel;
import java.util.Map;

public class GuideAddProductAction implements DashboardAction {
    private final DashboardFrame frame;

    public GuideAddProductAction(DashboardFrame frame) {
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        frame.navigateTo(I18n.getString("dashboard.tab.productsAndBatches"));
        ProdutoPanel produtoPanel = frame.getProdutoPanel();
        if (produtoPanel != null) {
            UIGuide.highlightComponent(produtoPanel.getNovoProdutoButton());
        }
    }
}