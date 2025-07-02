package com.titanaxis.view.panels;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ClienteService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientePanel extends JPanel {
    // ALTERAÇÃO: Introdução dos serviços
    private final ClienteService clienteService;
    private final AuthService authService;

    private final DefaultTableModel tableModel;
    private final JTable clienteTable;
    private final JTextField idField, nomeField, contatoField, enderecoField;

    public ClientePanel(AuthService authService) { // Construtor atualizado
        this.authService = authService;
        this.clienteService = new ClienteService();
        setLayout(new BorderLayout(10, 10));

        // --- Painel Norte: Formulário e Botões ---
        JPanel northPanel = new JPanel(new BorderLayout());

        // Formulário
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalhes do Cliente"));
        idField = new JTextField();
        idField.setEditable(false);
        nomeField = new JTextField();
        contatoField = new JTextField();
        enderecoField = new JTextField();
        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Nome:"));
        formPanel.add(nomeField);
        formPanel.add(new JLabel("Contato (E-mail/Telefone):"));
        formPanel.add(contatoField);
        formPanel.add(new JLabel("Endereço:"));
        formPanel.add(enderecoField);

        // Painel de botões do formulário
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addButton = new JButton("Adicionar");
        JButton updateButton = new JButton("Atualizar");
        JButton deleteButton = new JButton("Eliminar");
        JButton clearButton = new JButton("Limpar Campos");
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        northPanel.add(formPanel, BorderLayout.CENTER);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // --- Painel Central: Tabela e Busca ---
        JPanel centerPanel = new JPanel(new BorderLayout(5,5));

        // Busca
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(25);
        JButton searchButton = new JButton("Buscar");
        JButton clearSearchButton = new JButton("Limpar Busca");
        searchPanel.add(new JLabel("Buscar por Nome:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Tabela
        String[] columnNames = {"ID", "Nome", "Contato", "Endereço"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        clienteTable = new JTable(tableModel);
        centerPanel.add(new JScrollPane(clienteTable), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- Lógica dos Listeners (Ações) ---
        clienteTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedCliente();
            }
        });

        addButton.addActionListener(e -> addOrUpdateCliente());
        updateButton.addActionListener(e -> addOrUpdateCliente());
        deleteButton.addActionListener(e -> deleteCliente());
        clearButton.addActionListener(e -> clearFields());
        searchButton.addActionListener(e -> performSearch(searchField.getText()));
        searchField.addActionListener(e -> performSearch(searchField.getText()));
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            loadClientes();
        });

        loadClientes();
    }

    private void popularTabela(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        clientes.forEach(cliente -> tableModel.addRow(new Object[]{
                cliente.getId(), cliente.getNome(), cliente.getContato(), cliente.getEndereco()
        }));
    }

    private void performSearch(String searchTerm) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            popularTabela(clienteService.buscarPorNome(searchTerm));
        } else {
            loadClientes();
        }
    }

    private void loadClientes() {
        popularTabela(clienteService.listarTodos());
    }

    private void displaySelectedCliente() {
        int selectedRow = clienteTable.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            nomeField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            contatoField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            enderecoField.setText(tableModel.getValueAt(selectedRow, 3).toString());
        }
    }

    private void addOrUpdateCliente() {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome do cliente é obrigatório.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isUpdate = !idField.getText().isEmpty();
        int id = isUpdate ? Integer.parseInt(idField.getText()) : 0;
        String contato = contatoField.getText().trim();
        String endereco = enderecoField.getText().trim();

        Cliente cliente = new Cliente(id, nome, contato, endereco);
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        try {
            clienteService.salvar(cliente, ator);
            JOptionPane.showMessageDialog(this, "Cliente " + (isUpdate ? "atualizado" : "adicionado") + " com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadClientes();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCliente() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idField.getText());
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja eliminar este cliente?", "Confirmar Eliminação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Usuario ator = authService.getUsuarioLogado().orElse(null);
            try {
                clienteService.deletar(id, ator);
                JOptionPane.showMessageDialog(this, "Cliente eliminado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                loadClientes();
                clearFields();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao eliminar cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        idField.setText("");
        nomeField.setText("");
        contatoField.setText("");
        enderecoField.setText("");
        clienteTable.clearSelection();
    }
}