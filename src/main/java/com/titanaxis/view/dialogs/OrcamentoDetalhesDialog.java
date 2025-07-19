package com.titanaxis.view.dialogs;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OrcamentoDetalhesDialog extends JDialog {

    private final Venda orcamento;
    private final AppContext appContext;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public OrcamentoDetalhesDialog(Frame owner, Venda orcamento, AppContext appContext) {
        super(owner, I18n.getString("quoteDetailDialog.title", orcamento.getId()), true);
        this.orcamento = orcamento;
        this.appContext = appContext;

        setSize(750, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createItemsPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(I18n.getString("quoteDetailDialog.border.info")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Coluna 0: Labels ---
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.gridy = 0; panel.add(new JLabel(I18n.getString("saleDetailDialog.label.client")), gbc);
        gbc.gridy = 1; panel.add(new JLabel(I18n.getString("saleDetailDialog.label.seller")), gbc);
        gbc.gridy = 2; panel.add(new JLabel(I18n.getString("saleDetailDialog.label.totalDiscount")), gbc);

        // --- Coluna 1: Valores ---
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0; panel.add(new JLabel(orcamento.getCliente() != null ? orcamento.getCliente().getNome() : I18n.getString("general.notSpecified")), gbc);
        gbc.gridy = 1; panel.add(new JLabel(orcamento.getUsuario() != null ? orcamento.getUsuario().getNomeUsuario() : I18n.getString("general.notAvailable")), gbc);
        gbc.gridy = 2; panel.add(new JLabel(currencyFormat.format(orcamento.getDescontoTotal())), gbc);


        // --- Coluna 2: Labels ---
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 0; panel.add(new JLabel(I18n.getString("saleDetailDialog.label.date")), gbc);
        gbc.gridy = 1; panel.add(new JLabel(I18n.getString("saleDetailDialog.label.id")), gbc);
        gbc.gridy = 2; panel.add(new JLabel(I18n.getString("saleDetailDialog.label.creditUsed")), gbc);


        // --- Coluna 3: Valores ---
        gbc.gridx = 3; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        gbc.gridy = 0; panel.add(new JLabel(orcamento.getDataVenda().format(formatter)), gbc);
        gbc.gridy = 1; panel.add(new JLabel(String.valueOf(orcamento.getId())), gbc);
        gbc.gridy = 2; panel.add(new JLabel(currencyFormat.format(orcamento.getCreditoUtilizado())), gbc);

        return panel;
    }

    private JScrollPane createItemsPanel() {
        String[] columnNames = {
                I18n.getString("saleDetailDialog.table.header.product"),
                I18n.getString("saleDetailDialog.table.header.batch"),
                I18n.getString("saleDetailDialog.table.header.quantity"),
                I18n.getString("saleDetailDialog.table.header.unitPrice"),
                I18n.getString("saleDetailDialog.table.header.discount"),
                I18n.getString("saleDetailDialog.table.header.subtotal")
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (VendaItem item : orcamento.getItens()) {
            model.addRow(new Object[]{
                    item.getProduto().getNome(),
                    item.getLote().getNumeroLote(),
                    item.getQuantidade(),
                    currencyFormat.format(item.getPrecoUnitario()),
                    currencyFormat.format(item.getDesconto()),
                    currencyFormat.format(item.getSubtotal())
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        return new JScrollPane(table);
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel totalLabel = new JLabel(I18n.getString("saleDetailDialog.label.totalValue", currencyFormat.format(orcamento.getValorTotal())));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(totalLabel, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton convertButton = new JButton(I18n.getString("history.button.convertQuote"));
        convertButton.setFont(new Font("Arial", Font.BOLD, 14));
        convertButton.addActionListener(e -> converterParaVenda());

        buttonsPanel.add(convertButton);

        panel.add(buttonsPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private void converterParaVenda() {
        if (UIMessageUtil.showConfirmDialog(this, I18n.getString("history.dialog.confirmConversion", orcamento.getId()), I18n.getString("history.dialog.confirmConversion.title"))) {
            try {
                appContext.getVendaService().converterOrcamentoEmVenda(orcamento.getId(), appContext.getAuthService().getUsuarioLogado().orElse(null));
                UIMessageUtil.showInfoMessage(this, I18n.getString("history.quoteConvertedSuccess"), I18n.getString("success.title"));
                dispose();
            } catch (Exception e) {
                UIMessageUtil.showErrorMessage(this, I18n.getString("history.error.conversionFailed", e.getMessage()), I18n.getString("history.error.conversionFailed.title"));
            }
        }
    }
}