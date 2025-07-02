package com.titanaxis.view.panels;

import com.titanaxis.service.RelatorioService;
import com.titanaxis.util.AppLogger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RelatorioPanel extends JPanel {
    private final RelatorioService relatorioService;
    private static final Logger logger = AppLogger.getLogger();

    // ALTERAÇÃO: Referências para os botões para poder desativá-los
    private JButton inventarioCsvButton, inventarioPdfButton, vendasCsvButton, vendasPdfButton;

    public RelatorioPanel() {
        this.relatorioService = new RelatorioService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título Geral
        JLabel titleLabel = new JLabel("Geração de Relatórios");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Painel central
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weighty = 0.2;
        centerPanel.add(Box.createVerticalStrut(0), gbc);
        gbc.weighty = 0;

        // --- PAINEL DE RELATÓRIO DE INVENTÁRIO ---
        JPanel inventarioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inventarioPanel.setBorder(BorderFactory.createTitledBorder("Relatório de Inventário"));

        inventarioCsvButton = new JButton("Gerar CSV");
        inventarioCsvButton.addActionListener(e -> gerarRelatorioInventarioCsv());
        inventarioPanel.add(inventarioCsvButton);

        inventarioPdfButton = new JButton("Gerar PDF");
        inventarioPdfButton.addActionListener(e -> gerarRelatorioInventarioPdf());
        inventarioPanel.add(inventarioPdfButton);

        centerPanel.add(inventarioPanel, gbc);

        // --- PAINEL DE RELATÓRIO DE VENDAS ---
        JPanel vendasPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        vendasPanel.setBorder(BorderFactory.createTitledBorder("Relatório de Vendas"));

        vendasCsvButton = new JButton("Gerar CSV");
        vendasCsvButton.addActionListener(e -> gerarRelatorioVendasCsv());
        vendasPanel.add(vendasCsvButton);

        vendasPdfButton = new JButton("Gerar PDF");
        vendasPdfButton.addActionListener(e -> gerarRelatorioVendasPdf());
        vendasPanel.add(vendasPdfButton);

        centerPanel.add(vendasPanel, gbc);

        gbc.weighty = 0.8;
        centerPanel.add(Box.createVerticalStrut(0), gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    // ALTERAÇÃO: Lógica de geração movida para dentro de um SwingWorker
    private void gerarRelatorioInventarioCsv() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Inventario", "csv", "Arquivo CSV (*.csv)");
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".csv");
        setBotoesAtivados(false); // Desativa os botões

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                logger.info("Iniciando geração do relatório de inventário (CSV)...");
                return relatorioService.gerarRelatorioInventario();
            }

            @Override
            protected void done() {
                try {
                    String conteudoCSV = get(); // Obtém o resultado do doInBackground()
                    try (PrintWriter out = new PrintWriter(arquivoParaSalvar, StandardCharsets.UTF_8)) {
                        out.print(conteudoCSV);
                        JOptionPane.showMessageDialog(RelatorioPanel.this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    handleException("Erro inesperado ao gerar/salvar o relatório de inventário (CSV).", ex);
                } finally {
                    setBotoesAtivados(true); // Reativa os botões, mesmo que dê erro
                }
            }
        };
        worker.execute();
    }

    // ALTERAÇÃO: Lógica de geração movida para dentro de um SwingWorker
    private void gerarRelatorioVendasCsv() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Vendas", "csv", "Arquivo CSV (*.csv)");
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".csv");
        setBotoesAtivados(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                logger.info("Iniciando geração do relatório de vendas (CSV)...");
                return relatorioService.gerarRelatorioVendas();
            }

            @Override
            protected void done() {
                try {
                    String conteudoCSV = get();
                    try (PrintWriter out = new PrintWriter(arquivoParaSalvar, StandardCharsets.UTF_8)) {
                        out.print(conteudoCSV);
                        JOptionPane.showMessageDialog(RelatorioPanel.this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    handleException("Erro inesperado ao gerar/salvar o relatório de vendas (CSV).", ex);
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    // ALTERAÇÃO: Lógica de geração movida para dentro de um SwingWorker
    private void gerarRelatorioInventarioPdf() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Inventario", "pdf", "Arquivo PDF (*.pdf)");
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".pdf");
        setBotoesAtivados(false);

        SwingWorker<ByteArrayOutputStream, Void> worker = new SwingWorker<>() {
            @Override
            protected ByteArrayOutputStream doInBackground() throws Exception {
                logger.info("Iniciando geração do relatório de inventário (PDF)...");
                return relatorioService.gerarRelatorioInventarioPdf();
            }

            @Override
            protected void done() {
                try {
                    ByteArrayOutputStream baos = get();
                    try (FileOutputStream fos = new FileOutputStream(arquivoParaSalvar)) {
                        baos.writeTo(fos);
                        JOptionPane.showMessageDialog(RelatorioPanel.this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    handleException("Erro inesperado ao gerar/salvar o relatório de inventário (PDF).", ex);
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    // ALTERAÇÃO: Lógica de geração movida para dentro de um SwingWorker
    private void gerarRelatorioVendasPdf() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Vendas", "pdf", "Arquivo PDF (*.pdf)");
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".pdf");
        setBotoesAtivados(false);

        SwingWorker<ByteArrayOutputStream, Void> worker = new SwingWorker<>() {
            @Override
            protected ByteArrayOutputStream doInBackground() throws Exception {
                logger.info("Iniciando geração do relatório de vendas (PDF)...");
                return relatorioService.gerarRelatorioVendasPdf();
            }

            @Override
            protected void done() {
                try {
                    ByteArrayOutputStream baos = get();
                    try (FileOutputStream fos = new FileOutputStream(arquivoParaSalvar)) {
                        baos.writeTo(fos);
                        JOptionPane.showMessageDialog(RelatorioPanel.this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    handleException("Erro inesperado ao gerar/salvar o relatório de vendas (PDF).", ex);
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    // NOVO MÉTODO: Ativa/desativa os botões para dar feedback visual ao utilizador
    private void setBotoesAtivados(boolean ativado) {
        inventarioCsvButton.setEnabled(ativado);
        inventarioPdfButton.setEnabled(ativado);
        vendasCsvButton.setEnabled(ativado);
        vendasPdfButton.setEnabled(ativado);

        // Opcional: Altera o cursor para indicar que a aplicação está ocupada
        setCursor(ativado ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    // --- MÉTODOS AUXILIARES SEM ALTERAÇÃO ---
    private JFileChooser createFileChooser(String nomeBase, String extension, String description) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório");
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        fileChooser.setSelectedFile(new File(nomeBase + "_" + dataAtual + "." + extension));
        fileChooser.setFileFilter(new FileNameExtensionFilter(description, extension));
        return fileChooser;
    }

    private File getSelectedFileWithExtension(JFileChooser fileChooser, String extension) {
        File file = fileChooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(extension)) {
            file = new File(file.getParentFile(), file.getName() + extension);
        }
        return file;
    }

    private void handleException(String message, Exception ex) {
        // Assegura que o JOptionPane é mostrado na thread correta
        SwingUtilities.invokeLater(() -> {
            logger.log(Level.SEVERE, message, ex);
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado. Verifique os logs.", "Erro", JOptionPane.ERROR_MESSAGE);
        });
    }
}