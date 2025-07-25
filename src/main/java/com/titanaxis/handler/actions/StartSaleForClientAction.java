package com.titanaxis.handler.actions;

import com.titanaxis.model.Cliente;
import com.titanaxis.util.I18n;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.panels.VendaPanel;
import java.util.Map;

public class StartSaleForClientAction implements DashboardAction {
    private final DashboardFrame frame;

    public StartSaleForClientAction(DashboardFrame frame) {
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        frame.navigateTo(I18n.getString("dashboard.tab.newSale"));
        VendaPanel vendaPanel = frame.getVendaPanel();
        if (vendaPanel != null) {
            vendaPanel.refreshData();
            Cliente cliente = (Cliente) params.get("cliente");
            if (cliente != null) {
                vendaPanel.selecionarCliente(cliente);
            }
        }
    }
}