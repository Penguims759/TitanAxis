package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.service.RelatorioService;
import com.titanaxis.util.AppLogger;

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
import java.util.logging.Level;

public class AuditoriaPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private JTable acoesTable;
    private JTable acessoTable;
    private DefaultTableModel acoesTableModel;
    private DefaultTableModel acessoTableModel;
    private final RelatorioService relatorioService;

    public AuditoriaPanel(AppContext appContext) {
        this.relatorioService = appContext.getRelatorioService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Logs de Auditoria do Sistema"));

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Logs de Ações", createLogPanel(true));
        tabbedPane.addTab("Logs de Acesso", createLogPanel(false));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createLogPanel(boolean isAcoes) {
        String title = isAcoes ? "Logs de Ações do Sistema" : "Logs de Acesso ao Sistema";
        String[] headers = isAcoes ?
                new String[]{"Data/Hora", "Utilizador", "Ação", "Entidade", "Detalhes"} :
                new String[]{"Data/Hora", "Utilizador", "Resultado", "Entidade", "Detalhes"};

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
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
        filterPanel.add(new JLabel("Filtrar:"));
        filterPanel.add(filterField);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> {
            if (isAcoes) loadAcoesLogs(model); else loadAcessoLogs(model);
        });
        buttonsPanel.add(refreshButton);

        JButton csvButton = new JButton("Gerar CSV");
        csvButton.addActionListener(e -> exportarLogParaCsv(isAcoes, headers));
        buttonsPanel.add(csvButton);

        JButton pdfButton = new JButton("Gerar PDF");
        pdfButton.addActionListener(e -> exportarLogParaPdf(isAcoes, headers));
        buttonsPanel.add(pdfButton);
        topPanel.add(buttonsPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        if (isAcoes) {
            acoesTableModel = model;
            acoesTable = table;
            loadAcoesLogs(model);
        } else {
            acessoTableModel = model;
            acessoTable = table;
            loadAcessoLogs(model);
        }
        return panel;
    }

    private void loadAcoesLogs(DefaultTableModel model) {
        carregarDadosDeLog(model, true);
    }

    private void loadAcessoLogs(DefaultTableModel model) {
        carregarDadosDeLog(model, false);
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
                    handleException("Erro ao carregar logs de auditoria.", e);
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
                    handleException("Erro ao gerar relatório CSV.", e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void exportarLogParaPdf(boolean isAcoes, String[] headers) {
        String title = isAcoes ? "Relatório de Auditoria de Ações" : "Relatório de Auditoria de Acesso";
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
                    handleException("Erro ao gerar relatório PDF.", e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void salvarRelatorioCsv(String nomeBase, String conteudo) {
        JFileChooser fileChooser = createFileChooser(nomeBase, "csv", "Arquivo CSV (*.csv)");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = getSelectedFileWithExtension(fileChooser, ".csv");
            try (PrintWriter out = new PrintWriter(arquivo, StandardCharsets.UTF_8)) {
                out.print(conteudo);
                JOptionPane.showMessageDialog(this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                handleException("Erro ao salvar o ficheiro.", e);
            }
        }
    }

    private void salvarRelatorioPdf(String nomeBase, ByteArrayOutputStream baos) {
        JFileChooser fileChooser = createFileChooser(nomeBase, "pdf", "Arquivo PDF (*.pdf)");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = getSelectedFileWithExtension(fileChooser, ".pdf");
            try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                baos.writeTo(fos);
                JOptionPane.showMessageDialog(this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                handleException("Erro ao salvar o ficheiro.", e);
            }
        }
    }

    private void handleException(String message, Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        AppLogger.getLogger().log(Level.SEVERE, message, cause);
        String errorMessage = "Ocorreu um erro inesperado.";
        if (cause instanceof PersistenciaException) {
            errorMessage = "Erro de Base de Dados: " + cause.getMessage();
        } else if (cause instanceof IOException) {
            errorMessage = "Erro de Ficheiro: Falha ao salvar o relatório.";
        }
        JOptionPane.showMessageDialog(this, errorMessage + "\nConsulte os logs para mais detalhes.", "Erro", JOptionPane.ERROR_MESSAGE);
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
}