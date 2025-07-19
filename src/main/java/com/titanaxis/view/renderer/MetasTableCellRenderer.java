package com.titanaxis.view.renderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MetasTableCellRenderer extends DefaultTableCellRenderer {

    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final Color META_ATINGIDA_LIGHT = new Color(220, 255, 220);
    private final Color META_ATINGIDA_DARK = new Color(22, 80, 48);

    public MetasTableCellRenderer() {
        progressBar.setStringPainted(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Converte o índice da view para o do modelo em caso de ordenação
        int modelRow = table.convertRowIndexToModel(row);
        int progresso = (int) table.getModel().getValueAt(modelRow, 5);

        if (column == 5) { // Se for a coluna de progresso
            progressBar.setValue(progresso);
            progressBar.setForeground(getColorForProgress(progresso));
            return progressBar;
        }

        // Para as outras colunas, usa o renderer padrão
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Pinta o fundo da linha se a meta foi atingida e a linha não está selecionada
        if (!isSelected) {
            if (progresso >= 100) {
                boolean isDarkTheme = isDark(table.getBackground());
                c.setBackground(isDarkTheme ? META_ATINGIDA_DARK : META_ATINGIDA_LIGHT);
            } else {
                c.setBackground(table.getBackground());
            }
        }

        return c;
    }

    private Color getColorForProgress(int progress) {
        if (progress < 40) return new Color(220, 53, 69); // Vermelho
        if (progress < 100) return new Color(255, 193, 7); // Amarelo
        return new Color(40, 167, 69); // Verde
    }

    private boolean isDark(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }
}