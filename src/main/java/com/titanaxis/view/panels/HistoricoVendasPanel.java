package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaStatus;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.dialogs.DevolucaoDialog;
import com.titanaxis.view.dialogs.OrcamentoDetalhesDialog;
import com.titanaxis.view.dialogs.VendaDetalhesDialog;
import com.titanaxis.view.interfaces.HistoricoVendasView;
import com.titanaxis.view.renderer.HistoricoVendasTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HistoricoVendasPanel extends JPanel implements HistoricoVendasView, DashboardFrame.Refreshable {

    private HistoricoVendasListener listener;
    private AppContext appContext;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JSpinner dataInicioSpinner;
    private final JSpinner dataFimSpinner;
    private final JComboBox<VendaStatus> statusComboBox;
    private final JTextField clienteNomeField;
    private final JButton atualizarButton, limparButton, convertButton, returnButton;
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final Timer dateFilterTimer;


    public HistoricoVendasPanel() {
        setLayout(new BorderLayout(10, 10));

        SpinnerDateModel inicioModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dataInicioSpinner = new JSpinner(inicioModel);
        dataInicioSpinner.setEditor(new JSpinner.DateEditor(dataInicioSpinner, "dd/MM/yyyy"));

        SpinnerDateModel fimModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dataFimSpinner = new JSpinner(fimModel);
        dataFimSpinner.setEditor(new JSpinner.DateEditor(dataFimSpinner, "dd/MM/yyyy"));

        statusComboBox = new JComboBox<>();
        clienteNomeField = new JTextField(20);
        atualizarButton = new JButton(I18n.getString("button.refresh"));

        limparButton = new JButton(I18n.getString("button.clearFilters"));

        convertButton = new JButton(I18n.getString("history.button.convertQuote"));
        returnButton = new JButton(I18n.getString("history.button.registerReturn"));

        String[] columnNames = {
                I18n.getString("history.table.header.id"),
                I18n.getString("history.table.header.date"),
                I18n.getString("history.table.header.client"),
                I18n.getString("history.table.header.seller"),
                I18n.getString("history.table.header.totalDiscount"),
                I18n.getString("history.table.header.totalValue"),
                I18n.getString("history.table.header.status")
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);

        initComponents();

        dateFilterTimer = new Timer(500, e -> listener.aoAplicarFiltros());
        dateFilterTimer.setRepeats(false);

        dataInicioSpinner.addChangeListener(e -> dateFilterTimer.restart());
        dataFimSpinner.addChangeListener(e -> dateFilterTimer.restart());
    }

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void refreshData(){
        if(listener != null) listener.aoAplicarFiltros();
        updateActionButtons();
    }

    private void initComponents() {
        add(createFilterPanel(), BorderLayout.NORTH);

        table.setRowSorter(new TableRowSorter<>(tableModel));
        table.getSelectionModel().addListSelectionListener(e -> updateActionButtons());
        table.setDefaultRenderer(Object.class, new HistoricoVendasTableCellRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    abrirDialogoDetalhes();
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(I18n.getString("history.border.searchFilters")));

        JPanel leftFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusComboBox.addItem(null);
        for (VendaStatus status : VendaStatus.values()) {
            statusComboBox.addItem(status);
        }
        statusComboBox.addActionListener(e -> listener.aoAplicarFiltros());
        clienteNomeField.addActionListener(e -> listener.aoAplicarFiltros());

        leftFilterPanel.add(new JLabel(I18n.getString("history.label.client")));
        leftFilterPanel.add(clienteNomeField);
        leftFilterPanel.add(new JLabel(I18n.getString("history.label.status")));
        leftFilterPanel.add(statusComboBox);


        JPanel rightFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        atualizarButton.addActionListener(e -> listener.aoAplicarFiltros());
        limparButton.addActionListener(e -> {
            dataInicioSpinner.setValue(new Date());
            dataFimSpinner.setValue(new Date());
            statusComboBox.setSelectedIndex(0);
            clienteNomeField.setText("");
            listener.aoLimparFiltros();
        });

        rightFilterPanel.add(new JLabel(I18n.getString("movement.label.from")));
        rightFilterPanel.add(dataInicioSpinner);
        rightFilterPanel.add(new JLabel(I18n.getString("movement.label.to")));
        rightFilterPanel.add(dataFimSpinner);
        rightFilterPanel.add(limparButton);
        rightFilterPanel.add(atualizarButton);

        panel.add(leftFilterPanel, BorderLayout.WEST);
        panel.add(rightFilterPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        convertButton.addActionListener(e -> converterOrcamento());
        returnButton.addActionListener(e -> registrarDevolucao());

        panel.add(convertButton);
        panel.add(returnButton);
        return panel;
    }

    private void updateActionButtons() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            convertButton.setEnabled(false);
            returnButton.setEnabled(false);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String statusStr = (String) tableModel.getValueAt(modelRow, 6);

        convertButton.setEnabled(VendaStatus.ORCAMENTO.getDescricao().equals(statusStr));
        returnButton.setEnabled(VendaStatus.FINALIZADA.getDescricao().equals(statusStr));
    }

    private Optional<Venda> getSelectedVenda() {
        if (appContext == null) return Optional.empty();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIMessageUtil.showWarningMessage(this, I18n.getString("history.error.noSaleSelected"), I18n.getString("history.error.noSaleSelected.title"));
            return Optional.empty();
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int vendaId = (int) tableModel.getValueAt(modelRow, 0);
        try {
            return appContext.getVendaService().buscarVendaCompletaPorId(vendaId);
        } catch (PersistenciaException e) {
            mostrarErro(I18n.getString("error.db.title"), I18n.getString("history.error.loadSaleFailed"));
            return Optional.empty();
        }
    }

    private void converterOrcamento() {
        getSelectedVenda().ifPresent(venda -> {
            if (venda.getStatus() != VendaStatus.ORCAMENTO) return;
            if (UIMessageUtil.showConfirmDialog(this, I18n.getString("history.dialog.confirmConversion", venda.getId()), I18n.getString("history.dialog.confirmConversion.title"))) {
                try {
                    appContext.getVendaService().converterOrcamentoEmVenda(venda.getId(), appContext.getAuthService().getUsuarioLogado().orElse(null));
                    UIMessageUtil.showInfoMessage(this, I18n.getString("history.quoteConvertedSuccess"), I18n.getString("success.title"));
                    refreshData();
                } catch (Exception e) {
                    mostrarErro(I18n.getString("history.error.conversionFailed.title"), I18n.getString("history.error.conversionFailed", e.getMessage()));
                }
            }
        });
    }

    private void registrarDevolucao() {
        getSelectedVenda().ifPresent(venda -> {
            if (venda.getStatus() != VendaStatus.FINALIZADA) return;
            DevolucaoDialog dialog = new DevolucaoDialog((Frame) SwingUtilities.getWindowAncestor(this), appContext, venda);
            dialog.setVisible(true);
        });
    }

    private void abrirDialogoDetalhes() {
        getSelectedVenda().ifPresent(venda -> {
            setCarregando(true);
            try {
                if (venda.getStatus() == VendaStatus.ORCAMENTO) {
                    OrcamentoDetalhesDialog dialog = new OrcamentoDetalhesDialog((Frame) SwingUtilities.getWindowAncestor(this), venda, appContext);
                    dialog.setVisible(true);
                } else {
                    VendaDetalhesDialog dialog = new VendaDetalhesDialog((Frame) SwingUtilities.getWindowAncestor(this), venda, appContext.getRelatorioService());
                    dialog.setVisible(true);
                }
            } finally {
                setCarregando(false);
                refreshData();
            }
        });
    }

    @Override
    public void setVendasNaTabela(List<Venda> vendas) {
        tableModel.setRowCount(0);
        for (Venda v : vendas) {
            tableModel.addRow(new Object[]{
                    v.getId(),
                    v.getDataVenda().format(FORMATTER),
                    v.getCliente() != null ? v.getCliente().getNome() : I18n.getString("general.notAvailable"),
                    v.getUsuario() != null ? v.getUsuario().getNomeUsuario() : I18n.getString("general.notAvailable"),
                    CURRENCY_FORMAT.format(v.getDescontoTotal()),
                    CURRENCY_FORMAT.format(v.getValorTotal()),
                    v.getStatus().getDescricao()
            });
        }
        updateActionButtons();
    }

    @Override
    public void mostrarErro(String titulo, String mensagem) {
        UIMessageUtil.showErrorMessage(this, mensagem, titulo);
    }

    @Override
    public void setCarregando(boolean carregando) {
        setCursor(carregando ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        atualizarButton.setEnabled(!carregando);
        limparButton.setEnabled(!carregando);
    }

    @Override
    public Optional<LocalDate> getDataInicio() {
        Date date = (Date) dataInicioSpinner.getValue();
        return Optional.ofNullable(date).map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @Override
    public Optional<LocalDate> getDataFim() {
        Date date = (Date) dataFimSpinner.getValue();
        return Optional.ofNullable(date).map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @Override
    public Optional<VendaStatus> getStatusFiltro() {
        return Optional.ofNullable((VendaStatus) statusComboBox.getSelectedItem());
    }

    @Override
    public String getClienteNomeFiltro() {
        return clienteNomeField.getText().trim();
    }

    @Override
    public void setListener(HistoricoVendasListener listener) {
        this.listener = listener;
    }
}