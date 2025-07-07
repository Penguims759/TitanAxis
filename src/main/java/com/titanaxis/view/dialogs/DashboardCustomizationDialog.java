package com.titanaxis.view.dialogs;

import com.titanaxis.service.UIPersonalizationService;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DashboardCustomizationDialog extends JDialog {

    private final UIPersonalizationService personalizationService;
    private final Map<String, JCheckBox> checkBoxes = new HashMap<>();
    private final Runnable onSaveCallback;

    public DashboardCustomizationDialog(Frame owner, UIPersonalizationService personalizationService, Runnable onSaveCallback) {
        super(owner, "Personalizar Dashboard", true);
        this.personalizationService = personalizationService;
        this.onSaveCallback = onSaveCallback;

        setLayout(new BorderLayout(10, 10));
        setSize(400, 450);
        setLocationRelativeTo(owner);

        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
        checkPanel.setBorder(BorderFactory.createTitledBorder("Selecione os Componentes para Exibir"));
        checkPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // NOVOS CARTÕES ADICIONADOS
        addCheckBox(checkPanel, "kpi_cards", "Mostrar Cartões de Indicadores (KPIs)");
        addCheckBox(checkPanel, "financial_summary", "Mostrar Resumo Financeiro");
        addCheckBox(checkPanel, "inventory_snapshot", "Mostrar Fotografia do Inventário");
        addCheckBox(checkPanel, "sales_chart", "Mostrar Gráfico de Vendas");
        addCheckBox(checkPanel, "performance_rankings", "Mostrar Rankings de Desempenho");
        addCheckBox(checkPanel, "recent_activity", "Mostrar Atividade Recente");
        addCheckBox(checkPanel, "quick_actions", "Mostrar Botões de Ação Rápida");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Salvar");
        saveButton.addActionListener(e -> savePreferences());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(new JScrollPane(checkPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadPreferences();
    }

    private void addCheckBox(JPanel panel, String key, String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFont(new Font("Arial", Font.PLAIN, 14));
        checkBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        checkBoxes.put(key, checkBox);
        panel.add(checkBox);
    }

    private void loadPreferences() {
        for (Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            boolean isVisible = Boolean.parseBoolean(personalizationService.getPreference("dashboard.card." + entry.getKey(), "true"));
            entry.getValue().setSelected(isVisible);
        }
    }

    private void savePreferences() {
        for (Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            personalizationService.savePreference("dashboard.card." + entry.getKey(), String.valueOf(entry.getValue().isSelected()));
        }

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }

        dispose();
    }
}