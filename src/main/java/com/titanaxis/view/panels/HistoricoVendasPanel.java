// src/main/java/com/titanaxis/view/panels/HistoricoVendasPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException; // IMPORTAÇÃO CORRIGIDA
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaStatus;
import com.titanaxis.presenter.HistoricoVendasPresenter;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.dialogs.DevolucaoDialog;
import com.titanaxis.view.dialogs.VendaDetalhesDialog;
import com.titanaxis.view.interfaces.HistoricoVendasView;
import com.toedter.calendar.JDateChooser;

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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HistoricoVendasPanel extends JPanel implements HistoricoVendasView, DashboardFrame.Refreshable {

    private HistoricoVendasListener listener;
    private final AppContext appContext;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JDateChooser dataInicioChooser, dataFimChooser;
    private final JComboBox<VendaStatus> statusComboBox;
    private final JTextField clienteNomeField;
    private final JButton buscarButton, limparButton, convertButton, returnButton;
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public HistoricoVendasPanel(AppContext appContext) {
        this.appContext = appContext;
        setLayout(new BorderLayout(10, 10));

        // Filtros
        dataInicioChooser = new JDateChooser();
        dataInicioChooser.setPreferredSize(new Dimension(120, 25));
        dataFimChooser = new JDateChooser();
        dataFimChooser.setPreferredSize(new Dimension(120, 25));
        statusComboBox = new JComboBox<>();
        clienteNomeField = new JTextField(20);
        buscarButton = new JButton("Buscar");
        limparButton = new JButton("Limpar Filtros");
        convertButton = new JButton("Converter Orçamento em Venda");
        returnButton = new JButton("Registrar Devolução");


        // Tabela
        String[] columnNames = {"ID", "Data", "Cliente", "Vendedor", "Valor Total", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);

        initComponents();
        new HistoricoVendasPresenter(this, appContext.getVendaService());
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
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    int vendaId = (int) tableModel.getValueAt(modelRow, 0);
                    abrirDetalhesVenda(vendaId);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Filtros de Busca"));

        statusComboBox.addItem(null);
        for (VendaStatus status : VendaStatus.values()) {
            statusComboBox.addItem(status);
        }

        buscarButton.addActionListener(e -> listener.aoAplicarFiltros());
        limparButton.addActionListener(e -> {
            dataInicioChooser.setDate(null);
            dataFimChooser.setDate(null);
            statusComboBox.setSelectedIndex(0);
            clienteNomeField.setText("");
            listener.aoLimparFiltros();
        });

        panel.add(new JLabel("Período:"));
        panel.add(dataInicioChooser);
        panel.add(new JLabel("até"));
        panel.add(dataFimChooser);
        panel.add(new JLabel("Status:"));
        panel.add(statusComboBox);
        panel.add(new JLabel("Cliente:"));
        panel.add(clienteNomeField);
        panel.add(buscarButton);
        panel.add(limparButton);

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
        String statusStr = (String) tableModel.getValueAt(modelRow, 5);

        convertButton.setEnabled(VendaStatus.ORCAMENTO.getDescricao().equals(statusStr));
        returnButton.setEnabled(VendaStatus.FINALIZADA.getDescricao().equals(statusStr));
    }

    private Optional<Venda> getSelectedVenda() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIMessageUtil.showWarningMessage(this, "Por favor, selecione uma venda na tabela.", "Nenhuma Venda Selecionada");
            return Optional.empty();
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int vendaId = (int) tableModel.getValueAt(modelRow, 0);
        try {
            return appContext.getVendaService().buscarVendaCompletaPorId(vendaId);
        } catch (PersistenciaException e) {
            mostrarErro("Erro de Base de Dados", "Não foi possível carregar os dados da venda selecionada.");
            return Optional.empty();
        }
    }

    private void converterOrcamento() {
        getSelectedVenda().ifPresent(venda -> {
            if (venda.getStatus() != VendaStatus.ORCAMENTO) return;
            if (UIMessageUtil.showConfirmDialog(this, "Deseja converter o orçamento #" + venda.getId() + " em uma venda finalizada?", "Confirmar Conversão")) {
                try {
                    appContext.getVendaService().converterOrcamentoEmVenda(venda.getId(), appContext.getAuthService().getUsuarioLogado().orElse(null));
                    UIMessageUtil.showInfoMessage(this, "Orçamento convertido em venda com sucesso!", "Sucesso");
                    refreshData();
                } catch (Exception e) {
                    mostrarErro("Erro na Conversão", "Falha ao converter orçamento: " + e.getMessage());
                }
            }
        });
    }

    private void registrarDevolucao() {
        getSelectedVenda().ifPresent(venda -> {
            if (venda.getStatus() != VendaStatus.FINALIZADA) return;
            DevolucaoDialog dialog = new DevolucaoDialog((Frame) SwingUtilities.getWindowAncestor(this), appContext, venda);
            dialog.setVisible(true);
            // Poderia adicionar lógica aqui para atualizar algo se necessário após o dialog fechar
        });
    }

    private void abrirDetalhesVenda(int vendaId) {
        try {
            setCarregando(true);
            appContext.getVendaService().buscarVendaCompletaPorId(vendaId)
                    .ifPresentOrElse(venda -> {
                        VendaDetalhesDialog dialog = new VendaDetalhesDialog((Frame) SwingUtilities.getWindowAncestor(this), venda, appContext.getRelatorioService());
                        dialog.setVisible(true);
                    }, () -> UIMessageUtil.showWarningMessage(this, "Não foi possível encontrar os detalhes para a venda selecionada.", "Aviso"));
        } catch (Exception e) {
            mostrarErro("Erro ao Abrir Detalhes", "Ocorreu um erro inesperado ao buscar os detalhes da venda: " + e.getMessage());
        } finally {
            setCarregando(false);
        }
    }

    @Override
    public void setVendasNaTabela(List<Venda> vendas) {
        tableModel.setRowCount(0);
        for (Venda v : vendas) {
            tableModel.addRow(new Object[]{
                    v.getId(),
                    v.getDataVenda().format(FORMATTER),
                    v.getCliente() != null ? v.getCliente().getNome() : "N/A",
                    v.getUsuario() != null ? v.getUsuario().getNomeUsuario() : "N/A",
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
        buscarButton.setEnabled(!carregando);
        limparButton.setEnabled(!carregando);
    }

    @Override
    public Optional<LocalDate> getDataInicio() {
        return Optional.ofNullable(dataInicioChooser.getDate()).map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @Override
    public Optional<LocalDate> getDataFim() {
        return Optional.ofNullable(dataFimChooser.getDate()).map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
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