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
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

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
        super(owner, "Detalhes do Produto", true);
        this.produtoService = ps;
        this.categoriaService = cs;
        this.fornecedorService = fs;
        this.produto = (p != null) ? p : new Produto();
        this.ator = ator;

        setTitle(this.produto.getId() == 0 ? "Novo Produto" : "Editar Produto");
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
            UIMessageUtil.showErrorMessage(this, "Erro ao carregar dados de suporte: " + e.getMessage(), "Erro de Base de Dados");
        }

        formPanel.add(new JLabel("Nome*:"));
        formPanel.add(nomeField);
        formPanel.add(new JLabel("Descrição:"));
        formPanel.add(descricaoField);
        formPanel.add(new JLabel("Preço*:"));
        formPanel.add(precoField);
        formPanel.add(new JLabel("Categoria*:"));
        formPanel.add(categoriaComboBox);
        formPanel.add(new JLabel("Fornecedor:"));
        formPanel.add(fornecedorComboBox);
        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Salvar");
        saveButton.addActionListener(e -> save());
        JButton cancelButton = new JButton("Cancelar");
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
            UIMessageUtil.showErrorMessage(this, "Os campos com * (Nome, Preço e Categoria) são obrigatórios.", "Erro de Validação");
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
            UIMessageUtil.showErrorMessage(this, "Preço inválido. Use ponto como separador decimal.", "Erro de Formato");
        } catch (UtilizadorNaoAutenticadoException e) {
            UIMessageUtil.showErrorMessage(this, e.getMessage(), "Erro de Autenticação");
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Erro de Base de Dados ao salvar: " + e.getMessage(), "Erro de Persistência");
        } catch (Exception e) {
            AppLogger.getLogger().log(Level.SEVERE, "Erro inesperado ao salvar o produto.", e);
            UIMessageUtil.showErrorMessage(this, "Erro ao salvar produto: " + e.getMessage(), "Erro Inesperado");
        }
    }
}