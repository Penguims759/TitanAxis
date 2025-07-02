package com.titanaxis.view.panels;

import com.titanaxis.model.Categoria;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.CategoriaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoriaPanel extends JPanel {
    // ALTERAÇÃO: Injeção de dependências do AuthService e introdução do CategoriaService
    private final AuthService authService;
    private final CategoriaService categoriaService;
    private final DefaultTableModel tableModel;
    private final JTable categoriaTable;
    private final JTextField idField, nomeField;

    public CategoriaPanel(AuthService authService) {
        this.authService = authService;
        this.categoriaService = new CategoriaService(); // Serviço encapsula o repositório
        setLayout(new BorderLayout(10, 10));

        // --- Painel Norte: Formulário e Botões ---
        JPanel northPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalhes da Categoria"));
        idField = new JTextField();
        idField.setEditable(false);
        nomeField = new JTextField();
        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Nome:"));
        formPanel.add(nomeField);

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
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(25);
        JButton searchButton = new JButton("Buscar");
        JButton clearSearchButton = new JButton("Limpar Busca");
        searchPanel.add(new JLabel("Buscar por Nome:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nome da Categoria", "Nº de Produtos"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        categoriaTable = new JTable(tableModel);
        centerPanel.add(new JScrollPane(categoriaTable), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- Lógica dos Listeners (Ações) ---
        categoriaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedCategoria();
            }
        });

        addButton.addActionListener(e -> addOrUpdateCategoria());
        updateButton.addActionListener(e -> addOrUpdateCategoria());
        deleteButton.addActionListener(e -> deleteCategoria());
        clearButton.addActionListener(e -> clearFields());
        searchButton.addActionListener(e -> performSearch(searchField.getText()));
        searchField.addActionListener(e -> performSearch(searchField.getText()));
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            loadCategorias();
        });

        loadCategorias();
    }

    private void popularTabela(List<Categoria> categorias) {
        tableModel.setRowCount(0);
        categorias.forEach(categoria -> tableModel.addRow(new Object[]{
                categoria.getId(), categoria.getNome(), categoria.getTotalProdutos()
        }));
    }

    private void performSearch(String searchTerm) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // ALTERAÇÃO: Usa o serviço
            popularTabela(categoriaService.buscarCategoriasPorNome(searchTerm));
        } else {
            loadCategorias();
        }
    }

    private void loadCategorias() {
        // ALTERAÇÃO: Usa o serviço
        popularTabela(categoriaService.listarTodasCategorias());
    }

    private void displaySelectedCategoria() {
        int selectedRow = categoriaTable.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            nomeField.setText(tableModel.getValueAt(selectedRow, 1).toString());
        }
    }

    private void addOrUpdateCategoria() {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome da categoria é obrigatório.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isUpdate = !idField.getText().isEmpty();
        int id = isUpdate ? Integer.parseInt(idField.getText()) : 0;
        Categoria categoria = new Categoria(id, nome);

        try {
            // ALTERAÇÃO: Usa o CategoriaService, passando a informação do utilizador logado
            categoriaService.salvar(categoria, authService.getUsuarioLogado().orElse(null));
            JOptionPane.showMessageDialog(this, "Categoria " + (isUpdate ? "atualizada" : "adicionada") + " com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadCategorias();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar categoria: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCategoria() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idField.getText());
        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja eliminar esta categoria?\n(Os produtos nesta categoria ficarão sem categoria definida)",
                "Confirmar Eliminação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // ALTERAÇÃO: Usa o CategoriaService, passando o utilizador logado
            categoriaService.deletar(id, authService.getUsuarioLogado().orElse(null));
            JOptionPane.showMessageDialog(this, "Categoria eliminada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadCategorias();
            clearFields();
        }
    }

    private void clearFields() {
        idField.setText("");
        nomeField.setText("");
        categoriaTable.clearSelection();
    }
}