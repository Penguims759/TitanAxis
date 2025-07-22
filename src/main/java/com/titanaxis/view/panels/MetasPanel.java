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
import com.titanaxis.view.dialogs.MetaDialog;
import com.titanaxis.view.renderer.MetasTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetasPanel extends JPanel implements DashboardFrame.Refreshable {

    private final FinanceiroService financeiroService;
    private final AnalyticsService analyticsService;
    private final AuthService authService;
    private final AppContext appContext;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<Usuario> usuarioFiltroComboBox;
    private final JComboBox<String> statusFiltroComboBox;
    private List<MetaVenda> listaDeMetas;


    public MetasPanel(AppContext appContext) {
        this.appContext = appContext;
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
        table.setRowSorter(new TableRowSorter<>(tableModel));

        usuarioFiltroComboBox = new JComboBox<>();
        statusFiltroComboBox = new JComboBox<>(new String[]{"Todas", "Ativas", "Passadas"});

        initComponents();
        loadInitialData();
    }

    private void initComponents() {
        add(createFilterPanel(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        table.setDefaultRenderer(Object.class, new MetasTableCellRenderer());
        table.setComponentPopupMenu(createPopupMenu());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                int row = table.rowAtPoint(point);
                if (row != -1) {
                    table.setRowSelectionInterval(row, row);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    editarMeta();
                }
            }
        });

        add(scrollPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(I18n.getString("goals.label.user")));
        panel.add(usuarioFiltroComboBox);
        panel.add(new JLabel(I18n.getString("history.label.status"))); // Reutilizando
        panel.add(statusFiltroComboBox);

        JButton clearFiltersButton = new JButton(I18n.getString("button.clearFilters"));
        clearFiltersButton.addActionListener(e -> {
            usuarioFiltroComboBox.setSelectedIndex(0);
            statusFiltroComboBox.setSelectedIndex(0);
            filtrarTabela();
        });
        panel.add(clearFiltersButton);

        usuarioFiltroComboBox.addActionListener(e -> filtrarTabela());
        statusFiltroComboBox.addActionListener(e -> filtrarTabela());

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton novaMetaButton = new JButton(I18n.getString("goals.button.new"));
        novaMetaButton.addActionListener(e -> adicionarMeta());
        panel.add(novaMetaButton);
        return panel;
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem eliminarItem = new JMenuItem(I18n.getString("goals.menu.delete"));
        eliminarItem.addActionListener(e -> eliminarMeta());
        menu.add(eliminarItem);
        return menu;
    }

    private void loadInitialData() {
        try {
            usuarioFiltroComboBox.addItem(null); // Opção "Todos"
            authService.listarUsuarios().forEach(usuarioFiltroComboBox::addItem);
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.loadInitialData", e.getMessage()), I18n.getString("error.db.title"));
        }
    }

    @Override
    public void refreshData() {
        try {
            this.listaDeMetas = financeiroService.listarMetas();
            filtrarTabela();
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.loadGoals", e.getMessage()), I18n.getString("error.db.title"));
        }
    }

    private void filtrarTabela() {
        if (listaDeMetas == null) return;

        Usuario usuarioFiltro = (Usuario) usuarioFiltroComboBox.getSelectedItem();
        String statusFiltro = (String) statusFiltroComboBox.getSelectedItem();

        List<MetaVenda> metasFiltradas = listaDeMetas.stream()
                .filter(meta -> (usuarioFiltro == null || meta.getUsuario().equals(usuarioFiltro)))
                .filter(meta -> {
                    if ("Ativas".equals(statusFiltro)) {
                        return !LocalDate.now().isAfter(meta.getDataFim());
                    }
                    if ("Passadas".equals(statusFiltro)) {
                        return LocalDate.now().isAfter(meta.getDataFim());
                    }
                    return true; // "Todas"
                })
                .collect(Collectors.toList());

        popularTabela(metasFiltradas);
    }

    private void popularTabela(List<MetaVenda> metas) {
        tableModel.setRowCount(0);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        DateTimeFormatter periodFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

        for (MetaVenda meta : metas) {
            try {
                double valorVendido = analyticsService.getVendasPorVendedorNoPeriodo(meta.getUsuario().getId(), meta.getDataInicio(), meta.getDataFim());
                double progresso = meta.getValorMeta() > 0 ? (valorVendido / meta.getValorMeta()) * 100 : 0;

                String periodoFormatado = meta.getDataInicio().format(periodFormatter) + " - " + meta.getDataFim().format(periodFormatter);

                tableModel.addRow(new Object[]{
                        meta.getId(),
                        meta.getUsuario().getNomeUsuario(),
                        periodoFormatado,
                        currencyFormat.format(meta.getValorMeta()),
                        currencyFormat.format(valorVendido),
                        (int) Math.round(progresso)
                });
            } catch (PersistenciaException e) {
                System.err.println("Erro ao calcular vendas para a meta ID " + meta.getId() + ": " + e.getMessage());
            }
        }
    }

    private void adicionarMeta() {
        MetaDialog dialog = new MetaDialog((Frame) SwingUtilities.getWindowAncestor(this), appContext, null);
        dialog.setVisible(true);
        if (dialog.isSalvo()) {
            UIMessageUtil.showInfoMessage(this, I18n.getString("goals.success.save"), I18n.getString("success.title"));
            refreshData();
        }
    }

    private Optional<MetaVenda> getMetaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIMessageUtil.showWarningMessage(this, I18n.getString("goals.error.noGoalSelected"), I18n.getString("warning.title"));
            return Optional.empty();
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int metaId = (int) tableModel.getValueAt(modelRow, 0);

        try {
            return financeiroService.findMetaById(metaId);
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao buscar detalhes da meta.", "Erro");
            return Optional.empty();
        }
    }

    private void editarMeta() {
        getMetaSelecionada().ifPresent(meta -> {
            MetaDialog dialog = new MetaDialog((Frame) SwingUtilities.getWindowAncestor(this), appContext, meta);
            dialog.setVisible(true);
            if (dialog.isSalvo()) {
                UIMessageUtil.showInfoMessage(this, I18n.getString("goals.success.edit"), I18n.getString("success.title"));
                refreshData();
            }
        });
    }

    private void eliminarMeta() {
        getMetaSelecionada().ifPresent(meta -> {
            DateTimeFormatter periodFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String periodoFormatado = meta.getDataInicio().format(periodFormatter) + " a " + meta.getDataFim().format(periodFormatter);
            if (UIMessageUtil.showConfirmDialog(this, I18n.getString("goals.confirm.delete", periodoFormatado, meta.getUsuario().getNomeUsuario()), I18n.getString("goals.confirm.delete.title"))) {
                try {
                    financeiroService.deletarMeta(meta.getId());
                    UIMessageUtil.showInfoMessage(this, I18n.getString("goals.success.delete"), I18n.getString("success.title"));
                    refreshData();
                } catch (PersistenciaException e) {
                    UIMessageUtil.showErrorMessage(this, I18n.getString("goals.error.delete", e.getMessage()), I18n.getString("error.title"));
                }
            }
        });
    }
}