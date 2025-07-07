package com.titanaxis.view.panels.dashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class KPICardPanel extends JPanel {

    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final Color defaultBackgroundColor = UIManager.getColor("Panel.background").brighter();
    private final Color hoverBackgroundColor = UIManager.getColor("Panel.background").darker();

    public KPICardPanel(String title, String tooltip) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(70, 130, 180)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        setToolTipText(tooltip);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBackground(defaultBackgroundColor);

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(titleLabel, BorderLayout.NORTH);

        valueLabel = new JLabel("...", SwingConstants.LEFT);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        add(valueLabel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverBackgroundColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(defaultBackgroundColor);
            }
        });
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setValueColor(Color color) {
        valueLabel.setForeground(color);
    }
}