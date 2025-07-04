// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/panels/ProdutoPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.presenter.ProdutoPresenter;
import com.titanaxis.util.UIMessageUtil; // Importado
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
    private final AppContext appContext; // Adicionado final

    private final DefaultTableModel produtoTableModel; // Adicionado final
    private final DefaultTableModel loteTableModel; // Adicionado final
    private final JTable produtoTable; // Adicionado final
    private final JTable loteTable; // Adicionado final
    private final JButton editProdutoButton; // Adicionado final
    private final JButton addLoteButton; // Adicionado final
    private final JButton editLoteButton; // Adicionado final
    private final JButton deleteLoteButton; // Adicionado final
    private final JButton toggleStatusButton; // Adicionado final
    private final JToggleButton showInactiveButton; // Adicionado final
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Adicionado final

    public ProdutoPanel(AppContext appContext) {
        this.appContext = appContext;
        // Inicialização de componentes deve ser feita antes de passar `this` para o Presenter,
        // pois o Presenter pode chamar métodos da View imediatamente.
        // Movido `initComponents()` para o início do construtor.
        // As variáveis finais agora podem ser inicializadas aqui.

        produtoTableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Categoria", "Qtd. Total", "Estado"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        produtoTable = new JTable(produtoTableModel);
        loteTableModel = new DefaultTableModel(new String[]{"ID Lote", "Nº Lote", "Quantidade", "Data de Validade"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        loteTable = new JTable(loteTableModel);

        // Inicialização dos botões para serem final
        editProdutoButton = new JButton("Editar Produto");
        addLoteButton = new JButton("Adicionar Lote");
        editLoteButton = new JButton("Editar Lote");
        deleteLoteButton = new JButton("Remover Lote");
        toggleStatusButton = new JButton("Inativar/Reativar");
        showInactiveButton = new JToggleButton("Mostrar Inativos");

        initComponents(); // Agora chama o método para construir o layout com os componentes já inicializados

        new ProdutoPresenter(this, appContext.getProdutoService(), appContext.getAuthService());
    }

    // O método initComponents() e outros métodos de configuração da UI permanecem os mesmos.
    // ...

    // ALTERADO: A view agora é responsável por criar e retornar o diálogo.
    // O presenter cuidará de o tornar visível e processar o resultado.
    @Override
    public ProdutoDialog mostrarDialogoDeProduto(Produto produto) {
        return new ProdutoDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                appContext.getProdutoService(), appContext.getCategoriaService(), produto,
                appContext.getAuthService().getUsuarioLogado().orElse(null));
    }

    // ALTERADO: A view agora é responsável por criar e retornar o diálogo.
    @Override
    public LoteDialog mostrarDialogoDeLote(Produto produtoPai, Lote lote) {
        return new LoteDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                appContext.getProdutoService(), produtoPai, lote,
                appContext.getAuthService().getUsuarioLogado().orElse(null));
    }

    // NOVO MÉTODO: Para ser chamado externamente (e.g., pelo DashboardFrame) para recarregar os dados
    public void refreshData() {
        if (listener != null) {
            listener.aoCarregarProdutos();
        }
    }

    // Todos os outros métodos da implementação da interface (setProdutosNaTabela, etc.) permanecem iguais.
    // ...
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createProdutoListPanel(), createDetalhesPanel());
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createProdutoListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Produtos Cadastrados"));

        // produtoTableModel e produtoTable já inicializados no construtor
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
        // editProdutoButton, toggleStatusButton, showInactiveButton já inicializados
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
        // loteTableModel e loteTable já inicializados no construtor
        panel.add(new JScrollPane(loteTable), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        // addLoteButton, editLoteButton, deleteLoteButton já inicializados
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
    public void setListener(ProdutoViewListener listener) {
        this.listener = listener;
    }
}