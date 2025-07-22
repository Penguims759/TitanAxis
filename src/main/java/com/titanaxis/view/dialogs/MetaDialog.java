package com.titanaxis.view.dialogs;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MetaVenda;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.FinanceiroService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public class MetaDialog extends JDialog {

    private final FinanceiroService financeiroService;
    private final AuthService authService;
    private final JComboBox<Usuario> usuarioComboBox;
    private final JSpinner dataInicioSpinner;
    private final JSpinner dataFimSpinner;
    private final JTextField valorMetaField;
    private boolean salvo = false;
    private final Optional<MetaVenda> metaExistente;

    public MetaDialog(Frame owner, AppContext appContext, MetaVenda meta) {
        super(owner, meta == null ? I18n.getString("goals.dialog.title.new") : I18n.getString("goals.dialog.title.edit"), true);
        this.financeiroService = appContext.getFinanceiroService();
        this.authService = appContext.getAuthService();
        this.metaExistente = Optional.ofNullable(meta);

        usuarioComboBox = new JComboBox<>();
        dataInicioSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        dataInicioSpinner.setEditor(new JSpinner.DateEditor(dataInicioSpinner, "dd/MM/yyyy"));
        dataFimSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        dataFimSpinner.setEditor(new JSpinner.DateEditor(dataFimSpinner, "dd/MM/yyyy"));
        valorMetaField = new JTextField(10);

        initComponents();
        loadInitialData();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel.add(new JLabel(I18n.getString("goals.label.user")));
        formPanel.add(usuarioComboBox);
        formPanel.add(new JLabel(I18n.getString("commissionDialog.label.startDate"))); // Reutilizando
        formPanel.add(dataInicioSpinner);
        formPanel.add(new JLabel(I18n.getString("commissionDialog.label.endDate"))); // Reutilizando
        formPanel.add(dataFimSpinner);
        formPanel.add(new JLabel(I18n.getString("goals.label.goalValue")));
        formPanel.add(valorMetaField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(I18n.getString("button.save"));
        saveButton.addActionListener(e -> salvarMeta());
        JButton cancelButton = new JButton(I18n.getString("button.cancel"));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadInitialData() {
        try {
            authService.listarUsuarios().forEach(usuarioComboBox::addItem);
            metaExistente.ifPresent(meta -> {
                usuarioComboBox.setSelectedItem(meta.getUsuario());
                dataInicioSpinner.setValue(Date.from(meta.getDataInicio().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                dataFimSpinner.setValue(Date.from(meta.getDataFim().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                valorMetaField.setText(String.format(Locale.US, "%.2f", meta.getValorMeta()));
                usuarioComboBox.setEnabled(false);
            });
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.loadInitialData", e.getMessage()), I18n.getString("error.db.title"));
            dispose();
        }
    }

    private void salvarMeta() {
        try {
            Usuario usuario = (Usuario) usuarioComboBox.getSelectedItem();
            LocalDate dataInicio = ((Date) dataInicioSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate dataFim = ((Date) dataFimSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            double valor = Double.parseDouble(valorMetaField.getText().replace(",", "."));

            if (dataInicio.isAfter(dataFim)) {
                UIMessageUtil.showWarningMessage(this, I18n.getString("goals.error.invalidDateRange"), I18n.getString("error.validation.title"));
                return;
            }

            MetaVenda meta = metaExistente.orElse(new MetaVenda());
            meta.setUsuario(usuario);
            meta.setDataInicio(dataInicio);
            meta.setDataFim(dataFim);
            meta.setValorMeta(valor);

            financeiroService.salvarMeta(meta);
            salvo = true;
            dispose();

        } catch (NumberFormatException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.invalidValue"), I18n.getString("error.format.title"));
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.save", e.getMessage()), I18n.getString("error.title"));
        }
    }

    public boolean isSalvo() {
        return salvo;
    }
}