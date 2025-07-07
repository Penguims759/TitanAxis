package com.titanaxis.view.panels.dashboard;

import javax.swing.*;
import java.awt.*;

public class QuickActionsPanel extends JPanel {

    public final JButton newSaleButton, newProductButton, newClientButton;

    public QuickActionsPanel() {
        setBorder(BorderFactory.createTitledBorder("Ações Rápidas"));
        setLayout(new GridLayout(1, 3, 10, 10));

        newSaleButton = createActionButton("Nova Venda");
        newProductButton = createActionButton("Novo Produto");
        newClientButton = createActionButton("Novo Cliente");

        add(newSaleButton);
        add(newProductButton);
        add(newClientButton);
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }
}