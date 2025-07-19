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
import java.time.YearMonth;
import java.util.Locale;
import java.util.Optional;

public class MetaDialog extends JDialog {

    private final FinanceiroService financeiroService;
    private final AuthService authService;
    private final JComboBox<Usuario> usuarioComboBox;
    private final JSpinner mesSpinner;
    private final JSpinner anoSpinner;
    private final JTextField valorMetaField;
    private boolean salvo = false;
    private final Optional<MetaVenda> metaExistente;

    public MetaDialog(Frame owner, AppContext appContext, MetaVenda meta) {
        super(owner, meta == null ? I18n.getString("goals.dialog.title.new") : I18n.getString("goals.dialog.title.edit"), true);
        this.financeiroService = appContext.getFinanceiroService();
        this.authService = appContext.getAuthService();
        this.metaExistente = Optional.ofNullable(meta);

        usuarioComboBox = new JComboBox<>();
        mesSpinner = new JSpinner(new SpinnerNumberModel(YearMonth.now().getMonthValue(), 1, 12, 1));
        anoSpinner = new JSpinner(new SpinnerNumberModel(YearMonth.now().getYear(), 2020, 2100, 1));
        valorMetaField = new JTextField(10);

        initComponents();
        loadInitialData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel.add(new JLabel(I18n.getString("goals.label.user")));
        formPanel.add(usuarioComboBox);
        formPanel.add(new JLabel(I18n.getString("goals.label.month")));
        formPanel.add(mesSpinner);
        formPanel.add(new JLabel(I18n.getString("goals.label.year")));
        formPanel.add(anoSpinner);
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
                YearMonth ym = YearMonth.parse(meta.getAnoMes());
                mesSpinner.setValue(ym.getMonthValue());
                anoSpinner.setValue(ym.getYear());
                valorMetaField.setText(String.format(Locale.US, "%.2f", meta.getValorMeta()));
                usuarioComboBox.setEnabled(false);
                mesSpinner.setEnabled(false);
                anoSpinner.setEnabled(false);
            });
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.loadInitialData", e.getMessage()), I18n.getString("error.db.title"));
            dispose();
        }
    }

    private void salvarMeta() {
        try {
            Usuario usuario = (Usuario) usuarioComboBox.getSelectedItem();
            int mes = (int) mesSpinner.getValue();
            int ano = (int) anoSpinner.getValue();
            double valor = Double.parseDouble(valorMetaField.getText().replace(",", "."));
            String anoMes = String.format("%d-%02d", ano, mes);

            MetaVenda meta = metaExistente.orElse(new MetaVenda());
            meta.setUsuario(usuario);
            meta.setAnoMes(anoMes);
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