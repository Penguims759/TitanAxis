// src/main/java/com/titanaxis/view/panels/FornecedorPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Fornecedor;
import com.titanaxis.presenter.FornecedorPresenter;
import com.titanaxis.service.FornecedorService;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.interfaces.FornecedorView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class FornecedorPanel extends JPanel implements FornecedorView, DashboardFrame.Refreshable {

    private FornecedorViewListener listener;
    private final AppContext appContext;
    private final FornecedorService fornecedorService;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField, nomeField, cnpjField, contatoNomeField, contatoTelefoneField, contatoEmailField, enderecoField;
    private final JTextField searchField;

    public FornecedorPanel(AppContext appContext) {
        this.appContext = appContext;
        this.fornecedorService = appContext.getFornecedorService();

        setLayout(new BorderLayout(10, 10));

        // Form fields
        idField = new JTextField();
        idField.setEditable(false);
        nomeField = new JTextField();
        cnpjField = new JTextField();
        contatoNomeField = new JTextField();
        contatoTelefoneField = new JTextField();
        contatoEmailField = new JTextField();
        enderecoField = new JTextField();
        searchField = new JTextField(25);

        // Table
        String[] columnNames = {"ID", "Nome", "CNPJ", "Contato", "Telefone"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        initComponents();
        new FornecedorPresenter(this, this.fornecedorService, this.appContext.getAuthService());
        listener.aoCarregarDados();
    }

    private void initComponents() {
        // Painel Norte com o formulário e botões
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createFormPanel(), BorderLayout.CENTER);
        northPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Painel Central com a busca e a tabela
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(createSearchPanel(), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                int fornecedorId = (int) tableModel.getValueAt(modelRow, 0);
                preencherCamposPelaTabela(fornecedorId);
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Detalhes do Fornecedor"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linha 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.2; panel.add(idField, gbc);
        gbc.gridx = 2; gbc.weightx = 0; panel.add(new JLabel("CNPJ:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.8; panel.add(cnpjField, gbc);

        // Linha 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(new JLabel("Nome*:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panel.add(nomeField, gbc);

        // Linha 2
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0; panel.add(new JLabel("Contato:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(contatoNomeField, gbc);
        gbc.gridx = 2; gbc.weightx = 0; panel.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; panel.add(contatoTelefoneField, gbc);

        // Linha 3
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panel.add(contatoEmailField, gbc);

        // Linha 4
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0; panel.add(new JLabel("Endereço:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panel.add(enderecoField, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton("Salvar");
        JButton deleteButton = new JButton("Eliminar");
        JButton clearButton = new JButton("Limpar Campos");

        saveButton.addActionListener(e -> salvar());
        deleteButton.addActionListener(e -> deletar());
        clearButton.addActionListener(e -> listener.aoLimpar());

        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        return buttonPanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("Buscar");
        JButton clearSearchButton = new JButton("Limpar Busca");

        searchPanel.add(new JLabel("Buscar por Nome:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        searchButton.addActionListener(e -> sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText())));
        searchField.addActionListener(e -> sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText())));
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            sorter.setRowFilter(null);
        });
        return searchPanel;
    }

    private void salvar() {
        if (nomeField.getText().trim().isEmpty()) {
            UIMessageUtil.showErrorMessage(this, "O nome do fornecedor é obrigatório.", "Erro de Validação");
            return;
        }
        Fornecedor f = new Fornecedor();
        if (!idField.getText().isEmpty()) f.setId(Integer.parseInt(idField.getText()));
        f.setNome(nomeField.getText().trim());
        f.setCnpj(cnpjField.getText().trim());
        f.setContatoNome(contatoNomeField.getText().trim());
        f.setContatoEmail(contatoEmailField.getText().trim());
        f.setContatoTelefone(contatoTelefoneField.getText().trim());
        f.setEndereco(enderecoField.getText().trim());
        listener.aoSalvar(f);
    }

    private void deletar() {
        if (idField.getText().isEmpty()) {
            UIMessageUtil.showErrorMessage(this, "Selecione um fornecedor para eliminar.", "Erro");
            return;
        }
        listener.aoApagar(Integer.parseInt(idField.getText()));
    }

    private void preencherCamposPelaTabela(int fornecedorId) {
        try {
            appContext.getFornecedorService().buscarPorId(fornecedorId)
                    .ifPresent(f -> {
                        idField.setText(String.valueOf(f.getId()));
                        nomeField.setText(f.getNome());
                        cnpjField.setText(f.getCnpj());
                        contatoNomeField.setText(f.getContatoNome());
                        contatoTelefoneField.setText(f.getContatoTelefone());
                        contatoEmailField.setText(f.getContatoEmail());
                        enderecoField.setText(f.getEndereco());
                    });
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao buscar detalhes do fornecedor: " + e.getMessage(), "Erro");
        }
    }

    @Override
    public void refreshData() {
        if(listener != null) listener.aoCarregarDados();
    }

    @Override
    public void setFornecedoresNaTabela(List<Fornecedor> fornecedores) {
        tableModel.setRowCount(0);
        fornecedores.forEach(f -> tableModel.addRow(new Object[]{f.getId(), f.getNome(), f.getCnpj(), f.getContatoNome(), f.getContatoTelefone()}));
    }

    @Override
    public void mostrarMensagem(String titulo, String mensagem, boolean isErro) {
        if (isErro) UIMessageUtil.showErrorMessage(this, mensagem, titulo);
        else UIMessageUtil.showInfoMessage(this, mensagem, titulo);
    }

    @Override
    public boolean mostrarConfirmacao(String titulo, String mensagem) {
        return UIMessageUtil.showConfirmDialog(this, mensagem, titulo);
    }

    @Override
    public void limparCampos() {
        idField.setText("");
        nomeField.setText("");
        cnpjField.setText("");
        contatoNomeField.setText("");
        contatoTelefoneField.setText("");
        contatoEmailField.setText("");
        enderecoField.setText("");
        table.clearSelection();
    }

    @Override
    public void setListener(FornecedorViewListener listener) {
        this.listener = listener;
    }
}