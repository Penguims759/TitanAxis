package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ContasAReceber;
import com.titanaxis.service.FinanceiroService;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ContasAReceberPanel extends JPanel implements DashboardFrame.Refreshable {

    private final FinanceiroService financeiroService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JCheckBox mostrarPagosCheckBox;

    public ContasAReceberPanel(AppContext appContext) {
        this.financeiroService = appContext.getFinanceiroService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Gestão de Contas a Receber"));

        tableModel = new DefaultTableModel(new String[]{"ID Parcela", "ID Venda", "Cliente", "Valor", "Vencimento", "Data Pgto.", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        mostrarPagosCheckBox = new JCheckBox("Mostrar contas já pagas");
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> refreshData());
        mostrarPagosCheckBox.addActionListener(e -> refreshData());

        topPanel.add(mostrarPagosCheckBox);
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0 && evt.getClickCount() == 2) {
                    int contaId = (int) tableModel.getValueAt(row, 0);
                    String status = (String) tableModel.getValueAt(row, 6);
                    if (!"Pago".equalsIgnoreCase(status)) {
                        confirmarPagamento(contaId);
                    }
                }
            }
        });
    }

    @Override
    public void refreshData() {
        try {
            boolean apenasPendentes = !mostrarPagosCheckBox.isSelected();
            List<ContasAReceber> contas = financeiroService.listarContasAReceber(apenasPendentes);
            tableModel.setRowCount(0);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

            for (ContasAReceber conta : contas) {
                tableModel.addRow(new Object[]{
                        conta.getId(),
                        conta.getVenda().getId(),
                        conta.getVenda().getCliente().getNome(),
                        currencyFormat.format(conta.getValorParcela()),
                        conta.getDataVencimento().format(dateFormatter),
                        conta.getDataPagamento() != null ? conta.getDataPagamento().format(dateFormatter) : "---",
                        conta.getStatus()
                });
            }
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao carregar contas a receber: " + e.getMessage(), "Erro de Base de Dados");
        }
    }

    private void confirmarPagamento(int contaId) {
        if (UIMessageUtil.showConfirmDialog(this, "Deseja marcar esta parcela como paga?", "Confirmar Pagamento")) {
            try {
                financeiroService.registrarPagamento(contaId);
                refreshData();
                UIMessageUtil.showInfoMessage(this, "Pagamento registrado com sucesso!", "Sucesso");
            } catch (PersistenciaException e) {
                UIMessageUtil.showErrorMessage(this, "Erro ao registrar pagamento: " + e.getMessage(), "Erro de Base de Dados");
            }
        }
    }
}