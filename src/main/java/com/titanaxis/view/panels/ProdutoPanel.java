// src/main/java/com/titanaxis/view/panels/ProdutoPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.presenter.ProdutoPresenter;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.dialogs.LoteDialog;
import com.titanaxis.view.dialogs.ProdutoDialog;
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

    private final DefaultTableModel produtoTableModel;
    private final DefaultTableModel loteTableModel;
    private final JTable produtoTable;
    private final JTable loteTable;
    private final JButton editProdutoButton;
    private final JButton addLoteButton;
    private final JButton editLoteButton;
    private final JButton deleteLoteButton;
    private final JButton toggleStatusButton;
    private final JToggleButton showInactiveButton;
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProdutoPanel(AppContext appContext) {
        this.appContext = appContext;

        produtoTableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Categoria", "Qtd. Total", "Estado"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        produtoTable = new JTable(produtoTableModel);

        loteTableModel = new DefaultTableModel(new String[]{"ID Lote", "Nº Lote", "Quantidade", "Data de Validade"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        loteTable = new JTable(loteTableModel);

        editProdutoButton = new JButton("Editar Produto");
        addLoteButton = new JButton("Adicionar Lote");
        editLoteButton = new JButton("Editar Lote");
        deleteLoteButton = new JButton("Remover Lote");
        toggleStatusButton = new JButton("Inativar/Reativar");
        showInactiveButton = new JToggleButton("Mostrar Inativos");

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

        produtoTable.setRowSorter(new TableRowSorter<>(produtoTableModel));
        produtoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        editProdutoButton.addActionListener(e -> listener.aoClicarEditarProduto());
        toggleStatusButton.addActionListener(e -> listener.aoAlternarStatusDoProduto());
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
        panel.add(new JScrollPane(loteTable), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addLoteButton.addActionListener(e -> listener.aoClicarAdicionarLote());
        editLoteButton.addActionListener(e -> listener.aoClicarEditarLote());
        deleteLoteButton.addActionListener(e -> listener.aoClicarRemoverLote());

        buttonsPanel.add(addLoteButton);
        buttonsPanel.add(editLoteButton);
        buttonsPanel.add(deleteLoteButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public void setProdutosNaTabela(List<Produto> produtos) {
        produtoTableModel.setRowCount(0);
        for (Produto p : produtos) {
            produtoTableModel.addRow(new Object[]{
                    p.getId(), p.getNome(), p.getNomeCategoria(), p.getQuantidadeTotal(), p.isAtivo() ? "Ativo" : "Inativo"
            });
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
    public ProdutoDialog mostrarDialogoDeProduto(Produto produto) {
        return new ProdutoDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                appContext.getProdutoService(), appContext.getCategoriaService(), produto,
                appContext.getAuthService().getUsuarioLogado().orElse(null));
    }

    @Override
    public LoteDialog mostrarDialogoDeLote(Produto produtoPai, Lote lote) {
        return new LoteDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                appContext.getProdutoService(), produtoPai, lote,
                appContext.getAuthService().getUsuarioLogado().orElse(null));
    }

    public void refreshData() {
        if (listener != null) {
            listener.aoCarregarProdutos();
            limparSelecaoDaTabelaDeProdutos();
            limparPainelDeDetalhes();
        }
    }

    public JButton getAddLoteButton() {
        return addLoteButton;
    }

    public void selectFirstProduct() {
        if (produtoTable.getRowCount() > 0) {
            produtoTable.setRowSelectionInterval(0, 0);
        }
    }

    @Override
    public void setListener(ProdutoViewListener listener) {
        this.listener = listener;
    }
}