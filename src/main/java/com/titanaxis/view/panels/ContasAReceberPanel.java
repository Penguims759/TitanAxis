// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/panels/ContasAReceberPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ContasAReceber;
import com.titanaxis.service.FinanceiroService;
import com.titanaxis.util.I18n; // Importado
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
        setBorder(BorderFactory.createTitledBorder(I18n.getString("accountsReceivable.panel.title"))); // ALTERADO

        // ALTERADO
        tableModel = new DefaultTableModel(new String[]{
                I18n.getString("accountsReceivable.table.header.id"),
                I18n.getString("accountsReceivable.table.header.saleId"),
                I18n.getString("accountsReceivable.table.header.client"),
                I18n.getString("accountsReceivable.table.header.value"),
                I18n.getString("accountsReceivable.table.header.dueDate"),
                I18n.getString("accountsReceivable.table.header.paymentDate"),
                I18n.getString("accountsReceivable.table.header.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setDefaultRenderer(Object.class, new ContasAReceberTableCellRenderer());

        mostrarPagosCheckBox = new JCheckBox(I18n.getString("accountsReceivable.checkbox.showPaid")); // ALTERADO
        totalPendenteLabel = new JLabel(I18n.getString("accountsReceivable.label.totalPending", "R$ 0,00")); // ALTERADO
        totalAtrasadoLabel = new JLabel(I18n.getString("accountsReceivable.label.totalOverdue", "R$ 0,00")); // ALTERADO

        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton(I18n.getString("button.refresh")); // ALTERADO
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
                    if (!I18n.getString("status.paid").equalsIgnoreCase(status)) { // ALTERADO
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
                    status = I18n.getString("status.overdue"); // ALTERADO
                } else if(status.equalsIgnoreCase("Pago")){
                    status = I18n.getString("status.paid");
                } else {
                    status = I18n.getString("status.pending");
                }


                if (!status.equals(I18n.getString("status.paid"))) { // ALTERADO
                    totalPendente += conta.getValorParcela();
                    if (status.equals(I18n.getString("status.overdue"))) { // ALTERADO
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

            totalPendenteLabel.setText(I18n.getString("accountsReceivable.label.totalPending", currencyFormat.format(totalPendente))); // ALTERADO
            totalAtrasadoLabel.setText(I18n.getString("accountsReceivable.label.totalOverdue", currencyFormat.format(totalAtrasado))); // ALTERADO

        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("accountsReceivable.error.load", e.getMessage()), I18n.getString("error.db.title")); // ALTERADO
        }
    }

    private void confirmarPagamento(int contaId) {
        if (UIMessageUtil.showConfirmDialog(this, I18n.getString("accountsReceivable.dialog.confirmPayment"), I18n.getString("accountsReceivable.dialog.confirmPayment.title"))) { // ALTERADO
            try {
                financeiroService.registrarPagamento(contaId);
                refreshData();
                UIMessageUtil.showInfoMessage(this, I18n.getString("accountsReceivable.success.paymentRegistered"), I18n.getString("success.title")); // ALTERADO
            } catch (PersistenciaException e) {
                UIMessageUtil.showErrorMessage(this, I18n.getString("accountsReceivable.error.registerPayment", e.getMessage()), I18n.getString("error.db.title")); // ALTERADO
            }
        }
    }
}