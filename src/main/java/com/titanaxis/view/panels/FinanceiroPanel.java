// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/panels/FinanceiroPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.view.DashboardFrame;

import javax.swing.*;
import java.awt.*;

public class FinanceiroPanel extends JTabbedPane implements DashboardFrame.Refreshable {

    private final ContasAReceberPanel contasAReceberPanel;
    private final MetasPanel metasPanel;

    public FinanceiroPanel(AppContext appContext) {
        this.contasAReceberPanel = new ContasAReceberPanel(appContext);
        this.metasPanel = new MetasPanel(appContext);

        // ALTERADO
        addTab(I18n.getString("financial.tab.accountsReceivable"), this.contasAReceberPanel);
        addTab(I18n.getString("financial.tab.salesGoals"), this.metasPanel);

        // Adiciona um listener para atualizar os dados da aba selecionada
        addChangeListener(e -> {
            Component selectedComponent = getSelectedComponent();
            if (selectedComponent instanceof DashboardFrame.Refreshable) {
                ((DashboardFrame.Refreshable) selectedComponent).refreshData();
            }
        });
    }

    @Override
    public void refreshData() {
        // Quando o painel financeiro principal é atualizado, atualiza a aba que está visível
        Component selectedComponent = getSelectedComponent();
        if (selectedComponent instanceof DashboardFrame.Refreshable) {
            ((DashboardFrame.Refreshable) selectedComponent).refreshData();
        }
    }
}