// penguims759/titanaxis/Penguims759-TitanAxis-e9669e5c4e163f98311d4f51683c348827675c7a/src/main/java/com/titanaxis/view/panels/ChatBubbleRenderer.java
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

        if (message.getType() == ChatMessage.MessageType.THINKING) {
            String thinkingText = "A pensar" + ".".repeat((index % 3) + 1);
            bubble = new ChatBubble(thinkingText, botBubbleColor, botTextColor);
        } else {
            bubble = new ChatBubble(message.getText(), isUser ? userBubbleColor : botBubbleColor, isUser ? userTextColor : botTextColor);
        }

        // ALTERAÇÃO: A lógica do painel wrapper foi reescrita para alinhar corretamente.
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(list.getBackground());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.NONE;

        // Define uma largura máxima para a bolha de chat para que o texto quebre a linha.
        int maxWidth = (int) (list.getWidth() * 0.7);
        bubble.setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));

        if (isUser) {
            // Utilizador: [Espaçador Flexível] [Bolha de Chat]
            gbc.gridx = 0;
            gbc.weightx = 1.0; // O espaçador ocupa todo o espaço à esquerda.
            wrapper.add(Box.createHorizontalGlue(), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0; // A bolha ocupa apenas o seu espaço.
            gbc.anchor = GridBagConstraints.EAST;
            wrapper.add(bubble, gbc);
        } else {
            // Assistente: [Bolha de Chat] [Espaçador Flexível]
            gbc.gridx = 0;
            gbc.weightx = 0; // A bolha ocupa apenas o seu espaço.
            gbc.anchor = GridBagConstraints.WEST;
            wrapper.add(bubble, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0; // O espaçador ocupa todo o espaço à direita.
            wrapper.add(Box.createHorizontalGlue(), gbc);
        }

        return wrapper;
    }
}