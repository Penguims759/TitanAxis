package com.titanaxis.view.panels.dashboard;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class PerformanceRankingsCard extends JPanel {

    private final DefaultListModel<String> topProductsModel;
    private final DefaultListModel<String> topClientsModel;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "PT"));

    public PerformanceRankingsCard() {
        setLayout(new GridLayout(1, 2, 15, 0));
        setBorder(BorderFactory.createTitledBorder("Rankings de Desempenho (Mês Atual)"));

        topProductsModel = new DefaultListModel<>();
        JList<String> topProductsList = new JList<>(topProductsModel);

        topClientsModel = new DefaultListModel<>();
        JList<String> topClientsList = new JList<>(topClientsModel);

        add(createTitledScrollPane(topProductsList, "Top 3 Produtos (por Qtd)"));
        add(createTitledScrollPane(topClientsList, "Top 3 Clientes (por Valor)"));
    }

    private JScrollPane createTitledScrollPane(JComponent content, String title) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        return scrollPane;
    }

    public void setTopProducts(Map<String, Integer> products) {
        topProductsModel.clear();
        if (products.isEmpty()) {
            topProductsModel.addElement("Nenhum produto vendido este mês.");
        } else {
            products.forEach((name, qty) -> topProductsModel.addElement(String.format("%s (%d un.)", name, qty)));
        }
    }

    public void setTopClients(Map<String, Double> clients) {
        topClientsModel.clear();
        if (clients.isEmpty()) {
            topClientsModel.addElement("Nenhuma venda a clientes este mês.");
        } else {
            clients.forEach((name, value) -> topClientsModel.addElement(String.format("%s (%s)", name, currencyFormat.format(value))));
        }
    }
}