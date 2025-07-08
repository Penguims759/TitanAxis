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
        novoProdutoButton = new JButton("Novo Produto");
        importarCsvButton = new JButton("Importar de CSV");
        importarPdfButton = new JButton("Importar de PDF");

        addTooltips();
        initComponents();

        new ProdutoPresenter(this, appContext.getProdutoService(), appContext.getAuthService());
    }

    private void addTooltips() {
        novoProdutoButton.setToolTipText("Criar um novo produto no sistema.");
        importarCsvButton.setToolTipText("Importar uma lista de produtos a partir de um ficheiro CSV.");
        importarPdfButton.setToolTipText("Importar uma lista de produtos a partir de um ficheiro PDF (funcionalidade futura).");
        editProdutoButton.setToolTipText("Editar os detalhes do produto selecionado.");
        toggleStatusButton.setToolTipText("Alternar o estado do produto entre Ativo e Inativo.");
        showInactiveButton.setToolTipText("Exibir ou ocultar os produtos inativos na lista.");
        addLoteButton.setToolTipText("Adicionar um novo lote de estoque para o produto selecionado.");
        editLoteButton.setToolTipText("Editar o lote selecionado na tabela de lotes.");
        deleteLoteButton.setToolTipText("Remover permanentemente o lote selecionado.");
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
        filterPanel.add(new JLabel("Filtrar por Nome:"));
        filterPanel.add(filtroTexto);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Atualizar");
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
        panel.setBorder(BorderFactory.createTitledBorder("Produtos Cadastrados"));

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
                    // CORRIGIDO: Usada a variável 'produtoTable' em vez de 'table'
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
        fileChooser.setDialogTitle("Selecionar Ficheiro CSV para Importação");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Ficheiros CSV", "csv"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    @Override
    public File mostrarSeletorDeFicheiroPdf() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Ficheiro PDF para Importação");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Ficheiros PDF", "pdf"));
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

        JMenuItem editarItem = new JMenuItem("Editar Produto");
        editarItem.addActionListener(e -> listener.aoClicarEditarProduto());

        JMenuItem statusItem = new JMenuItem("Inativar/Reativar");
        statusItem.addActionListener(e -> listener.aoAlternarStatusDoProduto());

        JMenuItem loteItem = new JMenuItem("Adicionar Lote");
        loteItem.addActionListener(e -> listener.aoClicarAdicionarLote());

        contextMenu.add(editarItem);
        contextMenu.add(statusItem);
        contextMenu.addSeparator();
        contextMenu.add(loteItem);

        return contextMenu;
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