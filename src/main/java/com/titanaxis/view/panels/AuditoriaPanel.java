// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/panels/AuditoriaPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.service.RelatorioService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

public class AuditoriaPanel extends JPanel {
    private final JTabbedPane tabbedPane;
    private final JTable acoesTable;
    private final JTable acessoTable;
    private final DefaultTableModel acoesTableModel;
    private final DefaultTableModel acessoTableModel;
    private final RelatorioService relatorioService;

    public AuditoriaPanel(AppContext appContext) {
        this.relatorioService = appContext.getRelatorioService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(I18n.getString("audit.panel.title"))); // ALTERADO

        // ALTERADO
        acoesTableModel = new DefaultTableModel(new String[]{
                I18n.getString("audit.table.header.datetime"),
                I18n.getString("audit.table.header.user"),
                I18n.getString("audit.table.header.action"),
                I18n.getString("audit.table.header.entity"),
                I18n.getString("audit.table.header.details")
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        acoesTable = new JTable(acoesTableModel);

        // ALTERADO
        acessoTableModel = new DefaultTableModel(new String[]{
                I18n.getString("audit.table.header.datetime"),
                I18n.getString("audit.table.header.user"),
                I18n.getString("audit.table.header.result"),
                I18n.getString("audit.table.header.entity"),
                I18n.getString("audit.table.header.details")
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        acessoTable = new JTable(acessoTableModel);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(I18n.getString("audit.tab.actions"), createLogPanel(true)); // ALTERADO
        tabbedPane.addTab(I18n.getString("audit.tab.access"), createLogPanel(false)); // ALTERADO

        tabbedPane.addChangeListener(e -> refreshData());
        add(tabbedPane, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createLogPanel(boolean isAcoes) {
        // As strings de cabeçalho são passadas para o método de exportação, então precisam ser internacionalizadas aqui.
        final String[] headers;
        if (isAcoes) {
            headers = new String[]{
                    I18n.getString("audit.table.header.datetime"), I18n.getString("audit.table.header.user"),
                    I18n.getString("audit.table.header.action"), I18n.getString("audit.table.header.entity"),
                    I18n.getString("audit.table.header.details")
            };
        } else {
            headers = new String[]{
                    I18n.getString("audit.table.header.datetime"), I18n.getString("audit.table.header.user"),
                    I18n.getString("audit.table.header.result"), I18n.getString("audit.table.header.entity"),
                    I18n.getString("audit.table.header.details")
            };
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        DefaultTableModel model = isAcoes ? acoesTableModel : acessoTableModel;
        JTable table = isAcoes ? acoesTable : acessoTable;

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTextField filterField = new JTextField(30);
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterField.getText()));
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel(I18n.getString("audit.label.filter"))); // ALTERADO
        filterPanel.add(filterField);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton(I18n.getString("button.refresh")); // ALTERADO
        refreshButton.addActionListener(e -> {
            if (isAcoes) loadAcoesLogs(); else loadAcessoLogs();
        });
        buttonsPanel.add(refreshButton);

        JButton csvButton = new JButton(I18n.getString("button.generateCsv")); // ALTERADO
        csvButton.addActionListener(e -> exportarLogParaCsv(isAcoes, headers));
        buttonsPanel.add(csvButton);

        JButton pdfButton = new JButton(I18n.getString("button.generatePdf")); // ALTERADO
        pdfButton.addActionListener(e -> exportarLogParaPdf(isAcoes, headers));
        buttonsPanel.add(pdfButton);
        topPanel.add(buttonsPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadAcoesLogs() {
        carregarDadosDeLog(acoesTableModel, true);
    }

    private void loadAcessoLogs() {
        carregarDadosDeLog(acessoTableModel, false);
    }

    private void carregarDadosDeLog(DefaultTableModel model, boolean isAcoes) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<List<Vector<Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vector<Object>> doInBackground() throws Exception {
                if (isAcoes) return relatorioService.getAuditoriaAcoes();
                return relatorioService.getAuditoriaAcesso();
            }

            @Override
            protected void done() {
                try {
                    List<Vector<Object>> data = get();
                    model.setRowCount(0);
                    for (Vector<Object> row : data) {
                        model.addRow(row);
                    }
                } catch (Exception e) {
                    handleException(I18n.getString("audit.error.load"), e); // ALTERADO
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void exportarLogParaCsv(boolean isAcoes, String[] headers) {
        String fileNameBase = isAcoes ? "Relatorio_Auditoria_Acoes" : "Relatorio_Auditoria_Acesso";
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                List<Vector<Object>> data = isAcoes ? relatorioService.getAuditoriaAcoes() : relatorioService.getAuditoriaAcesso();
                return relatorioService.gerarRelatorioAuditoriaCsv(data, headers);
            }
            @Override
            protected void done() {
                try {
                    String csvContent = get();
                    salvarRelatorioCsv(fileNameBase, csvContent);
                } catch (Exception e) {
                    handleException(I18n.getString("audit.error.generateCsv"), e); // ALTERADO
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void exportarLogParaPdf(boolean isAcoes, String[] headers) {
        String title = isAcoes ? I18n.getString("audit.pdf.title.actions") : I18n.getString("audit.pdf.title.access"); // ALTERADO
        String fileNameBase = isAcoes ? "Relatorio_Auditoria_Acoes" : "Relatorio_Auditoria_Acesso";
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<ByteArrayOutputStream, Void> worker = new SwingWorker<>() {
            @Override
            protected ByteArrayOutputStream doInBackground() throws Exception {
                List<Vector<Object>> data = isAcoes ? relatorioService.getAuditoriaAcoes() : relatorioService.getAuditoriaAcesso();
                return relatorioService.gerarRelatorioAuditoriaPdf(title, headers, data);
            }
            @Override
            protected void done() {
                try {
                    ByteArrayOutputStream baos = get();
                    salvarRelatorioPdf(fileNameBase, baos);
                } catch (Exception e) {
                    handleException(I18n.getString("audit.error.generatePdf"), e); // ALTERADO
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void salvarRelatorioCsv(String nomeBase, String conteudo) {
        JFileChooser fileChooser = createFileChooser(nomeBase, "csv", I18n.getString("report.fileChooser.csvFilter")); // ALTERADO
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = getSelectedFileWithExtension(fileChooser, ".csv");
            try (PrintWriter out = new PrintWriter(arquivo, StandardCharsets.UTF_8)) {
                out.print(conteudo);
                UIMessageUtil.showInfoMessage(this, I18n.getString("report.save.success"), I18n.getString("success.title")); // ALTERADO
            } catch (IOException e) {
                handleException(I18n.getString("report.save.error"), e); // ALTERADO
            }
        }
    }

    private void salvarRelatorioPdf(String nomeBase, ByteArrayOutputStream baos) {
        JFileChooser fileChooser = createFileChooser(nomeBase, "pdf", I18n.getString("report.fileChooser.pdfFilter")); // ALTERADO
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = getSelectedFileWithExtension(fileChooser, ".pdf");
            try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                baos.writeTo(fos);
                UIMessageUtil.showInfoMessage(this, I18n.getString("report.save.success"), I18n.getString("success.title")); // ALTERADO
            } catch (IOException e) {
                handleException(I18n.getString("report.save.error"), e); // ALTERADO
            }
        }
    }

    private void handleException(String message, Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        AppLogger.getLogger().error(message, cause);
        String errorMessage = I18n.getString("error.unexpected.title"); // ALTERADO
        if (cause instanceof PersistenciaException) {
            errorMessage = I18n.getString("error.db.title") + ": " + cause.getMessage(); // ALTERADO
        } else if (cause instanceof IOException) {
            errorMessage = I18n.getString("error.file.title") + ": " + I18n.getString("report.error.saveFile"); // ALTERADO
        }
        UIMessageUtil.showErrorMessage(this, errorMessage + "\n" + I18n.getString("error.seeLogs"), I18n.getString("error.title")); // ALTERADO
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
        if (tabbedPane.getSelectedIndex() == 0) {
            loadAcoesLogs();
        } else {
            loadAcessoLogs();
        }
    }
}