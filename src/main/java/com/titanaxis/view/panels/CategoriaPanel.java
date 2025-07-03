package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Categoria;
import com.titanaxis.presenter.CategoriaPresenter;
import com.titanaxis.view.interfaces.CategoriaView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoriaPanel extends JPanel implements CategoriaView {

    private CategoriaViewListener listener;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField idField, nomeField, searchField;

    public CategoriaPanel(AppContext appContext) {
        initComponents();
        new CategoriaPresenter(this, appContext.getCategoriaService(), appContext.getAuthService());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createFormPanel(), BorderLayout.CENTER);
        northPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField(25);
        centerPanel.add(createSearchPanel(searchField), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome da Categoria", "Nº de Produtos"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                int id = (int) tableModel.getValueAt(modelRow, 0);
                String nome = (String) tableModel.getValueAt(modelRow, 1);
                int totalProdutos = (int) tableModel.getValueAt(modelRow, 2);
                listener.aoSelecionarCategoria(new Categoria(id, nome, totalProdutos));
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Detalhes da Categoria"));
        idField = new JTextField();
        idField.setEditable(false);
        nomeField = new JTextField();
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton("Salvar");
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

    private JPanel createSearchPanel(JTextField searchField) {
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

    @Override
    public String getId() { return idField.getText(); }

    @Override
    public String getNome() { return nomeField.getText(); }

    @Override
    public void setId(String id) { idField.setText(id); }

    @Override
    public void setNome(String nome) { nomeField.setText(nome); }

    @Override
    public void setCategoriasNaTabela(List<Categoria> categorias) {
        tableModel.setRowCount(0);
        categorias.forEach(c -> tableModel.addRow(new Object[]{c.getId(), c.getNome(), c.getTotalProdutos()}));
    }

    @Override
    public void mostrarMensagem(String titulo, String mensagem, boolean isErro) {
        JOptionPane.showMessageDialog(this, mensagem, titulo, isErro ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public boolean mostrarConfirmacao(String titulo, String mensagem) {
        return JOptionPane.showConfirmDialog(this, mensagem, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public String getTermoBusca() { return searchField.getText(); }

    @Override
    public void setTermoBusca(String termo) { searchField.setText(termo); }

    @Override
    public void clearTableSelection() { table.clearSelection(); }

    @Override
    public void setListener(CategoriaViewListener listener) {
        this.listener = listener;
    }
}