package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.service.RelatorioService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.UIMessageUtil;

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

    private final JButton inventarioCsvButton, inventarioPdfButton, vendasCsvButton, vendasPdfButton;

    public RelatorioPanel(AppContext appContext) {
        this.relatorioService = appContext.getRelatorioService();

        inventarioCsvButton = new JButton("Gerar CSV");
        inventarioPdfButton = new JButton("Gerar PDF");
        vendasCsvButton = new JButton("Gerar CSV");
        vendasPdfButton = new JButton("Gerar PDF");

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Geração de Relatórios");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel inventarioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inventarioPanel.setBorder(BorderFactory.createTitledBorder("Relatório de Inventário"));
        inventarioCsvButton.addActionListener(e -> gerarRelatorioInventarioCsv());
        inventarioPanel.add(inventarioCsvButton);
        inventarioPdfButton.addActionListener(e -> gerarRelatorioInventarioPdf());
        inventarioPanel.add(inventarioPdfButton);
        centerPanel.add(inventarioPanel, gbc);

        JPanel vendasPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        vendasPanel.setBorder(BorderFactory.createTitledBorder("Relatório de Vendas"));
        vendasCsvButton.addActionListener(e -> gerarRelatorioVendasCsv());
        vendasPanel.add(vendasCsvButton);
        vendasPdfButton.addActionListener(e -> gerarRelatorioVendasPdf());
        vendasPanel.add(vendasPdfButton);
        centerPanel.add(vendasPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void gerarRelatorioInventarioCsv() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Inventario", "csv", "Arquivo CSV (*.csv)");
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".csv");
        setBotoesAtivados(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return relatorioService.gerarRelatorioInventario();
            }

            @Override
            protected void done() {
                try {
                    String conteudoCSV = get();
                    salvarEabrirRelatorio(arquivoParaSalvar, conteudoCSV, null);
                } catch (Exception ex) {
                    handleException("Erro ao gerar/salvar o relatório de inventário (CSV).", ex);
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    private void gerarRelatorioVendasCsv() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Vendas", "csv", "Arquivo CSV (*.csv)");
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".csv");
        setBotoesAtivados(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return relatorioService.gerarRelatorioVendas();
            }

            @Override
            protected void done() {
                try {
                    String conteudoCSV = get();
                    salvarEabrirRelatorio(arquivoParaSalvar, conteudoCSV, null);
                } catch (Exception ex) {
                    handleException("Erro ao gerar/salvar o relatório de vendas (CSV).", ex);
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    private void gerarRelatorioInventarioPdf() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Inventario", "pdf", "Arquivo PDF (*.pdf)");
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".pdf");
        setBotoesAtivados(false);

        SwingWorker<ByteArrayOutputStream, Void> worker = new SwingWorker<>() {
            @Override
            protected ByteArrayOutputStream doInBackground() throws Exception {
                return relatorioService.gerarRelatorioInventarioPdf();
            }

            @Override
            protected void done() {
                try {
                    ByteArrayOutputStream baos = get();
                    salvarEabrirRelatorio(arquivoParaSalvar, null, baos);
                } catch (Exception ex) {
                    handleException("Erro ao gerar/salvar o relatório de inventário (PDF).", ex);
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    private void gerarRelatorioVendasPdf() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Vendas", "pdf", "Arquivo PDF (*.pdf)");
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File arquivoParaSalvar = getSelectedFileWithExtension(fileChooser, ".pdf");
        setBotoesAtivados(false);

        SwingWorker<ByteArrayOutputStream, Void> worker = new SwingWorker<>() {
            @Override
            protected ByteArrayOutputStream doInBackground() throws Exception {
                return relatorioService.gerarRelatorioVendasPdf();
            }

            @Override
            protected void done() {
                try {
                    ByteArrayOutputStream baos = get();
                    salvarEabrirRelatorio(arquivoParaSalvar, null, baos);
                } catch (Exception ex) {
                    handleException("Erro ao gerar/salvar o relatório de vendas (PDF).", ex);
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    // NOVO MÉTODO: Centraliza a lógica de salvar e perguntar se abre o ficheiro
    private void salvarEabrirRelatorio(File file, String csvContent, ByteArrayOutputStream pdfContent) throws IOException {
        if (csvContent != null) {
            try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
                out.print(csvContent);
            }
        } else if (pdfContent != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                pdfContent.writeTo(fos);
            }
        }

        if (UIMessageUtil.showConfirmDialog(this, "Relatório salvo com sucesso em:\n" + file.getAbsolutePath() + "\n\nDeseja abri-lo agora?", "Sucesso")) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    handleException("Não foi possível abrir o ficheiro. Verifique se tem um programa instalado para abrir este tipo de ficheiro.", ex);
                }
            } else {
                UIMessageUtil.showWarningMessage(this, "A sua plataforma não suporta a abertura automática de ficheiros.", "Aviso");
            }
        }
    }

    private void setBotoesAtivados(boolean ativado) {
        inventarioCsvButton.setEnabled(ativado);
        inventarioPdfButton.setEnabled(ativado);
        vendasCsvButton.setEnabled(ativado);
        vendasPdfButton.setEnabled(ativado);
        setCursor(ativado ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void handleException(String message, Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        logger.log(Level.SEVERE, message, cause);
        String errorMessage = "Ocorreu um erro inesperado.";
        if (cause instanceof PersistenciaException) {
            errorMessage = "Erro de Base de Dados: " + cause.getMessage();
        } else if (cause instanceof IOException) {
            errorMessage = "Erro de Ficheiro: Falha ao salvar o relatório.";
        }
        UIMessageUtil.showErrorMessage(this, errorMessage + "\nConsulte os logs para mais detalhes.", "Erro");
    }

    private JFileChooser createFileChooser(String nomeBase, String ext, String desc) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salvar Relatório");
        fc.setSelectedFile(new File(nomeBase + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "." + ext));
        fc.setFileFilter(new FileNameExtensionFilter(desc, ext));
        return fc;
    }

    private File getSelectedFileWithExtension(JFileChooser fc, String ext) {
        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(ext)) {
            file = new File(file.getParentFile(), file.getName() + ext);
        }
        return file;
    }

    public void refreshData() {
        logger.info("RelatorioPanel refreshData() chamado. Botões de relatório reativados.");
        setBotoesAtivados(true);
    }
}