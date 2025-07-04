package com.titanaxis.view.dialogs;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.CategoriaService;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.util.AppLogger;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.logging.Level;

public class ProdutoDialog extends JDialog {
    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final Produto produto;
    private final Usuario ator;
    private boolean saved = false;
    private JTextField nomeField, descricaoField, precoField;
    private JComboBox<Categoria> categoriaComboBox;

    public ProdutoDialog(Frame owner, ProdutoService ps, CategoriaService cs, Produto p, Usuario ator) {
        super(owner, "Detalhes do Produto", true);
        this.produtoService = ps;
        this.categoriaService = cs;
        this.produto = (p != null) ? p : new Produto("", "", 0.0, null);
        this.ator = ator;

        setTitle(p == null || p.getId() == 0 ? "Novo Produto" : "Editar Produto");
        setLayout(new BorderLayout());
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

        nomeField = new JTextField(20);
        descricaoField = new JTextField();
        precoField = new JTextField();
        categoriaComboBox = new JComboBox<>();
        try {
            populateCategoryComboBox();
        } catch (PersistenciaException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar categorias: " + e.getMessage(), "Erro de Base de Dados", JOptionPane.ERROR_MESSAGE);
        }

        formPanel.add(new JLabel("Nome:"));
        formPanel.add(nomeField);
        formPanel.add(new JLabel("Descrição:"));
        formPanel.add(descricaoField);
        formPanel.add(new JLabel("Preço:"));
        formPanel.add(precoField);
        formPanel.add(new JLabel("Categoria:"));
        formPanel.add(categoriaComboBox);
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

    private void populateFields() {
        if (produto.getId() != 0) {
            nomeField.setText(produto.getNome());
            descricaoField.setText(produto.getDescricao());
            precoField.setText(String.format(Locale.US, "%.2f", produto.getPreco()));
            if (produto.getCategoria() != null) {
                categoriaComboBox.setSelectedItem(produto.getCategoria());
            }
        }
    }

    private void save() {
        if (nomeField.getText().trim().isEmpty() || categoriaComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Nome e Categoria são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            produto.setNome(nomeField.getText().trim());
            produto.setDescricao(descricaoField.getText().trim());
            produto.setPreco(Double.parseDouble(precoField.getText().replace(",", ".")));
            produto.setCategoria((Categoria) categoriaComboBox.getSelectedItem());

            if (produto.getId() == 0) {
                produto.setAtivo(true);
            }
            produtoService.salvarProduto(produto, ator);
            saved = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Preço inválido. Use ponto como separador decimal.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (UtilizadorNaoAutenticadoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Autenticação", JOptionPane.ERROR_MESSAGE);
        } catch (PersistenciaException e) {
            JOptionPane.showMessageDialog(this, "Erro de Base de Dados ao salvar: " + e.getMessage(), "Erro de Persistência", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            AppLogger.getLogger().log(Level.SEVERE, "Erro inesperado ao salvar o produto.", e);
            JOptionPane.showMessageDialog(this, "Erro ao salvar produto: " + e.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
        }
    }
}