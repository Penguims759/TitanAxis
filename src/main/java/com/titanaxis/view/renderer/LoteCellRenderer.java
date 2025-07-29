package com.titanaxis.view.renderer;

import com.titanaxis.model.Lote;
import com.titanaxis.util.I18n;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class LoteCellRenderer extends DefaultListCellRenderer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Lote) {
            Lote lote = (Lote) value;
            String validadeFormatada = (lote.getDataValidade() != null) ? lote.getDataValidade().format(DATE_FORMATTER) : I18n.getString("general.notAvailable");

            // Usando chaves do I18n para montar a string
            String displayText = I18n.getString("renderer.lote.format",
                    lote.getNumeroLote(),
                    validadeFormatada,
                    lote.getQuantidade()
            );
            setText(displayText);
        }

        return this;
    }
}