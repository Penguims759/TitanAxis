// src/main/java/com/titanaxis/view/panels/ChatBubbleRenderer.java
package com.titanaxis.view.panels;

import com.titanaxis.model.ChatMessage;
import com.titanaxis.view.panels.components.ChatBubble;

import javax.swing.*;
import java.awt.*;

public class ChatBubbleRenderer implements ListCellRenderer<ChatMessage> {

    private final Color userBubbleColor = new Color(0, 123, 255);
    private final Color botBubbleColor = new Color(229, 231, 235);
    private final Color userTextColor = Color.WHITE;
    private final Color botTextColor = Color.BLACK;

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage message, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        ChatBubble bubble;
        boolean isUser = message.isUser();

        // Cria o balão de chat com base no tipo de mensagem
        if (message.getType() == ChatMessage.MessageType.THINKING) {
            String thinkingText = "A pensar" + ".".repeat((index % 3) + 1);
            bubble = new ChatBubble(thinkingText, botBubbleColor, botTextColor);
        } else {
            bubble = new ChatBubble(message.getText(), isUser ? userBubbleColor : botBubbleColor, isUser ? userTextColor : botTextColor);
        }

        // --- LÓGICA DE LAYOUT ---
        // Cria um painel "wrapper" para cada célula da lista para controlar o alinhamento e o tamanho
        JPanel wrapper = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Margens verticais e horizontais
        gbc.weightx = 1.0; // Permite que o painel se expanda horizontalmente
        gbc.fill = GridBagConstraints.HORIZONTAL;
        wrapper.add(Box.createGlue(), gbc); // Adiciona espaço flexível para "empurrar" o balão

        // Configura o balão
        gbc.gridy = 0;
        gbc.weightx = 0; // O balão não deve expandir
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = isUser ? GridBagConstraints.LINE_END : GridBagConstraints.LINE_START; // Alinha à direita (user) ou à esquerda (bot)

        // Define a largura máxima do balão (45% da largura da lista)
        int maxWidth = (int) (list.getWidth() * 0.45);
        bubble.setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));

        wrapper.add(bubble, gbc);
        wrapper.setBackground(list.getBackground()); // Garante que o fundo seja consistente

        return wrapper;
    }
}