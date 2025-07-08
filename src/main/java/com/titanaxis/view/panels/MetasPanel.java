package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MetaVenda;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.FinanceiroService;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MetasPanel extends JPanel implements DashboardFrame.Refreshable {

    private final FinanceiroService financeiroService;
    private final AuthService authService;
    private final DefaultTableModel tableModel;
    private final JComboBox<Usuario> usuarioComboBox;
    private final JSpinner mesSpinner;
    private final JSpinner anoSpinner;
    private final JTextField valorMetaField;

    public MetasPanel(AppContext appContext) {
        this.financeiroService = appContext.getFinanceiroService();
        this.authService = appContext.getAuthService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Gestão de Metas de Venda"));

        tableModel = new DefaultTableModel(new String[]{"ID", "Utilizador", "Período (Ano/Mês)", "Valor da Meta"}, 0);
        usuarioComboBox = new JComboBox<>();
        mesSpinner = new JSpinner(new SpinnerNumberModel(YearMonth.now().getMonthValue(), 1, 12, 1));
        anoSpinner = new JSpinner(new SpinnerNumberModel(YearMonth.now().getYear(), 2020, 2100, 1));
        valorMetaField = new JTextField(10);

        initComponents();
        loadUsuarios();
    }

    private void initComponents() {
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Definir/Atualizar Meta"));

        panel.add(new JLabel("Utilizador:"));
        panel.add(usuarioComboBox);
        panel.add(new JLabel("Mês:"));
        panel.add(mesSpinner);
        panel.add(new JLabel("Ano:"));
        panel.add(anoSpinner);
        panel.add(new JLabel("Valor da Meta:"));
        panel.add(valorMetaField);

        JButton saveButton = new JButton("Salvar Meta");
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
                tableModel.addRow(new Object[]{
                        meta.getId(),
                        meta.getUsuario().getNomeUsuario(),
                        meta.getAnoMes(),
                        currencyFormat.format(meta.getValorMeta())
                });
            }
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao carregar metas: " + e.getMessage(), "Erro de Base de Dados");
        }
    }

    private void loadUsuarios() {
        try {
            authService.listarUsuarios().forEach(usuarioComboBox::addItem);
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao carregar utilizadores: " + e.getMessage(), "Erro de Base de Dados");
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
            UIMessageUtil.showInfoMessage(this, "Meta salva com sucesso!", "Sucesso");
            refreshData();

        } catch (NumberFormatException e) {
            UIMessageUtil.showErrorMessage(this, "Valor da meta inválido.", "Erro de Formato");
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao salvar meta: " + e.getMessage(), "Erro");
        }
    }
}