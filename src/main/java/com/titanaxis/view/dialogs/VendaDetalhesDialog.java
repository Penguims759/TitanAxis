// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/dialogs/VendaDetalhesDialog.java
package com.titanaxis.view.dialogs;

import com.lowagie.text.DocumentException;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.service.RelatorioService;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class VendaDetalhesDialog extends JDialog {

    private final Venda venda;
    private final RelatorioService relatorioService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public VendaDetalhesDialog(Frame owner, Venda venda, RelatorioService relatorioService) {
        super(owner, I18n.getString("saleDetailDialog.title", venda.getId()), true); // ALTERADO
        this.venda = venda;
        this.relatorioService = relatorioService;

        setSize(650, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createItemsPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(I18n.getString("saleDetailDialog.border.info"))); // ALTERADO
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Labels (coluna 0) - ALTERADO
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel(I18n.getString("saleDetailDialog.label.client")), gbc);
        gbc.gridy = 1;
        panel.add(new JLabel(I18n.getString("saleDetailDialog.label.seller")), gbc);

        // Valores (coluna 1) - ALTERADO
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        panel.add(new JLabel(venda.getCliente() != null ? venda.getCliente().getNome() : I18n.getString("general.notSpecified")), gbc);
        gbc.gridy = 1;
        panel.add(new JLabel(venda.getUsuario() != null ? venda.getUsuario().getNomeUsuario() : I18n.getString("general.notAvailable")), gbc);

        // Labels (coluna 2) - ALTERADO
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 0;
        panel.add(new JLabel(I18n.getString("saleDetailDialog.label.date")), gbc);
        gbc.gridy = 1;
        panel.add(new JLabel(I18n.getString("saleDetailDialog.label.id")), gbc);

        // Valores (coluna 3)
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        panel.add(new JLabel(venda.getDataVenda().format(formatter)), gbc);
        gbc.gridy = 1;
        panel.add(new JLabel(String.valueOf(venda.getId())), gbc);

        return panel;
    }

    private JScrollPane createItemsPanel() {
        // ALTERADO
        String[] columnNames = {
                I18n.getString("saleDetailDialog.table.header.product"),
                I18n.getString("saleDetailDialog.table.header.batch"),
                I18n.getString("saleDetailDialog.table.header.quantity"),
                I18n.getString("saleDetailDialog.table.header.unitPrice"),
                I18n.getString("saleDetailDialog.table.header.subtotal")
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (VendaItem item : venda.getItens()) {
            model.addRow(new Object[]{
                    item.getProduto().getNome(),
                    item.getLote().getNumeroLote(),
                    item.getQuantidade(),
                    currencyFormat.format(item.getPrecoUnitario()),
                    currencyFormat.format(item.getQuantidade() * item.getPrecoUnitario())
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        return new JScrollPane(table);
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel totalLabel = new JLabel(I18n.getString("saleDetailDialog.label.totalValue", currencyFormat.format(venda.getValorTotal()))); // ALTERADO
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(totalLabel, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton exportCsvButton = new JButton(I18n.getString("saleDetailDialog.button.exportCsv")); // ALTERADO
        exportCsvButton.addActionListener(e -> exportarVendaParaCsv());

        JButton exportPdfButton = new JButton(I18n.getString("saleDetailDialog.button.exportPdf")); // ALTERADO
        exportPdfButton.addActionListener(e -> exportarVendaParaPdf());

        buttonsPanel.add(exportCsvButton);
        buttonsPanel.add(exportPdfButton);

        panel.add(buttonsPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private void exportarVendaParaCsv() {
        JFileChooser fileChooser = createFileChooser("csv");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                String csvContent = relatorioService.gerarReciboVendaCsv(venda);
                try (PrintWriter out = new PrintWriter(fileToSave, StandardCharsets.UTF_8)) {
                    out.print(csvContent);
                    UIMessageUtil.showInfoMessage(this, I18n.getString("saleDetailDialog.export.success"), I18n.getString("success.title")); // ALTERADO
                }
            } catch (IOException ex) {
                UIMessageUtil.showErrorMessage(this, I18n.getString("saleDetailDialog.export.error.csv", ex.getMessage()), I18n.getString("saleDetailDialog.export.error.title")); // ALTERADO
            }
        }
    }

    private void exportarVendaParaPdf() {
        JFileChooser fileChooser = createFileChooser("pdf");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ByteArrayOutputStream baos = relatorioService.gerarReciboVendaPdf(venda);
                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    baos.writeTo(fos);
                    UIMessageUtil.showInfoMessage(this, I18n.getString("saleDetailDialog.export.success"), I18n.getString("success.title")); // ALTERADO
                }
            } catch (DocumentException | IOException ex) {
                UIMessageUtil.showErrorMessage(this, I18n.getString("saleDetailDialog.export.error.pdf", ex.getMessage()), I18n.getString("saleDetailDialog.export.error.title")); // ALTERADO
            }
        }
    }

    private JFileChooser createFileChooser(String extension) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(I18n.getString("saleDetailDialog.dialog.save.title")); // ALTERADO
        fileChooser.setSelectedFile(new File(I18n.getString("saleDetailDialog.dialog.save.fileName", venda.getId()) + "." + extension)); // ALTERADO
        fileChooser.setFileFilter(new FileNameExtensionFilter(I18n.getString("saleDetailDialog.dialog.save.filter", extension.toUpperCase(), extension), extension)); // ALTERADO
        return fileChooser;
    }
}