package com.titanaxis.view.panels.dashboard;

import com.titanaxis.util.I18n;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class PerformanceRankingsCard extends JPanel {

    private final DefaultListModel<String> topProductsModel;
    private final DefaultListModel<String> topClientsModel;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public PerformanceRankingsCard() {
        setLayout(new GridLayout(1, 2, 15, 0));
        setBorder(BorderFactory.createTitledBorder(I18n.getString("home.rankings.title")));

        topProductsModel = new DefaultListModel<>();
        JList<String> topProductsList = new JList<>(topProductsModel);

        topClientsModel = new DefaultListModel<>();
        JList<String> topClientsList = new JList<>(topClientsModel);

        add(createTitledScrollPane(topProductsList, I18n.getString("home.rankings.topProducts")));
        add(createTitledScrollPane(topClientsList, I18n.getString("home.rankings.topClients")));
    }

    private JScrollPane createTitledScrollPane(JComponent content, String title) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        return scrollPane;
    }

    public void setTopProducts(Map<String, Integer> products) {
        topProductsModel.clear();
        if (products == null) {
            topProductsModel.addElement(I18n.getString("general.loading"));
            return;
        }

        if (products.isEmpty()) {
            topProductsModel.addElement(I18n.getString("home.rankings.noProducts"));
        } else {
            products.forEach((name, qty) -> topProductsModel.addElement(I18n.getString("home.rankings.productFormat", name, qty)));
        }
    }

    public void setTopClients(Map<String, Double> clients) {
        topClientsModel.clear();
        if (clients == null) {
            topClientsModel.addElement(I18n.getString("general.loading"));
            return;
        }

        if (clients.isEmpty()) {
            topClientsModel.addElement(I18n.getString("home.rankings.noClients"));
        } else {
            clients.forEach((name, value) -> topClientsModel.addElement(I18n.getString("home.rankings.clientFormat", name, currencyFormat.format(value))));
        }
    }
}