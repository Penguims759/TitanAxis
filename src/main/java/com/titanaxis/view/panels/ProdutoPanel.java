// src/main/java/com/titanaxis/view/panels/ProdutoPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.presenter.ProdutoPresenter;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.dialogs.LoteDialog;
import com.titanaxis.view.dialogs.ProdutoDialog;
import com.titanaxis.view.interfaces.ProdutoView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
    private final JButton novoProdutoButton;
    private final JButton importarCsvButton;
    private final JButton importarPdfButton;
    private JTextField filtroTexto;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProdutoPanel(AppContext appContext) {
        this.appContext = appContext;

        produtoTableModel = new DefaultTableModel(new String[]{
                I18n.getString("product.table.header.id"),
                I18n.getString("product.table.header.name"),
                I18n.getString("product.table.header.category"),
                I18n.getString("product.table.header.quantity"),
                I18n.getString("product.table.header.status")
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        produtoTable = new JTable(produtoTableModel);

        loteTableModel = new DefaultTableModel(new String[]{
                I18n.getString("batch.table.header.id"),
                I18n.getString("batch.table.header.number"),
                I18n.getString("batch.table.header.quantity"),
                I18n.getString("batch.table.header.expiry")
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        loteTable = new JTable(loteTableModel);

        editProdutoButton = new JButton(I18n.getString("product.button.edit"));
        addLoteButton = new JButton(I18n.getString("product.button.addBatch"));
        editLoteButton = new JButton(I18n.getString("product.button.editBatch"));
        deleteLoteButton = new JButton(I18n.getString("product.button.removeBatch"));
        toggleStatusButton = new JButton(I18n.getString("product.button.toggleStatus"));
        showInactiveButton = new JToggleButton(I18n.getString("product.button.showInactive"));
        novoProdutoButton = new JButton(I18n.getString("product.button.new"));
        importarCsvButton = new JButton(I18n.getString("product.button.importCsv"));
        importarPdfButton = new JButton(I18n.getString("product.button.importPdf"));

        addTooltips();
        initComponents();

        new ProdutoPresenter(this, appContext.getProdutoService(), appContext.getAuthService());
    }

    private void addTooltips() {
        novoProdutoButton.setToolTipText(I18n.getString("product.tooltip.new"));
        importarCsvButton.setToolTipText(I18n.getString("product.tooltip.importCsv"));
        importarPdfButton.setToolTipText(I18n.getString("product.tooltip.importPdf"));
        editProdutoButton.setToolTipText(I18n.getString("product.tooltip.edit"));
        toggleStatusButton.setToolTipText(I18n.getString("product.tooltip.toggleStatus"));
        showInactiveButton.setToolTipText(I18n.getString("product.tooltip.showInactive"));
        addLoteButton.setToolTipText(I18n.getString("product.tooltip.addBatch"));
        editLoteButton.setToolTipText(I18n.getString("product.tooltip.editBatch"));
        deleteLoteButton.setToolTipText(I18n.getString("product.tooltip.removeBatch"));
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createProdutoListPanel(), createDetalhesPanel());
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createFilterAndActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtroTexto = new JTextField(25);
        filtroTexto.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { listener.aoFiltrarTexto(filtroTexto.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { listener.aoFiltrarTexto(filtroTexto.getText()); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { listener.aoFiltrarTexto(filtroTexto.getText()); }
        });
        filterPanel.add(new JLabel(I18n.getString("product.label.filterByName")));
        filterPanel.add(filtroTexto);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton(I18n.getString("product.button.refresh"));
        refreshButton.addActionListener(e -> listener.aoCarregarProdutos());

        importarCsvButton.addActionListener(e -> listener.aoClicarImportarCsv());
        importarPdfButton.addActionListener(e -> listener.aoClicarImportarPdf());

        actionsPanel.add(importarCsvButton);
        actionsPanel.add(importarPdfButton);
        actionsPanel.add(refreshButton);

        panel.add(filterPanel, BorderLayout.WEST);
        panel.add(actionsPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createProdutoListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(I18n.getString("product.border.registeredProducts")));

        panel.add(createFilterAndActionsPanel(), BorderLayout.NORTH);

        produtoTable.setRowSorter(new TableRowSorter<>(produtoTableModel));
        produtoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        produtoTable.setComponentPopupMenu(createProdutoContextMenu());
        produtoTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                int row = produtoTable.rowAtPoint(point);
                if (row != -1) {
                    produtoTable.setRowSelectionInterval(row, row);
                }
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

    @Override
    public File mostrarSeletorDeFicheiroCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(I18n.getString("product.dialog.importCsv.title"));
        fileChooser.setFileFilter(new FileNameExtensionFilter(I18n.getString("product.dialog.importCsv.filter"), "csv"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    @Override
    public File mostrarSeletorDeFicheiroPdf() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(I18n.getString("product.dialog.importPdf.title"));
        fileChooser.setFileFilter(new FileNameExtensionFilter(I18n.getString("product.dialog.importPdf.filter"), "pdf"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    @Override
    public void setFiltroDeTexto(String texto) {
        if (!filtroTexto.getText().equals(texto)) {
            filtroTexto.setText(texto);
        }
    }

    @Override
    public void aplicarFiltroNaTabela(String texto) {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) produtoTable.getRowSorter();
        if (texto != null && !texto.trim().isEmpty()) {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        } else {
            sorter.setRowFilter(null);
        }
    }

    private JPopupMenu createProdutoContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem editarItem = new JMenuItem(I18n.getString("product.contextMenu.edit"));
        editarItem.addActionListener(e -> listener.aoClicarEditarProduto());

        JMenuItem statusItem = new JMenuItem(I18n.getString("product.contextMenu.toggleStatus"));
        statusItem.addActionListener(e -> listener.aoAlternarStatusDoProduto());

        JMenuItem loteItem = new JMenuItem(I18n.getString("product.contextMenu.addBatch"));
        loteItem.addActionListener(e -> listener.aoClicarAdicionarLote());

        contextMenu.add(editarItem);
        contextMenu.add(statusItem);
        contextMenu.addSeparator();
        contextMenu.add(loteItem);

        return contextMenu;
    }

    private JPanel createDetalhesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(I18n.getString("product.border.stockAndBatches")));
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
        String active = I18n.getString("product.status.active");
        String inactive = I18n.getString("product.status.inactive");
        for (Produto p : produtos) {
            produtoTableModel.addRow(new Object[]{
                    p.getId(), p.getNome(), p.getNomeCategoria(), p.getQuantidadeTotal(), p.isAtivo() ? active : inactive
            });
        }
    }

    @Override
    public void setLotesNaTabela(List<Lote> lotes) {
        loteTableModel.setRowCount(0);
        String notAvailable = I18n.getString("general.notAvailable");
        for (Lote lote : lotes) {
            loteTableModel.addRow(new Object[]{
                    lote.getId(), lote.getNumeroLote(), lote.getQuantidade(),
                    lote.getDataValidade() != null ? lote.getDataValidade().format(DATE_FORMATTER) : notAvailable
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
        toggleStatusButton.setText(I18n.getString("product.button.toggleStatus"));
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
        return new ProdutoDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                appContext.getProdutoService(),
                appContext.getCategoriaService(),
                appContext.getFornecedorService(),
                produto,
                appContext.getAuthService().getUsuarioLogado().orElse(null)
        );
    }

    @Override
    public LoteDialog mostrarDialogoDeLote(Produto produtoPai, Lote lote) {
        return new LoteDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                appContext.getProdutoService(),
                produtoPai,
                lote,
                appContext.getAuthService().getUsuarioLogado().orElse(null)
        );
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

    public JButton getNovoProdutoButton() {
        return novoProdutoButton;
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