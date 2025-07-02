package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.service.RelatorioService;

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
    private JTabbedPane tabbedPane;
    private JTable acoesTable;
    private JTable acessoTable;
    private DefaultTableModel acoesTableModel;
    private DefaultTableModel acessoTableModel;
    private RelatorioService relatorioService;

    public AuditoriaPanel(AppContext appContext) {
        this.relatorioService = appContext.getRelatorioService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Logs de Auditoria do Sistema"));

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Logs de Ações", createAcoesLogPanel());
        tabbedPane.addTab("Logs de Acesso", createAcessoLogPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createLogPanel(String title, String[] headers, boolean isAcoes) {
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

        JButton refreshButton = new JButton("Atualizar");
        if(isAcoes) refreshButton.addActionListener(e -> loadAcoesLogs());
        else refreshButton.addActionListener(e -> loadAcessoLogs());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(refreshButton);
        JButton csvButton = new JButton("Gerar CSV");
        csvButton.addActionListener(e -> exportarLogParaCsv(isAcoes));
        buttonsPanel.add(csvButton);

        JButton pdfButton = new JButton("Gerar PDF");
        pdfButton.addActionListener(e -> exportarLogParaPdf(isAcoes));
        buttonsPanel.add(pdfButton);

        topPanel.add(buttonsPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        if (isAcoes) {
            acoesTableModel = model;
            acoesTable = table;
        } else {
            acessoTableModel = model;
            acessoTable = table;
        }

        return panel;
    }

    private JPanel createAcoesLogPanel() {
        JPanel panel = createLogPanel("Logs de Ações", new String[]{"Data/Hora", "Utilizador", "Ação", "Entidade", "Detalhes"}, true);
        loadAcoesLogs();
        return panel;
    }

    private JPanel createAcessoLogPanel() {
        JPanel panel = createLogPanel("Logs de Acesso", new String[]{"Data/Hora", "Utilizador", "Resultado", "Entidade", "Detalhes"}, false);
        loadAcessoLogs();
        return panel;
    }

    private void loadAcoesLogs() {
        List<Vector<Object>> data = relatorioService.getAuditoriaAcoes();
        populateTable(data, acoesTableModel);
    }

    private void loadAcessoLogs() {
        List<Vector<Object>> data = relatorioService.getAuditoriaAcesso();
        populateTable(data, acessoTableModel);
    }

    private void populateTable(List<Vector<Object>> data, DefaultTableModel model) {
        model.setRowCount(0);
        for(Vector<Object> row : data) {
            model.addRow(row);
        }
    }

    private void exportarLogParaCsv(boolean isAcoes) {
        String[] headers = isAcoes ? new String[]{"Data/Hora", "Utilizador", "Ação", "Entidade", "Detalhes"} : new String[]{"Data/Hora", "Utilizador", "Resultado", "Entidade", "Detalhes"};
        List<Vector<Object>> data = isAcoes ? relatorioService.getAuditoriaAcoes() : relatorioService.getAuditoriaAcesso();
        String csvContent = relatorioService.gerarRelatorioAuditoriaCsv(data, headers);
        String fileNameBase = isAcoes ? "Relatorio_Auditoria_Acoes" : "Relatorio_Auditoria_Acesso";
        salvarRelatorioCsv(fileNameBase, csvContent);
    }

    private void exportarLogParaPdf(boolean isAcoes) {
        String title = isAcoes ? "Relatório de Auditoria de Ações" : "Relatório de Auditoria de Acesso";
        String[] headers = isAcoes ? new String[]{"Data/Hora", "Utilizador", "Ação", "Entidade", "Detalhes"} : new String[]{"Data/Hora", "Utilizador", "Resultado", "Entidade", "Detalhes"};
        List<Vector<Object>> data = isAcoes ? relatorioService.getAuditoriaAcoes() : relatorioService.getAuditoriaAcesso();
        try {
            ByteArrayOutputStream baos = relatorioService.gerarRelatorioAuditoriaPdf(title, headers, data);
            String fileNameBase = isAcoes ? "Relatorio_Auditoria_Acoes" : "Relatorio_Auditoria_Acesso";
            salvarRelatorioPdf(fileNameBase, baos);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao gerar o relatório PDF.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarRelatorioCsv(String nomeBase, String conteudo) {
        JFileChooser fileChooser = createFileChooser(nomeBase, "csv", "Arquivo CSV (*.csv)");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = getSelectedFileWithExtension(fileChooser, ".csv");
            try (PrintWriter out = new PrintWriter(arquivo, StandardCharsets.UTF_8)) {
                out.print(conteudo);
                JOptionPane.showMessageDialog(this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar o arquivo.", "Erro", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Erro ao salvar o arquivo.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
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