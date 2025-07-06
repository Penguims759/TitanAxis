// src/main/java/com/titanaxis/view/panels/ChatBubbleRenderer.java
package com.titanaxis.view.panels;

import com.titanaxis.model.ChatMessage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatBubbleRenderer extends JPanel implements ListCellRenderer<ChatMessage> {

    private final JLabel icon = new JLabel();
    private final JTextArea textArea = new JTextArea();
    private final JPanel bubblePanel;
    private final JPanel iconPanel;
    private final JPanel contentPanel;

    public ChatBubbleRenderer() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 5, 5, 5));

        // Ícone
        icon.setFont(new Font("Arial", Font.PLAIN, 24));
        iconPanel = new JPanel(new BorderLayout());
        iconPanel.add(icon, BorderLayout.NORTH);

        // Área de Texto
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setOpaque(false);

        // Painel que desenha o balão arredondado
        bubblePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubblePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bubblePanel.setOpaque(false);
        bubblePanel.add(textArea, BorderLayout.CENTER);

        // Painel que contém o balão para controlar o tamanho
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(bubblePanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage message, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        // Limpa o layout anterior antes de reconfigurar
        removeAll();

        // Configura o texto e a cor
        textArea.setText(message.getText());

        // Define a largura do espaçador para 60%, forçando o balão a ter no máximo 40%
        int spacerWidth = (int) (list.getWidth() * 0.60);

        if (message.isUser()) {
            icon.setText("👤");
            bubblePanel.setBackground(new Color(0, 123, 255));
            textArea.setForeground(Color.WHITE);

            // Adiciona um espaçador de 60% à esquerda para empurrar para a direita
            add(Box.createHorizontalStrut(spacerWidth), BorderLayout.WEST);
            add(contentPanel, BorderLayout.CENTER);
            iconPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
            add(iconPanel, BorderLayout.EAST);
        } else {
            icon.setText("🤖");
            bubblePanel.setBackground(new Color(229, 231, 235));
            textArea.setForeground(Color.BLACK);

            // Adiciona um espaçador de 60% à direita para empurrar para a esquerda
            iconPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
            add(iconPanel, BorderLayout.WEST);
            add(contentPanel, BorderLayout.CENTER);
            add(Box.createHorizontalStrut(spacerWidth), BorderLayout.EAST);
        }

        // Define a cor de fundo de toda a célula
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        iconPanel.setBackground(getBackground());

        return this;
    }
}