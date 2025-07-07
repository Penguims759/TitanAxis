package com.titanaxis.view.panels.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatBubble extends JPanel {

    public ChatBubble(String text, Color bubbleColor, Color textColor) {
        setLayout(new BorderLayout());
        setOpaque(false);

        JEditorPane textPane = new JEditorPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        String htmlText = String.format(
                "<html><body style='font-family: %s; font-size: %dpt; color: rgb(%d, %d, %d);'>%s</body></html>",
                UIManager.getFont("Label.font").getFamily(),
                UIManager.getFont("Label.font").getSize(),
                textColor.getRed(), textColor.getGreen(), textColor.getBlue(),
                text.replace("\n", "<br>")
        );
        textPane.setText(htmlText);

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bubbleColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(10, 15, 10, 15));
        bubble.add(textPane, BorderLayout.CENTER);

        add(bubble, BorderLayout.CENTER);
    }
}