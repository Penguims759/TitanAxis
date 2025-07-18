package com.titanaxis.view.renderer;

import com.titanaxis.model.VendaStatus;
import com.titanaxis.util.I18n;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class HistoricoVendasTableCellRenderer extends DefaultTableCellRenderer {

    // Cores para tema claro
    private final Color FINALIZADA_LIGHT = new Color(220, 255, 220); // Verde
    private final Color ORCAMENTO_LIGHT = new Color(255, 248, 220); // Amarelo
    private final Color CANCELADA_LIGHT = new Color(255, 220, 220); // Vermelho
    private final Color PADRAO_LIGHT = UIManager.getColor("Table.background");

    // Cores para tema escuro
    private final Color FINALIZADA_DARK = new Color(10, 45, 10);
    private final Color ORCAMENTO_DARK = new Color(50, 40, 10);
    private final Color CANCELADA_DARK = new Color(60, 10, 10);
    private final Color PADRAO_DARK = UIManager.getColor("Table.background");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            int modelRow = table.convertRowIndexToModel(row);
            String status = (String) table.getModel().getValueAt(modelRow, 5); // Coluna 5 é o Status
            boolean isDarkTheme = isDark(table.getBackground());

            if (status.equalsIgnoreCase(I18n.getString("status.finalized"))) {
                c.setBackground(isDarkTheme ? FINALIZADA_DARK : FINALIZADA_LIGHT);
            } else if (status.equalsIgnoreCase(I18n.getString("status.quote"))) {
                c.setBackground(isDarkTheme ? ORCAMENTO_DARK : ORCAMENTO_LIGHT);
            } else if (status.equalsIgnoreCase(I18n.getString("status.canceled"))) {
                c.setBackground(isDarkTheme ? CANCELADA_DARK : CANCELADA_LIGHT);
            } else {
                c.setBackground(isDarkTheme ? PADRAO_DARK : PADRAO_LIGHT);
            }
        }
        return c;
    }

    private boolean isDark(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }
}