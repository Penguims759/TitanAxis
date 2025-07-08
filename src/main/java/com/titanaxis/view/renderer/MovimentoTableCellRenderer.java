package com.titanaxis.view.renderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Renderer customizado para a tabela de movimentos.
 * Destaca as linhas com cores diferentes com base no tipo de movimento.
 */
public class MovimentoTableCellRenderer extends DefaultTableCellRenderer {

    // Cores para tema claro
    private final Color VENDA_LIGHT = new Color(220, 255, 220); // Verde claro
    private final Color ENTRADA_LIGHT = new Color(220, 235, 255); // Azul claro
    private final Color AJUSTE_LIGHT = new Color(255, 248, 220); // Amarelo claro

    // Cores para tema escuro
    private final Color VENDA_DARK = new Color(10, 45, 10);   // Verde escuro
    private final Color ENTRADA_DARK = new Color(10, 25, 50);  // Azul escuro
    private final Color AJUSTE_DARK = new Color(50, 40, 10);   // Amarelo/Laranja escuro

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            String tipoMovimento = (String) table.getModel().getValueAt(row, 3);

            // Determina se o tema é claro ou escuro com base na cor de fundo da tabela
            boolean isDarkTheme = isDark(table.getBackground());

            // Aplica a cor com base no tipo de movimento e no tema
            switch (tipoMovimento) {
                case "VENDA":
                    c.setBackground(isDarkTheme ? VENDA_DARK : VENDA_LIGHT);
                    break;
                case "ENTRADA":
                    c.setBackground(isDarkTheme ? ENTRADA_DARK : ENTRADA_LIGHT);
                    break;
                case "AJUSTE":
                    c.setBackground(isDarkTheme ? AJUSTE_DARK : AJUSTE_LIGHT);
                    break;
                default:
                    // Para qualquer outro tipo, usa a cor padrão
                    c.setBackground(table.getBackground());
                    break;
            }
        }
        // Se a linha estiver selecionada, o Look and Feel padrão cuida da cor.

        return c;
    }

    /**
     * Verifica se uma cor é considerada "escura".
     * @param color A cor a ser verificada.
     * @return true se a cor for escura, false caso contrário.
     */
    private boolean isDark(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }
}