// src/main/java/com/titanaxis/view/panels/RelatorioPanel.java
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

    public RelatorioPanel() {
        this.relatorioService = new RelatorioService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título Geral, posicionado no topo.
        JLabel titleLabel = new JLabel("Geração de Relatórios");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Painel central que usará GridBagLayout para centralizar o conteúdo.
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 10, 10);

        // ALTERAÇÃO: Adicionando um "espaçador" com peso menor no topo.
        // Isto cria um pequeno espaço acima dos painéis.
        gbc.weighty = 0.2; // Ex: 20% do espaço extra vai para cima
        centerPanel.add(Box.createVerticalStrut(0), gbc);
        gbc.weighty = 0; // Reset do peso para os componentes reais.

        // --- PAINEL SEPARADO PARA RELATÓRIO DE INVENTÁRIO ---
        JPanel inventarioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inventarioPanel.setBorder(BorderFactory.createTitledBorder("Relatório de Inventário"));

        JButton inventarioCsvButton = new JButton("Gerar CSV");
        inventarioCsvButton.addActionListener(e -> gerarRelatorioInventarioCsv());
        inventarioPanel.add(inventarioCsvButton);

        JButton inventarioPdfButton = new JButton("Gerar PDF");
        inventarioPdfButton.addActionListener(e -> gerarRelatorioInventarioPdf());
        inventarioPanel.add(inventarioPdfButton);

        centerPanel.add(inventarioPanel, gbc);

        // --- PAINEL SEPARADO PARA RELATÓRIO DE VENDAS ---
        JPanel vendasPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        vendasPanel.setBorder(BorderFactory.createTitledBorder("Relatório de Vendas"));

        JButton vendasCsvButton = new JButton("Gerar CSV");
        vendasCsvButton.addActionListener(e -> gerarRelatorioVendasCsv());
        vendasPanel.add(vendasCsvButton);

        JButton vendasPdfButton = new JButton("Gerar PDF");
        vendasPdfButton.addActionListener(e -> gerarRelatorioVendasPdf());
        vendasPanel.add(vendasPdfButton);

        centerPanel.add(vendasPanel, gbc);

        // ALTERAÇÃO: Adicionando um "espaçador" com peso maior em baixo.
        // Isto ocupa o resto do espaço e empurra os painéis para cima, mas não totalmente.
        gbc.weighty = 0.8; // Ex: 80% do espaço extra vai para baixo
        centerPanel.add(Box.createVerticalStrut(0), gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    // --- LÓGICA PARA GERAR RELATÓRIOS (sem alterações) ---
    private void gerarRelatorioInventarioCsv() {
        try {
            logger.info("Iniciando geração do relatório de inventário (CSV)...");
            String conteudoCSV = relatorioService.gerarRelatorioInventario();
            salvarRelatorioCsv("Relatorio_Inventario", conteudoCSV);
        } catch (Exception ex) {
            handleException("Erro inesperado ao gerar o relatório de inventário (CSV).", ex);
        }
    }

    private void gerarRelatorioVendasCsv() {
        try {
            logger.info("Iniciando geração do relatório de vendas (CSV)...");
            String conteudoCSV = relatorioService.gerarRelatorioVendas();
            salvarRelatorioCsv("Relatorio_Vendas", conteudoCSV);
        } catch (Exception ex) {
            handleException("Erro inesperado ao gerar o relatório de vendas (CSV).", ex);
        }
    }

    private void gerarRelatorioInventarioPdf() {
        try {
            logger.info("Iniciando geração do relatório de inventário (PDF)...");
            ByteArrayOutputStream baos = relatorioService.gerarRelatorioInventarioPdf();
            salvarRelatorioPdf("Relatorio_Inventario", baos);
        } catch (Exception ex) {
            handleException("Erro inesperado ao gerar o relatório de inventário (PDF).", ex);
        }
    }

    private void gerarRelatorioVendasPdf() {
        try {
            logger.info("Iniciando geração do relatório de vendas (PDF)...");
            ByteArrayOutputStream baos = relatorioService.gerarRelatorioVendasPdf();
            salvarRelatorioPdf("Relatorio_Vendas", baos);
        } catch (Exception ex) {
            handleException("Erro inesperado ao gerar o relatório de vendas (PDF).", ex);
        }
    }

    // --- MÉTODOS AUXILIARES PARA SALVAR FICHEIROS (sem alterações) ---
    private void salvarRelatorioCsv(String nomeBase, String conteudo) {
        JFileChooser fileChooser = createFileChooser(nomeBase, "csv", "Arquivo CSV (*.csv)");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".csv");
            try (PrintWriter out = new PrintWriter(arquivoParaSalvar, StandardCharsets.UTF_8)) {
                out.print(conteudo);
                JOptionPane.showMessageDialog(this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                handleException("Erro de I/O ao salvar o relatório.", ex);
            }
        }
    }

    private void salvarRelatorioPdf(String nomeBase, ByteArrayOutputStream baos) {
        JFileChooser fileChooser = createFileChooser(nomeBase, "pdf", "Arquivo PDF (*.pdf)");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".pdf");
            try (FileOutputStream fos = new FileOutputStream(arquivoParaSalvar)) {
                baos.writeTo(fos);
                JOptionPane.showMessageDialog(this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                handleException("Erro de I/O ao salvar o relatório.", ex);
            }
        }
    }

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
        logger.log(Level.SEVERE, message, ex);
        JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado. Verifique os logs.", "Erro", JOptionPane.ERROR_MESSAGE);
    }
}