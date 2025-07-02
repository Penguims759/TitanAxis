package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.CategoriaService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CategoriaPanel extends BaseCrudPanel<Categoria> {

    private final CategoriaService categoriaService;
    private final AuthService authService;

    private JTextField idField, nomeField;

    public CategoriaPanel(AppContext appContext) {
        super();
        this.authService = appContext.getAuthService();
        this.categoriaService = appContext.getCategoriaService();

        setupListeners();
        loadData();
    }

    @Override
    protected JPanel createFormPanel() {
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

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Nome da Categoria", "Nº de Produtos"};
    }

    @Override
    protected void populateTable(List<Categoria> categorias) {
        tableModel.setRowCount(0);
        categorias.forEach(categoria -> tableModel.addRow(new Object[]{
                categoria.getId(), categoria.getNome(), categoria.getTotalProdutos()
        }));
    }

    @Override
    protected void displaySelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            nomeField.setText(tableModel.getValueAt(selectedRow, 1).toString());
        }
    }

    @Override
    protected void clearFields() {
        idField.setText("");
        nomeField.setText("");
        table.clearSelection();
    }

    @Override
    protected void loadData() {
        populateTable(categoriaService.listarTodasCategorias());
    }

    @Override
    protected void onSearch() {
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            populateTable(categoriaService.buscarCategoriasPorNome(searchTerm));
        } else {
            loadData();
        }
    }

    @Override
    protected void onSave() {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome da categoria é obrigatório.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isUpdate = !idField.getText().isEmpty();
        int id = isUpdate ? Integer.parseInt(idField.getText()) : 0;
        Categoria categoria = new Categoria(id, nome);
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        try {
            categoriaService.salvar(categoria, ator);
            JOptionPane.showMessageDialog(this, "Categoria " + (isUpdate ? "atualizada" : "adicionada") + " com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar categoria: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onDelete() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idField.getText());
        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja eliminar esta categoria?\n(Os produtos nesta categoria ficarão sem categoria definida)",
                "Confirmar Eliminação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Usuario ator = authService.getUsuarioLogado().orElse(null);
            try {
                categoriaService.deletar(id, ator);
                JOptionPane.showMessageDialog(this, "Categoria eliminada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                clearFields();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao eliminar categoria: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}