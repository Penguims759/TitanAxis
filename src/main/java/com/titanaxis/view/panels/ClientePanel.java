package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Cliente;
import com.titanaxis.presenter.ClientePresenter;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.interfaces.ClienteView;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientePanel extends JPanel implements ClienteView {

    private ClienteViewListener listener;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField;
    private final JTextField nomeField;
    private final JTextField contatoField;
    private final JTextField enderecoField;
    private final JTextField searchField;
    private JButton saveButton; // ALTERADO: Removido 'final' para ser acessível nos métodos

    public ClientePanel(AppContext appContext) {
        searchField = new JTextField(25);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Contato", "Endereço"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);

        idField = new JTextField();
        idField.setEditable(false);
        nomeField = new JTextField();
        contatoField = new JTextField();
        enderecoField = new JTextField();

        initComponents();
        addFormValidation(); // ADICIONADO: Chama o método de validação
        new ClientePresenter(this, appContext.getClienteService(), appContext.getAuthService());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

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
                int id = (int) tableModel.getValueAt(modelRow, 0);
                String nome = (String) tableModel.getValueAt(modelRow, 1);
                String contato = (String) tableModel.getValueAt(modelRow, 2);
                String endereco = (String) tableModel.getValueAt(modelRow, 3);
                listener.aoSelecionarCliente(new Cliente(id, nome, contato, endereco));
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Detalhes do Cliente"));
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        panel.add(new JLabel("Contato (E-mail/Telefone):"));
        panel.add(contatoField);
        panel.add(new JLabel("Endereço:"));
        panel.add(enderecoField);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        saveButton = new JButton("Salvar"); // ALTERADO: Atribuição ao campo da classe
        JButton deleteButton = new JButton("Eliminar");
        JButton clearButton = new JButton("Limpar Campos");

        saveButton.addActionListener(e -> listener.aoSalvar());
        deleteButton.addActionListener(e -> listener.aoApagar());
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

        searchButton.addActionListener(e -> listener.aoBuscar());
        searchField.addActionListener(e -> listener.aoBuscar());
        clearSearchButton.addActionListener(e -> listener.aoLimparBusca());
        return searchPanel;
    }

    // NOVO MÉTODO: Adiciona validação em tempo real ao formulário
    private void addFormValidation() {
        DocumentListener validationListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateForm(); }
            public void removeUpdate(DocumentEvent e) { validateForm(); }
            public void insertUpdate(DocumentEvent e) { validateForm(); }
        };

        nomeField.getDocument().addDocumentListener(validationListener);
        validateForm(); // Define o estado inicial do botão
    }

    // NOVO MÉTODO: Lógica de validação
    private void validateForm() {
        boolean isNomeValid = !nomeField.getText().trim().isEmpty();
        saveButton.setEnabled(isNomeValid);
    }

    // --- Implementação dos métodos da interface ClienteView ---
    @Override public String getId() { return idField.getText(); }
    @Override public String getNome() { return nomeField.getText(); }
    @Override public String getContato() { return contatoField.getText(); }
    @Override public String getEndereco() { return enderecoField.getText(); }
    @Override public void setId(String id) { idField.setText(id); }
    @Override public void setNome(String nome) { nomeField.setText(nome); }
    @Override public void setContato(String contato) { contatoField.setText(contato); }
    @Override public void setEndereco(String endereco) { enderecoField.setText(endereco); }
    @Override public String getTermoBusca() { return searchField.getText(); }
    @Override public void setTermoBusca(String termo) { searchField.setText(termo); }

    @Override
    public void setClientesNaTabela(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        clientes.forEach(c -> tableModel.addRow(new Object[]{c.getId(), c.getNome(), c.getContato(), c.getEndereco()}));
        table.clearSelection();
    }

    @Override
    public void adicionarClienteNaTabela(Cliente cliente) {
        tableModel.addRow(new Object[]{cliente.getId(), cliente.getNome(), cliente.getContato(), cliente.getEndereco()});
    }

    @Override
    public void atualizarClienteNaTabela(Cliente cliente) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(cliente.getId())) {
                tableModel.setValueAt(cliente.getNome(), i, 1);
                tableModel.setValueAt(cliente.getContato(), i, 2);
                tableModel.setValueAt(cliente.getEndereco(), i, 3);
                return;
            }
        }
    }

    @Override
    public void removerClienteDaTabela(int clienteId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(clienteId)) {
                tableModel.removeRow(i);
                return;
            }
        }
    }

    @Override
    public void mostrarMensagem(String titulo, String mensagem, boolean isErro) {
        if (isErro) {
            UIMessageUtil.showErrorMessage(this, mensagem, titulo);
        } else {
            UIMessageUtil.showInfoMessage(this, mensagem, titulo);
        }
    }

    @Override
    public boolean mostrarConfirmacao(String titulo, String mensagem) {
        return UIMessageUtil.showConfirmDialog(this, mensagem, titulo);
    }

    public void refreshData() {
        if (listener != null) {
            listener.aoCarregarDadosIniciais();
            table.clearSelection();
        }
    }

    @Override
    public void setListener(ClienteViewListener listener) {
        this.listener = listener;
    }

    @Override
    public void clearTableSelection() {
        table.clearSelection();
    }
}