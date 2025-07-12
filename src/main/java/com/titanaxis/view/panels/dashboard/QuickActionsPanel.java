package com.titanaxis.view.panels.dashboard;

import com.titanaxis.util.I18n; // Importado

import javax.swing.*;
import java.awt.*;

public class QuickActionsPanel extends JPanel {

    public final JButton newSaleButton, newProductButton, newClientButton;

    public QuickActionsPanel() {
        // CORRIGIDO: O título do painel agora é internacionalizado
        setBorder(BorderFactory.createTitledBorder(I18n.getString("quickActions.title")));
        setLayout(new GridLayout(1, 3, 10, 10));

        // CORRIGIDO: Os textos dos botões agora são internacionalizados
        newSaleButton = createActionButton(I18n.getString("quickActions.newSale"));
        newProductButton = createActionButton(I18n.getString("quickActions.newProduct"));
        newClientButton = createActionButton(I18n.getString("quickActions.newClient"));

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