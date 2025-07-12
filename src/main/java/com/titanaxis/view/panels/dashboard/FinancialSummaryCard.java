package com.titanaxis.view.panels.dashboard;

import com.titanaxis.util.I18n;

import javax.swing.*;
import java.awt.*;

public class FinancialSummaryCard extends JPanel {

    private final JLabel revenueValue;
    private final JLabel comparisonLabel;
    private final JLabel avgTicketValue;

    public FinancialSummaryCard() {
        setLayout(new GridLayout(3, 1, 5, 5));
        setBorder(BorderFactory.createTitledBorder(I18n.getString("home.financial.title")));

        revenueValue = createValueLabel();
        comparisonLabel = createComparisonLabel();
        avgTicketValue = createValueLabel();

        add(createMetricPanel(I18n.getString("home.financial.revenue"), revenueValue));
        add(createMetricPanel(I18n.getString("home.financial.comparison"), comparisonLabel));
        add(createMetricPanel(I18n.getString("home.financial.avgTicket"), avgTicketValue));
    }

    private JPanel createMetricPanel(String title, JComponent valueComponent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(title), BorderLayout.WEST);
        panel.add(valueComponent, BorderLayout.EAST);
        return panel;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("...");
        label.setFont(new Font("Arial", Font.BOLD, 16));
        return label;
    }

    private JLabel createComparisonLabel() {
        JLabel label = new JLabel("...");
        label.setFont(new Font("Arial", Font.ITALIC, 14));
        return label;
    }

    public void setRevenue(String value) { revenueValue.setText(value); }
    public void setAvgTicket(String value) { avgTicketValue.setText(value); }
    public void setComparison(double percentage) {
        if (Double.isNaN(percentage)) {
            comparisonLabel.setText(I18n.getString("general.notAvailable"));
            comparisonLabel.setForeground(Color.GRAY);
        } else {
            String text = String.format("%.1f%%", percentage);
            comparisonLabel.setText(percentage >= 0 ? "▲ " + text : "▼ " + text);
            comparisonLabel.setForeground(percentage >= 0 ? new Color(34, 139, 34) : Color.RED);
        }
    }
}