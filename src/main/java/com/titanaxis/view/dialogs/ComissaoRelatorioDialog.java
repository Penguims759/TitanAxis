package com.titanaxis.view.dialogs;

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
        super(owner, "Gerar Relatório de Comissões", true);
        setLayout(new BorderLayout(10, 10));

        dataInicioChooser = new JDateChooser(new Date());
        dataFimChooser = new JDateChooser(new Date());

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Data de Início:"));
        panel.add(dataInicioChooser);
        panel.add(new JLabel("Data de Fim:"));
        panel.add(dataFimChooser);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Gerar");
        okButton.addActionListener(e -> {
            confirmado = true;
            dispose();
        });
        JButton cancelButton = new JButton("Cancelar");
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