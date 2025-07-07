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

        JPanel wrapper = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        wrapper.add(Box.createGlue(), gbc);

        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = isUser ? GridBagConstraints.LINE_END : GridBagConstraints.LINE_START;

        int maxWidth = (int) (list.getWidth() * 0.45);
        bubble.setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));

        wrapper.add(bubble, gbc);
        wrapper.setBackground(list.getBackground());

        return wrapper;
    }
}