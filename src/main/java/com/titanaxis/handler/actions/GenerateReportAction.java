package com.titanaxis.handler.actions;

import com.titanaxis.util.I18n;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.panels.RelatorioPanel;

import java.util.Map;

public class GenerateReportAction implements DashboardAction {
    private final DashboardFrame frame;

    public GenerateReportAction(DashboardFrame frame) {
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        // Por segurança e melhor UX, a ação apenas navega para o painel de relatórios.
        // A geração do ficheiro em si deve ser iniciada pelo utilizador.
        frame.navigateTo(I18n.getString("dashboard.tab.reports"));

        // Poderíamos, no futuro, passar os parâmetros 'reportType' e 'format'
        // para o RelatorioPanel para ele pré-selecionar ou até mesmo abrir o diálogo de "Salvar Como".
    }
}