package com.titanaxis.view.renderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ContasAReceberTableCellRenderer extends DefaultTableCellRenderer {

    private final Color PAGO_LIGHT = new Color(220, 255, 220); // Verde
    private final Color PENDENTE_LIGHT = UIManager.getColor("Table.background");
    private final Color ATRASADO_LIGHT = new Color(255, 220, 220); // Vermelho

    private final Color PAGO_DARK = new Color(10, 45, 10);
    private final Color PENDENTE_DARK = UIManager.getColor("Table.background");
    private final Color ATRASADO_DARK = new Color(60, 10, 10);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            String status = (String) table.getModel().getValueAt(row, 6);
            boolean isDarkTheme = isDark(table.getBackground());

            switch (status.toUpperCase()) {
                case "PAGO":
                    c.setBackground(isDarkTheme ? PAGO_DARK : PAGO_LIGHT);
                    break;
                case "ATRASADO":
                    c.setBackground(isDarkTheme ? ATRASADO_DARK : ATRASADO_LIGHT);
                    break;
                case "PENDENTE":
                default:
                    c.setBackground(isDarkTheme ? PENDENTE_DARK : PENDENTE_LIGHT);
                    break;
            }
        }
        return c;
    }

    private boolean isDark(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }
}