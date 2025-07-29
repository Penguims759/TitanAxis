package com.titanaxis.view.panels.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatBubble extends JPanel {

    public ChatBubble(String text, Color bubbleColor, Color textColor) {
        // O layout do próprio balão é simples, apenas para conter o texto.
        super(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(10, 15, 10, 15));

        // *** A MUDANÇA FUNDAMENTAL: Usar JLabel em vez de JEditorPane ***
        // JLabel é mais leve e calcula o seu tamanho de forma mais previsível com HTML.
        JLabel textLabel = new JLabel();
        textLabel.setOpaque(false);

        String htmlText = String.format(
                "<html><body style='font-family: %s; font-size: %dpt; color: rgb(%d, %d, %d);'>%s</body></html>",
                UIManager.getFont("Label.font").getFamily(),
                UIManager.getFont("Label.font").getSize(),
                textColor.getRed(), textColor.getGreen(), textColor.getBlue(),
                text.replace("\n", "<br>")
        );
        textLabel.setText(htmlText);

        add(textLabel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // **INÍCIO DA CORREÇÃO**
        // Aplicar hints de renderização para suavizar tanto as formas (anti-aliasing)
        // quanto o texto (text anti-aliasing). Isto fará com que a fonte pareça
        // muito mais suave e menos serrilhada.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // **FIM DA CORREÇÃO**

        // Desenha o fundo arredondado do balão
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        // Chama o método da superclasse para que os componentes filhos (o JLabel com o texto)
        // sejam desenhados. Eles usarão o objeto Graphics (g2d) que já tem os hints configurados.
        super.paintComponent(g);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
    }
}