package com.titanaxis.view.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public abstract class BaseCrudPanel<T> extends JPanel {

    protected final DefaultTableModel tableModel;
    protected final JTable table;
    protected final JTextField searchField;
    protected final JPanel formPanel;

    public BaseCrudPanel() {
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL NORTE (Formulário e Botões) ---
        JPanel northPanel = new JPanel(new BorderLayout());
        this.formPanel = createFormPanel();
        northPanel.add(formPanel, BorderLayout.CENTER);
        northPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // --- PAINEL CENTRAL (Tabela e Busca) ---
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        this.searchField = new JTextField(25);
        centerPanel.add(createSearchPanel(searchField), BorderLayout.NORTH);

        this.tableModel = new DefaultTableModel(getColumnNames(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(tableModel);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // CORREÇÃO: Os listeners e o carregamento de dados serão chamados pelas subclasses
        // setupListeners();
        // loadData();
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addButton = new JButton("Adicionar");
        JButton updateButton = new JButton("Atualizar");
        JButton deleteButton = new JButton("Eliminar");
        JButton clearButton = new JButton("Limpar Campos");

        addButton.addActionListener(e -> onSave());
        updateButton.addActionListener(e -> onSave());
        deleteButton.addActionListener(e -> onDelete());
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        return buttonPanel;
    }

    private JPanel createSearchPanel(JTextField searchField) {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("Buscar");
        JButton clearSearchButton = new JButton("Limpar Busca");

        searchPanel.add(new JLabel("Buscar por Nome:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);

        searchButton.addActionListener(e -> onSearch());
        searchField.addActionListener(e -> onSearch());
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            loadData();
        });
        return searchPanel;
    }

    // O método foi tornado público para que as subclasses possam chamá-lo.
    protected void setupListeners() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedItem();
            }
        });
    }

    // --- MÉTODOS ABSTRATOS ---

    protected abstract JPanel createFormPanel();
    protected abstract String[] getColumnNames();
    protected abstract void populateTable(List<T> items);
    protected abstract void displaySelectedItem();
    protected abstract void clearFields();
    protected abstract void loadData();
    protected abstract void onSearch();
    protected abstract void onSave();
    protected abstract void onDelete();
}