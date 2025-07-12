// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/panels/MetasPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MetaVenda;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.FinanceiroService;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.renderer.ProgressBarTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MetasPanel extends JPanel implements DashboardFrame.Refreshable {

    private final FinanceiroService financeiroService;
    private final AuthService authService;
    private final DefaultTableModel tableModel;
    private final JComboBox<Usuario> usuarioComboBox;
    private final JComboBox<String> mesComboBox;
    private final JSpinner anoSpinner;
    private final JTextField valorMetaField;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public MetasPanel(AppContext appContext) {
        this.financeiroService = appContext.getFinanceiroService();
        this.authService = appContext.getAuthService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(I18n.getString("goals.panel.title"))); // ALTERADO

        // ALTERADO
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
        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(5).setCellRenderer(new ProgressBarTableCellRenderer());

        usuarioComboBox = new JComboBox<>();
        mesComboBox = new JComboBox<>();
        anoSpinner = new JSpinner(new SpinnerNumberModel(Calendar.getInstance().get(Calendar.YEAR), 2020, 2100, 1));
        valorMetaField = new JTextField(10);

        initComponents(table);
        carregarDadosIniciais();
    }

    private void initComponents(JTable table) {
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("goals.form.title"))); // ALTERADO

        // ALTERADO
        formPanel.add(new JLabel(I18n.getString("goals.label.user")));
        formPanel.add(usuarioComboBox);
        formPanel.add(new JLabel(I18n.getString("goals.label.month")));
        formPanel.add(mesComboBox);
        formPanel.add(new JLabel(I18n.getString("goals.label.year")));
        formPanel.add(anoSpinner);
        formPanel.add(new JLabel(I18n.getString("goals.label.goalValue")));
        formPanel.add(valorMetaField);

        JButton saveButton = new JButton(I18n.getString("goals.button.save")); // ALTERADO
        saveButton.addActionListener(e -> salvarMeta());
        formPanel.add(saveButton);

        return formPanel;
    }

    private void carregarDadosIniciais() {
        try {
            // Carregar usuários
            List<Usuario> usuarios = authService.listarTodosUsuarios();
            usuarios.forEach(usuarioComboBox::addItem);

            // Carregar meses
            Locale locale = new Locale("pt", "BR");
            for (Month month : Month.values()) {
                mesComboBox.addItem(month.getDisplayName(TextStyle.FULL, locale));
            }
            mesComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

            refreshData();
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.loadInitialData", e.getMessage()), I18n.getString("error.db.title")); // ALTERADO
        }
    }

    @Override
    public void refreshData() {
        try {
            List<MetaVenda> metas = financeiroService.listarTodasAsMetas();
            tableModel.setRowCount(0);
            Locale locale = new Locale("pt", "BR");

            for (MetaVenda meta : metas) {
                double progresso = (meta.getValorMeta() > 0) ? (meta.getValorVendido() / meta.getValorMeta()) * 100 : 0;
                String periodo = Month.of(meta.getMes()).getDisplayName(TextStyle.FULL, locale) + "/" + meta.getAno();

                tableModel.addRow(new Object[]{
                        meta.getId(),
                        meta.getUsuario().getNomeUsuario(),
                        periodo,
                        currencyFormat.format(meta.getValorMeta()),
                        currencyFormat.format(meta.getValorVendido()),
                        (int) progresso
                });
            }
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.loadGoals", e.getMessage()), I18n.getString("error.db.title")); // ALTERADO
        }
    }

    private void salvarMeta() {
        Usuario usuario = (Usuario) usuarioComboBox.getSelectedItem();
        int mes = mesComboBox.getSelectedIndex() + 1;
        int ano = (Integer) anoSpinner.getValue();
        String valorStr = valorMetaField.getText().replace(",", ".");

        if (usuario == null || valorStr.trim().isEmpty()) {
            UIMessageUtil.showWarningMessage(this, I18n.getString("goals.error.requiredFields"), I18n.getString("error.validation.title")); // ALTERADO
            return;
        }

        try {
            double valor = Double.parseDouble(valorStr);
            financeiroService.definirOuAtualizarMeta(usuario.getId(), mes, ano, valor);
            UIMessageUtil.showInfoMessage(this, I18n.getString("goals.success.save"), I18n.getString("success.title")); // ALTERADO
            refreshData();
        } catch (NumberFormatException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.invalidValue"), I18n.getString("error.format.title")); // ALTERADO
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.save", e.getMessage()), I18n.getString("error.db.title")); // ALTERADO
        }
    }
}