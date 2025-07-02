// src/main/java/com/titanaxis/view/panels/ProdutoPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.repository.impl.AuditoriaRepositoryImpl;
import com.titanaxis.repository.impl.CategoriaRepositoryImpl;
import com.titanaxis.repository.impl.ProdutoRepositoryImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ProdutoPanel extends JPanel {
    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AuditoriaRepository auditoriaRepository;
    private DefaultTableModel produtoTableModel;
    private JTable produtoTable;
    private DefaultTableModel loteTableModel;
    private JTable loteTable;
    private JButton editProdutoButton, addLoteButton, editLoteButton, deleteLoteButton, toggleStatusButton;
    private JToggleButton showInactiveButton;

    private Produto produtoSelecionado;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProdutoPanel() {
        this.produtoRepository = new ProdutoRepositoryImpl();
        this.categoriaRepository = new CategoriaRepositoryImpl();
        this.auditoriaRepository = new AuditoriaRepositoryImpl();
        setLayout(new BorderLayout(10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createProdutoListPanel(), createDetalhesPanel());
        splitPane.setResizeWeight(0.45);
        add(splitPane, BorderLayout.CENTER);

        loadProdutos();
        SwingUtilities.invokeLater(() -> produtoTable.clearSelection());
    }

    private JPanel createProdutoListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Produtos Cadastrados"));

        produtoTableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Categoria", "Qtd. Total", "Estado"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        produtoTable = new JTable(produtoTableModel);
        produtoTable.setRowSorter(new TableRowSorter<>(produtoTableModel));
        produtoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        produtoTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                String status = (String) table.getModel().getValueAt(modelRow, 4);
                if ("Inativo".equals(status)) {
                    c.setForeground(Color.GRAY);
                    c.setFont(new Font(c.getFont().getName(), Font.ITALIC, c.getFont().getSize()));
                } else {
                    c.setForeground(table.getForeground());
                    c.setFont(new Font(c.getFont().getName(), Font.PLAIN, c.getFont().getSize()));
                }
                return c;
            }
        });

        produtoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (produtoTable.getSelectedRow() != -1) {
                    int modelRow = produtoTable.convertRowIndexToModel(produtoTable.getSelectedRow());
                    int produtoId = (int) produtoTableModel.getValueAt(modelRow, 0);
                    produtoRepository.findById(produtoId).ifPresent(this::displayDetalhesProduto);
                } else {
                    clearDetalhesPanel();
                }
            }
        });
        panel.add(new JScrollPane(produtoTable), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton novoProdutoButton = new JButton("Novo Produto");
        novoProdutoButton.addActionListener(e -> showProdutoDialog(null));

        editProdutoButton = new JButton("Editar Produto");
        editProdutoButton.addActionListener(e -> {
            if (produtoSelecionado != null) showProdutoDialog(produtoSelecionado);
        });

        toggleStatusButton = new JButton("Inativar/Reativar");
        toggleStatusButton.addActionListener(e -> toggleProdutoStatus());

        showInactiveButton = new JToggleButton("Mostrar Inativos");
        showInactiveButton.addActionListener(e -> loadProdutos());

        buttonPanel.add(novoProdutoButton);
        buttonPanel.add(editProdutoButton);
        buttonPanel.add(toggleStatusButton);

        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(showInactiveButton, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetalhesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Estoque e Lotes do Produto"));

        loteTableModel = new DefaultTableModel(new String[]{"ID Lote", "Nº Lote", "Quantidade", "Data de Validade"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        loteTable = new JTable(loteTableModel);
        panel.add(new JScrollPane(loteTable), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addLoteButton = new JButton("Adicionar Lote");
        addLoteButton.addActionListener(e -> {
            if (produtoSelecionado != null) showLoteDialog(null);
        });

        editLoteButton = new JButton("Editar Lote");
        editLoteButton.addActionListener(e -> {
            int selectedRow = loteTable.getSelectedRow();
            if (selectedRow != -1) {
                int loteId = (int) loteTableModel.getValueAt(selectedRow, 0);
                produtoRepository.findLoteById(loteId).ifPresent(this::showLoteDialog);
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um lote para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteLoteButton = new JButton("Remover Lote");
        deleteLoteButton.addActionListener(e -> removerLote());

        buttonsPanel.add(addLoteButton);
        buttonsPanel.add(editLoteButton);
        buttonsPanel.add(deleteLoteButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        clearDetalhesPanel();

        return panel;
    }

    private void loadProdutos() {
        List<Produto> produtos = showInactiveButton.isSelected()
                ? produtoRepository.findAllIncludingInactive()
                : produtoRepository.findAll();

        produtoTableModel.setRowCount(0);
        for (Produto p : produtos) {
            produtoTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    p.getNomeCategoria(),
                    p.getQuantidadeTotal(),
                    p.isAtivo() ? "Ativo" : "Inativo"
            });
        }
    }

    private void displayDetalhesProduto(Produto produto) {
        this.produtoSelecionado = produto;
        editProdutoButton.setEnabled(true);
        addLoteButton.setEnabled(true);
        editLoteButton.setEnabled(true);
        deleteLoteButton.setEnabled(true);
        toggleStatusButton.setEnabled(true);
        toggleStatusButton.setText(produto.isAtivo() ? "Inativar Produto" : "Reativar Produto");

        List<Lote> lotes = produtoRepository.findLotesByProdutoId(produto.getId());
        loteTableModel.setRowCount(0);
        for (Lote lote : lotes) {
            loteTableModel.addRow(new Object[]{
                    lote.getId(),
                    lote.getNumeroLote(),
                    lote.getQuantidade(),
                    lote.getDataValidade() != null ? lote.getDataValidade().format(DATE_FORMATTER) : "N/A"
            });
        }
    }

    private void clearDetalhesPanel() {
        this.produtoSelecionado = null;
        loteTableModel.setRowCount(0);
        if(editProdutoButton != null) editProdutoButton.setEnabled(false);
        if(addLoteButton != null) addLoteButton.setEnabled(false);
        if(editLoteButton != null) editLoteButton.setEnabled(false);
        if(deleteLoteButton != null) deleteLoteButton.setEnabled(false);
        if(toggleStatusButton != null) toggleStatusButton.setEnabled(false);
    }

    private void toggleProdutoStatus() {
        if (produtoSelecionado == null) return;

        boolean novoStatus = !produtoSelecionado.isAtivo();
        String acao = novoStatus ? "reativado" : "inativado";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja " + acao + " o produto '" + produtoSelecionado.getNome() + "'?",
                "Confirmar Alteração de Estado", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean sucesso = produtoRepository.updateStatusAtivo(produtoSelecionado.getId(), novoStatus);
            if (sucesso) {
                String detalhes = String.format("Produto '%s' (ID: %d) foi %s.", produtoSelecionado.getNome(), produtoSelecionado.getId(), acao);
                auditoriaRepository.registrarAcao(1, "admin", novoStatus ? "REATIVAÇÃO" : "INATIVAÇÃO", "Produto", detalhes);
                loadProdutos();
                clearDetalhesPanel();
                produtoTable.clearSelection();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao alterar o estado do produto.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showProdutoDialog(Produto produto) {
        ProdutoDialog dialog = new ProdutoDialog((JFrame) SwingUtilities.getWindowAncestor(this), produtoRepository, categoriaRepository, produto);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadProdutos();
            produtoTable.clearSelection();
        }
    }

    private void showLoteDialog(Lote lote) {
        LoteDialog dialog = new LoteDialog((JFrame) SwingUtilities.getWindowAncestor(this), produtoRepository, produtoSelecionado, lote);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            produtoRepository.findById(produtoSelecionado.getId()).ifPresent(this::displayDetalhesProduto);
            loadProdutos();
        }
    }

    private void removerLote() {
        if (produtoSelecionado == null || loteTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um lote para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int loteId = (int) loteTableModel.getValueAt(loteTable.getSelectedRow(), 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja remover este lote?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            produtoRepository.deleteLoteById(loteId);
            produtoRepository.findById(produtoSelecionado.getId()).ifPresent(this::displayDetalhesProduto);
            loadProdutos();
        }
    }
}

class ProdutoDialog extends JDialog {
    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final Produto produto;
    private boolean saved = false;

    private JTextField nomeField, descricaoField, precoField;
    private JComboBox<Categoria> categoriaComboBox;

    public ProdutoDialog(Frame owner, ProdutoRepository pr, CategoriaRepository cr, Produto p) {
        super(owner, "Detalhes do Produto", true);
        this.produtoRepository = pr;
        this.categoriaRepository = cr;
        this.produto = (p != null) ? p : new Produto("", "", 0.0, 0);

        setTitle(p == null || p.getId() == 0 ? "Novo Produto" : "Editar Produto");
        setLayout(new BorderLayout());

        initComponents();
        populateFields();

        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        nomeField = new JTextField(20);
        descricaoField = new JTextField();
        precoField = new JTextField();
        categoriaComboBox = new JComboBox<>();
        populateCategoryComboBox();

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

    private void populateCategoryComboBox() {
        categoriaRepository.findAll().forEach(categoriaComboBox::addItem);
    }

    private void populateFields() {
        if (produto.getId() != 0) {
            nomeField.setText(produto.getNome());
            descricaoField.setText(produto.getDescricao());
            precoField.setText(String.format(Locale.US, "%.2f", produto.getPreco()));
            for (int i = 0; i < categoriaComboBox.getItemCount(); i++) {
                if (categoriaComboBox.getItemAt(i).getId() == produto.getCategoriaId()) {
                    categoriaComboBox.setSelectedIndex(i);
                    break;
                }
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
            produto.setCategoriaId(((Categoria)categoriaComboBox.getSelectedItem()).getId());

            produtoRepository.save(produto);
            saved = true;
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Preço inválido. Use ponto como separador decimal.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}

class LoteDialog extends JDialog {
    private final ProdutoRepository produtoRepository;
    private final Lote lote;
    private boolean saved = false;

    private JTextField numeroLoteField, quantidadeField;
    private JFormattedTextField dataValidadeField;

    public LoteDialog(Frame owner, ProdutoRepository pr, Produto produtoPai, Lote l) {
        super(owner, "Detalhes do Lote", true);
        this.produtoRepository = pr;
        this.lote = (l != null) ? l : new Lote(produtoPai.getId(), "", 0, null);

        setTitle(l == null || l.getId() == 0 ? "Novo Lote para " + produtoPai.getNome() : "Editar Lote");
        setLayout(new BorderLayout());

        initComponents();
        populateFields();

        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        numeroLoteField = new JTextField(20);
        quantidadeField = new JTextField();
        dataValidadeField = new JFormattedTextField();
        try {
            MaskFormatter dateFormatter = new MaskFormatter("##/##/####");
            dateFormatter.setPlaceholderCharacter('_');
            dateFormatter.install(dataValidadeField);
        } catch (ParseException e) {}

        formPanel.add(new JLabel("Número do Lote:"));
        formPanel.add(numeroLoteField);
        formPanel.add(new JLabel("Quantidade:"));
        formPanel.add(quantidadeField);
        formPanel.add(new JLabel("Data de Validade:"));
        formPanel.add(dataValidadeField);

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

    private void populateFields() {
        if (lote.getId() != 0) {
            numeroLoteField.setText(lote.getNumeroLote());
            quantidadeField.setText(String.valueOf(lote.getQuantidade()));
            if (lote.getDataValidade() != null) {
                dataValidadeField.setText(lote.getDataValidade().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }
    }

    private void save() {
        try {
            lote.setNumeroLote(numeroLoteField.getText().trim());
            lote.setQuantidade(Integer.parseInt(quantidadeField.getText().trim()));

            String dataTexto = dataValidadeField.getText().replace("_", "").trim();
            if (!dataTexto.isEmpty()) {
                lote.setDataValidade(LocalDate.parse(dataTexto, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                lote.setDataValidade(null);
            }

            if(lote.getNumeroLote().isEmpty() || lote.getQuantidade() <= 0) {
                JOptionPane.showMessageDialog(this, "Número do lote e quantidade positiva são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            produtoRepository.saveLote(lote);
            saved = true;
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Dados inválidos. Verifique os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}