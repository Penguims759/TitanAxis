// src/main/java/com/titanaxis/view/renderer/ProgressBarTableCellRenderer.java
package com.titanaxis.view.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ProgressBarTableCellRenderer extends JProgressBar implements TableCellRenderer {

    public ProgressBarTableCellRenderer() {
        super(0, 100);
        setStringPainted(true);
        setForeground(new Color(34, 139, 34)); // Cor verde para a barra
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int progress = 0;
        if (value instanceof Number) {
            progress = ((Number) value).intValue();
        }
        setValue(progress);
        return this;
    }
}