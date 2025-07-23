// penguims759/titanaxis/Penguims759-TitanAxis-e9669e5c4e163f98311d4f51683c348827675c7a/src/main/java/com/titanaxis/view/panels/ChatBubbleRenderer.java
package com.titanaxis.view.panels;

import com.titanaxis.model.ai.ChatMessage;
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
            bubble.setBackground(botBubbleColor);
        } else {
            bubble = new ChatBubble(message.getText(), isUser ? userBubbleColor : botTextColor, isUser ? userTextColor : botTextColor);
            bubble.setBackground(isUser ? userBubbleColor : botBubbleColor);
        }

        // O painel principal (wrapper) usa BoxLayout para alinhar na horizontal.
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.LINE_AXIS));
        wrapper.setBackground(list.getBackground());
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        // Define uma largura máxima para o balão para que textos longos quebrem a linha.
        int maxWidth = (int) (list.getWidth() * 0.7);
        if (maxWidth > 0) {
            bubble.setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));
        }

        if (isUser) {
            // Adiciona um "espaçador" flexível (glue) que ocupa todo o espaço vazio à esquerda.
            wrapper.add(Box.createHorizontalGlue());
            wrapper.add(bubble);
        } else {
            // Adiciona o balão primeiro, e depois o espaçador, empurrando o balão para a esquerda.
            wrapper.add(bubble);
            wrapper.add(Box.createHorizontalGlue());
        }

        return wrapper;
    }
}