// src/main/java/com/titanaxis/util/UIGuide.java
package com.titanaxis.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UIGuide {

    /**
     * Destaca um componente da UI, mudando sua borda para uma cor chamativa
     * por alguns segundos e depois a revertendo.
     *
     * @param component O componente a ser destacado.
     */
    public static void highlightComponent(JComponent component) {
        final Border originalBorder = component.getBorder();
        final Border highlightBorder = BorderFactory.createLineBorder(Color.YELLOW, 3);

        component.setBorder(highlightBorder);

        // Timer para remover o destaque após 3 segundos
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.setBorder(originalBorder);
            }
        });
        timer.setRepeats(false); // Executar apenas uma vez
        timer.start();
    }
}