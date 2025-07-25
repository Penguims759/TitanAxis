package com.titanaxis.view.panels;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Fornecedor;
import com.titanaxis.util.I18n;
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
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField, nomeField, cnpjField, contatoNomeField, contatoTelefoneField, contatoEmailField, enderecoField;
    private final JTextField searchField;

    public FornecedorPanel() {
        setLayout(new BorderLayout(10, 10));

        idField = new JTextField();
        idField.setEditable(false);
        nomeField = new JTextField();
        cnpjField = new JTextField();
        contatoNomeField = new JTextField();
        contatoTelefoneField = new JTextField();
        contatoEmailField = new JTextField();
        enderecoField = new JTextField();
        searchField = new JTextField(25);

        String[] columnNames = {
                I18n.getString("supplier.table.header.id"),
                I18n.getString("supplier.table.header.name"),
                I18n.getString("supplier.table.header.cnpj"),
                I18n.getString("supplier.table.header.contact"),
                I18n.getString("supplier.table.header.phone")
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        initComponents();
    }

    private void initComponents() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createFormPanel(), BorderLayout.CENTER);
        northPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(createSearchPanel(), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                int fornecedorId = (int) tableModel.getValueAt(modelRow, 0);
                listener.aoSelecionarFornecedor(fornecedorId);
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(I18n.getString("supplier.border.details")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; panel.add(new JLabel(I18n.getString("supplier.label.id")), gbc);
        gbc.gridx = 1; gbc.weightx = 0.2; panel.add(idField, gbc);
        gbc.gridx = 2; gbc.weightx = 0; panel.add(new JLabel(I18n.getString("supplier.label.cnpj")), gbc);
        gbc.gridx = 3; gbc.weightx = 0.8; panel.add(cnpjField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(new JLabel(I18n.getString("supplier.label.name")), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panel.add(nomeField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0; panel.add(new JLabel(I18n.getString("supplier.label.contactName")), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(contatoNomeField, gbc);
        gbc.gridx = 2; gbc.weightx = 0; panel.add(new JLabel(I18n.getString("supplier.label.phone")), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; panel.add(contatoTelefoneField, gbc);

        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0; panel.add(new JLabel(I18n.getString("supplier.label.email")), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panel.add(contatoEmailField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0; panel.add(new JLabel(I18n.getString("supplier.label.address")), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panel.add(enderecoField, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton(I18n.getString("supplier.button.save"));
        JButton deleteButton = new JButton(I18n.getString("supplier.button.delete"));
        JButton clearButton = new JButton(I18n.getString("supplier.button.clear"));

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
        JButton searchButton = new JButton(I18n.getString("supplier.button.search"));
        JButton clearSearchButton = new JButton(I18n.getString("supplier.button.clearSearch"));

        searchPanel.add(new JLabel(I18n.getString("supplier.label.searchByName")));
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
            UIMessageUtil.showErrorMessage(this, I18n.getString("supplier.error.nameRequired"), I18n.getString("error.validation.title"));
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
            UIMessageUtil.showErrorMessage(this, I18n.getString("supplier.error.selectToDelete"), I18n.getString("error.title"));
            return;
        }
        listener.aoApagar(Integer.parseInt(idField.getText()));
    }

    public void preencherCamposPeloFornecedor(Fornecedor f) {
        idField.setText(String.valueOf(f.getId()));
        nomeField.setText(f.getNome());
        cnpjField.setText(f.getCnpj());
        contatoNomeField.setText(f.getContatoNome());
        contatoTelefoneField.setText(f.getContatoTelefone());
        contatoEmailField.setText(f.getContatoEmail());
        enderecoField.setText(f.getEndereco());
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