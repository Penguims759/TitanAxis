// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/panels/RelatorioPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.service.RelatorioService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.dialogs.ComissaoRelatorioDialog;

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

    private final JButton inventarioCsvButton, inventarioPdfButton, vendasCsvButton, vendasPdfButton, comissoesButton;

    public RelatorioPanel(AppContext appContext) {
        this.relatorioService = appContext.getRelatorioService();

        // ALTERADO
        inventarioCsvButton = new JButton(I18n.getString("report.button.generateCsv"));
        inventarioPdfButton = new JButton(I18n.getString("report.button.generatePdf"));
        vendasCsvButton = new JButton(I18n.getString("report.button.generateCsv"));
        vendasPdfButton = new JButton(I18n.getString("report.button.generatePdf"));
        comissoesButton = new JButton(I18n.getString("report.button.generateCommissionReport"));

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(I18n.getString("report.panel.title")); // ALTERADO
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel inventarioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inventarioPanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("report.panel.inventoryTitle"))); // ALTERADO
        inventarioCsvButton.addActionListener(e -> gerarRelatorioInventarioCsv());
        inventarioPanel.add(inventarioCsvButton);
        inventarioPdfButton.addActionListener(e -> gerarRelatorioInventarioPdf());
        inventarioPanel.add(inventarioPdfButton);
        centerPanel.add(inventarioPanel, gbc);

        JPanel vendasPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        vendasPanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("report.panel.salesTitle"))); // ALTERADO
        vendasCsvButton.addActionListener(e -> gerarRelatorioVendasCsv());
        vendasPanel.add(vendasCsvButton);
        vendasPdfButton.addActionListener(e -> gerarRelatorioVendasPdf());
        vendasPanel.add(vendasPdfButton);
        centerPanel.add(vendasPanel, gbc);

        JPanel comissoesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        comissoesPanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("report.panel.commissionTitle"))); // ALTERADO
        comissoesButton.addActionListener(e -> gerarRelatorioComissoes());
        comissoesPanel.add(comissoesButton);
        centerPanel.add(comissoesPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void gerarRelatorioComissoes() {
        ComissaoRelatorioDialog dialog = new ComissaoRelatorioDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);

        if (dialog.isConfirmado()) {
            LocalDate inicio = dialog.getDataInicio().orElse(LocalDate.now().withDayOfMonth(1));
            LocalDate fim = dialog.getDataFim().orElse(LocalDate.now());
            UIMessageUtil.showInfoMessage(this, I18n.getString("report.panel.commission.futureFeature"), I18n.getString("warning.title")); // ALTERADO
        }
    }


    private void gerarRelatorioInventarioCsv() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Inventario", "csv", I18n.getString("report.fileChooser.csvFilter")); // ALTERADO
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
                    handleException(I18n.getString("report.error.generateCsvInventory"), ex); // ALTERADO
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    private void gerarRelatorioVendasCsv() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Vendas", "csv", I18n.getString("report.fileChooser.csvFilter")); // ALTERADO
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
                    handleException(I18n.getString("report.error.generateCsvSales"), ex); // ALTERADO
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    private void gerarRelatorioInventarioPdf() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Inventario", "pdf", I18n.getString("report.fileChooser.pdfFilter")); // ALTERADO
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
                    handleException(I18n.getString("report.error.generatePdfInventory"), ex); // ALTERADO
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

    private void gerarRelatorioVendasPdf() {
        JFileChooser fileChooser = createFileChooser("Relatorio_Vendas", "pdf", I18n.getString("report.fileChooser.pdfFilter")); // ALTERADO
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
                    handleException(I18n.getString("report.error.generatePdfSales"), ex); // ALTERADO
                } finally {
                    setBotoesAtivados(true);
                }
            }
        };
        worker.execute();
    }

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

        if (UIMessageUtil.showConfirmDialog(this, I18n.getString("report.dialog.saveSuccess", file.getAbsolutePath()), I18n.getString("success.title"))) { // ALTERADO
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    handleException(I18n.getString("report.error.openFile"), ex); // ALTERADO
                }
            } else {
                UIMessageUtil.showWarningMessage(this, I18n.getString("report.error.unsupportedOpenFile"), I18n.getString("warning.title")); // ALTERADO
            }
        }
    }

    private void setBotoesAtivados(boolean ativado) {
        inventarioCsvButton.setEnabled(ativado);
        inventarioPdfButton.setEnabled(ativado);
        vendasCsvButton.setEnabled(ativado);
        vendasPdfButton.setEnabled(ativado);
        comissoesButton.setEnabled(ativado);
        setCursor(ativado ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void handleException(String message, Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        logger.log(Level.SEVERE, message, cause);
        String errorMessage = I18n.getString("error.unexpected.title"); // ALTERADO
        if (cause instanceof PersistenciaException) {
            errorMessage = I18n.getString("error.db.title") + ": " + cause.getMessage(); // ALTERADO
        } else if (cause instanceof IOException) {
            errorMessage = I18n.getString("error.file.title") + ": " + I18n.getString("report.error.saveFile"); // ALTERADO
        }
        UIMessageUtil.showErrorMessage(this, errorMessage + "\n" + I18n.getString("error.seeLogs"), "Erro"); // ALTERADO
    }

    private JFileChooser createFileChooser(String nomeBase, String ext, String desc) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(I18n.getString("report.fileChooser.title")); // ALTERADO
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