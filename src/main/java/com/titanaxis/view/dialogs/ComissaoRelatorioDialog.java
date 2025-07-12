// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/dialogs/ComissaoRelatorioDialog.java
package com.titanaxis.view.dialogs;

import com.titanaxis.util.I18n; // Importado
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class ComissaoRelatorioDialog extends JDialog {

    private final JDateChooser dataInicioChooser;
    private final JDateChooser dataFimChooser;
    private boolean confirmado = false;

    public ComissaoRelatorioDialog(Frame owner) {
        super(owner, I18n.getString("commissionDialog.title"), true); // ALTERADO
        setLayout(new BorderLayout(10, 10));

        dataInicioChooser = new JDateChooser(new Date());
        dataFimChooser = new JDateChooser(new Date());

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel(I18n.getString("commissionDialog.label.startDate"))); // ALTERADO
        panel.add(dataInicioChooser);
        panel.add(new JLabel(I18n.getString("commissionDialog.label.endDate"))); // ALTERADO
        panel.add(dataFimChooser);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton(I18n.getString("commissionDialog.button.generate")); // ALTERADO
        okButton.addActionListener(e -> {
            confirmado = true;
            dispose();
        });
        JButton cancelButton = new JButton(I18n.getString("button.cancel")); // ALTERADO
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public Optional<LocalDate> getDataInicio() {
        return Optional.ofNullable(dataInicioChooser.getDate()).map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public Optional<LocalDate> getDataFim() {
        return Optional.ofNullable(dataFimChooser.getDate()).map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }
}