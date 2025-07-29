// src/main/java/com/titanaxis/view/dialogs/ProdutoDialog.java
package com.titanaxis.view.dialogs;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Fornecedor;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.CategoriaService;
import com.titanaxis.service.FornecedorService;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.Objects;

public class ProdutoDialog extends JDialog {
    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final FornecedorService fornecedorService;
    private final Produto produto;
    private final Usuario ator;
    private boolean saved = false;

    private final JTextField nomeField;
    private final JTextField descricaoField;
    private final JTextField precoField;
    private final JComboBox<Categoria> categoriaComboBox;
    private final JComboBox<Fornecedor> fornecedorComboBox;

    public ProdutoDialog(Frame owner, ProdutoService ps, CategoriaService cs, FornecedorService fs, Produto p, Usuario ator) {
        super(owner, I18n.getString("productDialog.title"), true); 
        this.produtoService = ps;
        this.categoriaService = cs;
        this.fornecedorService = fs;
        this.produto = (p != null) ? p : new Produto();
        this.ator = ator;

        setTitle(this.produto.getId() == 0 ? I18n.getString("productDialog.title.new") : I18n.getString("productDialog.title.edit")); 
        setLayout(new BorderLayout());

        nomeField = new JTextField(20);
        descricaoField = new JTextField();
        precoField = new JTextField();
        categoriaComboBox = new JComboBox<>();
        fornecedorComboBox = new JComboBox<>();

        initComponents();
        populateFields();
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            populateCategoryComboBox();
            populateFornecedorComboBox();
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("productDialog.error.loadSupportData", e.getMessage()), I18n.getString("error.db.title")); 
        }

        formPanel.add(new JLabel(I18n.getString("productDialog.label.name"))); 
        formPanel.add(nomeField);
        formPanel.add(new JLabel(I18n.getString("productDialog.label.description"))); 
        formPanel.add(descricaoField);
        formPanel.add(new JLabel(I18n.getString("productDialog.label.price"))); 
        formPanel.add(precoField);
        formPanel.add(new JLabel(I18n.getString("productDialog.label.category"))); 
        formPanel.add(categoriaComboBox);
        formPanel.add(new JLabel(I18n.getString("productDialog.label.supplier"))); 
        formPanel.add(fornecedorComboBox);
        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(I18n.getString("button.save")); 
        saveButton.addActionListener(e -> save());
        JButton cancelButton = new JButton(I18n.getString("button.cancel")); 
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateCategoryComboBox() throws PersistenciaException {
        categoriaService.listarTodasCategorias().forEach(categoriaComboBox::addItem);
    }

    private void populateFornecedorComboBox() throws PersistenciaException {
        fornecedorComboBox.addItem(null); // Permite não selecionar nenhum fornecedor
        fornecedorService.listarTodos().forEach(fornecedorComboBox::addItem);
    }

    private void populateFields() {
        if (produto.getId() != 0) {
            nomeField.setText(produto.getNome());
            descricaoField.setText(produto.getDescricao());
            precoField.setText(String.format(Locale.US, "%.2f", produto.getPreco()));
            if (produto.getCategoria() != null) {
                categoriaComboBox.setSelectedItem(produto.getCategoria());
            }
            if (produto.getFornecedor() != null) {
                fornecedorComboBox.setSelectedItem(produto.getFornecedor());
            }
        }
    }

    private void save() {
        if (nomeField.getText().trim().isEmpty() || precoField.getText().trim().isEmpty() || categoriaComboBox.getSelectedItem() == null) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("productDialog.error.requiredFields"), I18n.getString("error.validation.title")); 
            return;
        }
        try {
            produto.setNome(nomeField.getText().trim());
            produto.setDescricao(descricaoField.getText().trim());
            produto.setPreco(Double.parseDouble(precoField.getText().replace(",", ".")));
            produto.setCategoria((Categoria) categoriaComboBox.getSelectedItem());
            produto.setFornecedor((Fornecedor) fornecedorComboBox.getSelectedItem());

            if (produto.getId() == 0) produto.setAtivo(true);

            produtoService.salvarProduto(produto, ator);
            saved = true;
            dispose();
        } catch (NumberFormatException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("productDialog.error.invalidPrice"), I18n.getString("error.format.title")); 
        } catch (UtilizadorNaoAutenticadoException e) {
            UIMessageUtil.showErrorMessage(this, e.getMessage(), I18n.getString("error.auth.title")); 
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("productDialog.error.save", e.getMessage()), I18n.getString("error.persistence.title")); 
        } catch (Exception e) {
            AppLogger.getLogger().error("Erro inesperado ao salvar o produto.", e);
            UIMessageUtil.showErrorMessage(this, I18n.getString("productDialog.error.unexpected", e.getMessage()), I18n.getString("error.unexpected.title")); 
        }
    }
}