// src/main/java/com/titanaxis/view/panels/MetasPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MetaVenda;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.FinanceiroService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.renderer.ProgressBarTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MetasPanel extends JPanel implements DashboardFrame.Refreshable {

    private final FinanceiroService financeiroService;
    private final AnalyticsService analyticsService;
    private final AuthService authService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<Usuario> usuarioComboBox;
    private final JSpinner mesSpinner;
    private final JSpinner anoSpinner;
    private final JTextField valorMetaField;

    public MetasPanel(AppContext appContext) {
        this.financeiroService = appContext.getFinanceiroService();
        this.analyticsService = appContext.getAnalyticsService();
        this.authService = appContext.getAuthService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(I18n.getString("goals.panel.title")));

        tableModel = new DefaultTableModel(new String[]{
                I18n.getString("goals.table.header.id"),
                I18n.getString("goals.table.header.user"),
                I18n.getString("goals.table.header.period"),
                I18n.getString("goals.table.header.goal"),
                I18n.getString("goals.table.header.sold"),
                I18n.getString("goals.table.header.progress")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(5).setCellRenderer(new ProgressBarTableCellRenderer());

        usuarioComboBox = new JComboBox<>();
        mesSpinner = new JSpinner(new SpinnerNumberModel(YearMonth.now().getMonthValue(), 1, 12, 1));
        anoSpinner = new JSpinner(new SpinnerNumberModel(YearMonth.now().getYear(), 2020, 2100, 1));
        valorMetaField = new JTextField(10);

        initComponents();
        loadUsuarios();
    }

    private void initComponents() {
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder(I18n.getString("goals.form.title")));

        panel.add(new JLabel(I18n.getString("goals.label.user")));
        panel.add(usuarioComboBox);
        panel.add(new JLabel(I18n.getString("goals.label.month")));
        panel.add(mesSpinner);
        panel.add(new JLabel(I18n.getString("goals.label.year")));
        panel.add(anoSpinner);
        panel.add(new JLabel(I18n.getString("goals.label.goalValue")));
        panel.add(valorMetaField);

        JButton saveButton = new JButton(I18n.getString("goals.button.save"));
        saveButton.addActionListener(e -> salvarMeta());
        panel.add(saveButton);

        return panel;
    }

    @Override
    public void refreshData() {
        try {
            List<MetaVenda> metas = financeiroService.listarMetas();
            tableModel.setRowCount(0);
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

            for (MetaVenda meta : metas) {
                YearMonth periodo = YearMonth.parse(meta.getAnoMes());
                LocalDate inicioPeriodo = periodo.atDay(1);
                LocalDate fimPeriodo = periodo.atEndOfMonth();

                double valorVendido = analyticsService.getVendasPorVendedorNoPeriodo(meta.getUsuario().getId(), inicioPeriodo, fimPeriodo);
                double progresso = meta.getValorMeta() > 0 ? (valorVendido / meta.getValorMeta()) * 100 : 0;

                tableModel.addRow(new Object[]{
                        meta.getId(),
                        meta.getUsuario().getNomeUsuario(),
                        meta.getAnoMes(),
                        currencyFormat.format(meta.getValorMeta()),
                        currencyFormat.format(valorVendido),
                        (int) Math.round(progresso)
                });
            }
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.loadGoals", e.getMessage()), I18n.getString("error.db.title"));
        }
    }

    private void loadUsuarios() {
        try {
            authService.listarUsuarios().forEach(usuarioComboBox::addItem);
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.loadInitialData", e.getMessage()), I18n.getString("error.db.title"));
        }
    }

    private void salvarMeta() {
        try {
            Usuario usuario = (Usuario) usuarioComboBox.getSelectedItem();
            int mes = (int) mesSpinner.getValue();
            int ano = (int) anoSpinner.getValue();
            double valor = Double.parseDouble(valorMetaField.getText().replace(",", "."));
            String anoMes = String.format("%d-%02d", ano, mes);

            MetaVenda meta = new MetaVenda();
            meta.setUsuario(usuario);
            meta.setAnoMes(anoMes);
            meta.setValorMeta(valor);

            financeiroService.salvarMeta(meta);
            UIMessageUtil.showInfoMessage(this, I18n.getString("goals.success.save"), I18n.getString("success.title"));
            refreshData();

        } catch (NumberFormatException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.invalidValue"), I18n.getString("error.format.title"));
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.save", e.getMessage()), I18n.getString("error.title"));
        }
    }
}