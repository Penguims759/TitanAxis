// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/panels/MovimentosPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.presenter.MovimentoPresenter;
import com.titanaxis.util.UIMessageUtil; // Importado
import com.titanaxis.view.interfaces.MovimentoView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MovimentosPanel extends JPanel implements MovimentoView {
    private MovimentoViewListener listener;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public MovimentosPanel(AppContext appContext) {
        initComponents();
        new MovimentoPresenter(this, appContext.getMovimentoService());
        listener.aoCarregarMovimentos(); // Inicia o carregamento
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Histórico de Movimentos de Estoque"));

        // Painel de Filtro e Refresh
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filtrar:"));
        JTextField filterField = new JTextField(30);
        filterPanel.add(filterField);
        topPanel.add(filterPanel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> listener.aoCarregarMovimentos());
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);
        topPanel.add(refreshPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Tabela de Movimentos
        tableModel = new DefaultTableModel(new String[]{"Data", "Produto", "Lote", "Tipo", "Quantidade", "Utilizador"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Ação de filtro
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterField.getText()));
            }
        });
    }

    @Override
    public void setMovimentosNaTabela(List<MovimentoEstoque> movimentos) {
        tableModel.setRowCount(0);
        for (MovimentoEstoque m : movimentos) {
            tableModel.addRow(new Object[]{
                    m.getDataMovimento().format(FORMATTER),
                    m.getNomeProduto(),
                    m.getNumeroLote(),
                    m.getTipoMovimento(),
                    m.getQuantidade(),
                    m.getNomeUsuario()
            });
        }
    }

    @Override
    public void mostrarErro(String titulo, String mensagem) {
        UIMessageUtil.showErrorMessage(this, mensagem, titulo);
    }

    @Override
    public void setCursorEspera(boolean emEspera) {
        setCursor(emEspera ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override
    public void setListener(MovimentoViewListener listener) {
        this.listener = listener;
    }
}