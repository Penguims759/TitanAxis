package com.titanaxis.view.dialogs;

import com.lowagie.text.DocumentException;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.service.RelatorioService;
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
        super(owner, "Detalhes da Venda #" + venda.getId(), true);
        this.venda = venda;
        this.relatorioService = relatorioService;

        setSize(650, 450); // Ligeiramente mais largo para acomodar os botões
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createItemsPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    // MÉTODO ALTERADO: Usa GridBagLayout para um alinhamento perfeito
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Informações da Venda"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Labels (coluna 0)
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Cliente:"), gbc);
        gbc.gridy = 1;
        panel.add(new JLabel("Vendedor:"), gbc);

        // Valores (coluna 1)
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Ocupa o espaço horizontal
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        panel.add(new JLabel(venda.getCliente() != null ? venda.getCliente().getNome() : "Não especificado"), gbc);
        gbc.gridy = 1;
        panel.add(new JLabel(venda.getUsuario() != null ? venda.getUsuario().getNomeUsuario() : "N/A"), gbc);

        // Labels (coluna 2)
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 0;
        panel.add(new JLabel("Data da Venda:"), gbc);
        gbc.gridy = 1;
        panel.add(new JLabel("ID da Venda:"), gbc);

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
        String[] columnNames = {"Produto", "Lote", "Quantidade", "Preço Unit.", "Subtotal"};
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

    // MÉTODO ALTERADO: Adicionado botão de CSV e painel de botões
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel totalLabel = new JLabel("Total da Venda: " + currencyFormat.format(venda.getValorTotal()));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(totalLabel, BorderLayout.WEST);

        // Painel para os botões de exportação
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton exportCsvButton = new JButton("Exportar para CSV");
        exportCsvButton.addActionListener(e -> exportarVendaParaCsv());

        JButton exportPdfButton = new JButton("Exportar para PDF");
        exportPdfButton.addActionListener(e -> exportarVendaParaPdf());

        buttonsPanel.add(exportCsvButton);
        buttonsPanel.add(exportPdfButton);

        panel.add(buttonsPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    // NOVO MÉTODO
    private void exportarVendaParaCsv() {
        JFileChooser fileChooser = createFileChooser("csv");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                String csvContent = relatorioService.gerarReciboVendaCsv(venda);
                try (PrintWriter out = new PrintWriter(fileToSave, StandardCharsets.UTF_8)) {
                    out.print(csvContent);
                    UIMessageUtil.showInfoMessage(this, "Recibo da venda exportado com sucesso!", "Sucesso");
                }
            } catch (IOException ex) {
                UIMessageUtil.showErrorMessage(this, "Erro ao salvar o ficheiro CSV: " + ex.getMessage(), "Erro de Exportação");
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
                    UIMessageUtil.showInfoMessage(this, "Recibo da venda exportado com sucesso!", "Sucesso");
                }
            } catch (DocumentException | IOException ex) {
                UIMessageUtil.showErrorMessage(this, "Erro ao gerar ou salvar o PDF: " + ex.getMessage(), "Erro de Exportação");
            }
        }
    }

    // NOVO MÉTODO: Auxiliar para criar o JFileChooser
    private JFileChooser createFileChooser(String extension) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Recibo da Venda");
        fileChooser.setSelectedFile(new File("Venda_" + venda.getId() + "." + extension));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivo " + extension.toUpperCase() + " (*." + extension + ")", extension));
        return fileChooser;
    }
}