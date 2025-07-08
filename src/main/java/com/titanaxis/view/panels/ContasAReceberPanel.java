package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ContasAReceber;
import com.titanaxis.service.FinanceiroService;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.renderer.ContasAReceberTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ContasAReceberPanel extends JPanel implements DashboardFrame.Refreshable {

    private final FinanceiroService financeiroService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JCheckBox mostrarPagosCheckBox;
    private final JLabel totalPendenteLabel;
    private final JLabel totalAtrasadoLabel;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

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
        table.setDefaultRenderer(Object.class, new ContasAReceberTableCellRenderer()); // Aplicar renderer

        mostrarPagosCheckBox = new JCheckBox("Mostrar contas já pagas");
        totalPendenteLabel = new JLabel("Total Pendente: R$ 0,00");
        totalAtrasadoLabel = new JLabel("Total em Atraso: R$ 0,00");

        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> refreshData());
        mostrarPagosCheckBox.addActionListener(e -> refreshData());

        topPanel.add(mostrarPagosCheckBox);
        topPanel.add(refreshButton);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        totalPendenteLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalAtrasadoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalAtrasadoLabel.setForeground(Color.RED.darker());
        bottomPanel.add(totalPendenteLabel);
        bottomPanel.add(totalAtrasadoLabel);


        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0 && evt.getClickCount() == 2) {
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

            double totalPendente = 0.0;
            double totalAtrasado = 0.0;
            LocalDate hoje = LocalDate.now();

            for (ContasAReceber conta : contas) {
                String status = conta.getStatus();
                if (status.equals("Pendente") && conta.getDataVencimento().isBefore(hoje)) {
                    status = "Atrasado";
                }

                if (!status.equals("Pago")) {
                    totalPendente += conta.getValorParcela();
                    if (status.equals("Atrasado")) {
                        totalAtrasado += conta.getValorParcela();
                    }
                }

                tableModel.addRow(new Object[]{
                        conta.getId(),
                        conta.getVenda().getId(),
                        conta.getVenda().getCliente().getNome(),
                        currencyFormat.format(conta.getValorParcela()),
                        conta.getDataVencimento().format(dateFormatter),
                        conta.getDataPagamento() != null ? conta.getDataPagamento().format(dateFormatter) : "---",
                        status
                });
            }

            totalPendenteLabel.setText("Total Pendente: " + currencyFormat.format(totalPendente));
            totalAtrasadoLabel.setText("Total em Atraso: " + currencyFormat.format(totalAtrasado));

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