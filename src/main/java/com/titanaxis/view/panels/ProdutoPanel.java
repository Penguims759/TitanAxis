// FICHEIRO ALTERADO: src/main/java/com/titanaxis/view/panels/ProdutoPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.presenter.ProdutoPresenter;
import com.titanaxis.view.dialogs.LoteDialog;         // <-- ADICIONE ESTE IMPORT
import com.titanaxis.view.dialogs.ProdutoDialog;   // <-- ADICIONE ESTE IMPORT
import com.titanaxis.view.interfaces.ProdutoView;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProdutoPanel extends JPanel implements ProdutoView {

    private ProdutoViewListener listener;
    private final AppContext appContext;

    private DefaultTableModel produtoTableModel, loteTableModel;
    private JTable produtoTable, loteTable;
    private JButton editProdutoButton, addLoteButton, editLoteButton, deleteLoteButton, toggleStatusButton;
    private JToggleButton showInactiveButton;
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProdutoPanel(AppContext appContext) {
        this.appContext = appContext;
        initComponents();
        new ProdutoPresenter(this, appContext.getProdutoService(), appContext.getAuthService());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createProdutoListPanel(), createDetalhesPanel());
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
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
                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
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
                    listener.aoSelecionarProduto(produtoId);
                } else {
                    limparPainelDeDetalhes();
                }
            }
        });
        panel.add(new JScrollPane(produtoTable), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton novoProdutoButton = new JButton("Novo Produto");
        novoProdutoButton.addActionListener(e -> listener.aoClicarNovoProduto());
        editProdutoButton = new JButton("Editar Produto");
        editProdutoButton.addActionListener(e -> listener.aoClicarEditarProduto());
        toggleStatusButton = new JButton("Inativar/Reativar");
        toggleStatusButton.addActionListener(e -> listener.aoAlternarStatusDoProduto());
        showInactiveButton = new JToggleButton("Mostrar Inativos");
        showInactiveButton.addActionListener(e -> listener.aoCarregarProdutos());

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
        addLoteButton.addActionListener(e -> listener.aoClicarAdicionarLote());
        editLoteButton = new JButton("Editar Lote");
        editLoteButton.addActionListener(e -> listener.aoClicarEditarLote());
        deleteLoteButton = new JButton("Remover Lote");
        deleteLoteButton.addActionListener(e -> listener.aoClicarRemoverLote());

        buttonsPanel.add(addLoteButton);
        buttonsPanel.add(editLoteButton);
        buttonsPanel.add(deleteLoteButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public void setProdutosNaTabela(List<Produto> produtos) {
        int selectedRow = produtoTable.getSelectedRow();
        int selectedId = -1;
        if (selectedRow != -1) {
            selectedId = (int) produtoTableModel.getValueAt(produtoTable.convertRowIndexToModel(selectedRow), 0);
        }

        produtoTableModel.setRowCount(0);
        for (Produto p : produtos) {
            produtoTableModel.addRow(new Object[]{
                    p.getId(), p.getNome(), p.getNomeCategoria(), p.getQuantidadeTotal(), p.isAtivo() ? "Ativo" : "Inativo"
            });
        }

        if (selectedId != -1) {
            for (int i = 0; i < produtoTableModel.getRowCount(); i++) {
                if ((int) produtoTableModel.getValueAt(i, 0) == selectedId) {
                    int viewRow = produtoTable.convertRowIndexToView(i);
                    produtoTable.setRowSelectionInterval(viewRow, viewRow);
                    break;
                }
            }
        }
    }

    @Override
    public void setLotesNaTabela(List<Lote> lotes) {
        loteTableModel.setRowCount(0);
        for (Lote lote : lotes) {
            loteTableModel.addRow(new Object[]{
                    lote.getId(), lote.getNumeroLote(), lote.getQuantidade(),
                    lote.getDataValidade() != null ? lote.getDataValidade().format(DATE_FORMATTER) : "N/A"
            });
        }
    }

    @Override
    public void setBotoesDeAcaoEnabled(boolean enabled) {
        editProdutoButton.setEnabled(enabled);
        addLoteButton.setEnabled(enabled);
        editLoteButton.setEnabled(enabled);
        deleteLoteButton.setEnabled(enabled);
        toggleStatusButton.setEnabled(enabled);
    }

    @Override
    public void setTextoBotaoStatus(String texto) {
        toggleStatusButton.setText(texto);
    }

    @Override
    public void limparPainelDeDetalhes() {
        loteTableModel.setRowCount(0);
        setBotoesDeAcaoEnabled(false);
        toggleStatusButton.setText("Inativar/Reativar");
    }

    @Override
    public void limparSelecaoDaTabelaDeProdutos() {
        produtoTable.clearSelection();
    }

    @Override
    public boolean isMostrarInativos() {
        return showInactiveButton.isSelected();
    }

    @Override
    public int getSelectedLoteId() {
        int selectedRow = loteTable.getSelectedRow();
        return (selectedRow != -1) ? (int) loteTableModel.getValueAt(selectedRow, 0) : -1;
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
    public void mostrarDialogoDeProduto(Produto produto) {
        ProdutoDialog dialog = new ProdutoDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                appContext.getProdutoService(), appContext.getCategoriaService(), produto,
                appContext.getAuthService().getUsuarioLogado().orElse(null));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            listener.aoCarregarProdutos();
        }
    }

    @Override
    public void mostrarDialogoDeLote(Produto produtoPai, Lote lote) {
        LoteDialog dialog = new LoteDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                appContext.getProdutoService(), produtoPai, lote,
                appContext.getAuthService().getUsuarioLogado().orElse(null));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            listener.aoSelecionarProduto(produtoPai.getId());
            listener.aoCarregarProdutos();
        }
    }

    @Override
    public void setListener(ProdutoViewListener listener) {
        this.listener = listener;
    }
}