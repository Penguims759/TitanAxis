// File: penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/panels/CategoriaPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Categoria;
import com.titanaxis.presenter.CategoriaPresenter;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoriaPanel extends JPanel implements CategoriaView {

    private CategoriaViewListener listener;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField;
    private final JTextField nomeField;
    private final JTextField searchField;

    public CategoriaPanel(AppContext appContext) {
        idField = new JTextField();
        idField.setEditable(false);
        nomeField = new JTextField();
        searchField = new JTextField(25);

        // ALTERADO
        tableModel = new DefaultTableModel(new String[]{
                I18n.getString("category.table.header.id"),
                I18n.getString("category.table.header.name"),
                I18n.getString("category.table.header.productCount")
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);

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
        centerPanel.add(createSearchPanel(searchField), BorderLayout.NORTH);
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
        panel.setBorder(BorderFactory.createTitledBorder(I18n.getString("category.border.details"))); // ALTERADO
        panel.add(new JLabel(I18n.getString("category.label.id"))); // ALTERADO
        panel.add(idField);
        panel.add(new JLabel(I18n.getString("category.label.name"))); // ALTERADO
        panel.add(nomeField);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton(I18n.getString("category.button.save")); // ALTERADO
        JButton deleteButton = new JButton(I18n.getString("category.button.delete")); // ALTERADO
        JButton clearButton = new JButton(I18n.getString("category.button.clear")); // ALTERADO

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
        JButton searchButton = new JButton(I18n.getString("category.button.search")); // ALTERADO
        JButton clearSearchButton = new JButton(I18n.getString("category.button.clearSearch")); // ALTERADO

        searchPanel.add(new JLabel(I18n.getString("category.label.searchByName"))); // ALTERADO
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
        table.clearSelection();
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

    @Override
    public String getTermoBusca() { return searchField.getText(); }

    @Override
    public void setTermoBusca(String termo) { searchField.setText(termo); }

    @Override
    public void clearTableSelection() { table.clearSelection(); }

    public void refreshData() {
        if (listener != null) {
            listener.aoCarregarDadosIniciais();
            table.clearSelection();
        }
    }

    @Override
    public void setListener(CategoriaViewListener listener) {
        this.listener = listener;
    }
}