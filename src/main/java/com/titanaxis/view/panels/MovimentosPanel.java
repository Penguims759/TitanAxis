package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.presenter.MovimentoPresenter;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.interfaces.MovimentoView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MovimentosPanel extends JPanel implements MovimentoView {
    private MovimentoViewListener listener;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private JSpinner dataInicioSpinner;
    private JSpinner dataFimSpinner;
    private final Timer dateFilterTimer;

    public MovimentosPanel(AppContext appContext) {
        tableModel = new DefaultTableModel(new String[]{"Data", "Produto", "Lote", "Tipo", "Quantidade", "Utilizador"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);

        initComponents();
        new MovimentoPresenter(this, appContext.getMovimentoService());

        dateFilterTimer = new Timer(500, e -> listener.aoFiltrarPorData());
        dateFilterTimer.setRepeats(false);

        listener.aoCarregarMovimentos();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Histórico de Movimentos de Estoque"));

        add(createTopPanel(), BorderLayout.NORTH);

        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        // PAINEL DE FILTRO DE TEXTO
        JPanel textFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textFilterPanel.add(new JLabel("Filtrar por texto:"));
        JTextField filterField = new JTextField(30);
        textFilterPanel.add(filterField);
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterField.getText()));
            }
        });

        // PAINEL DE FILTRO DE DATA
        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        SpinnerDateModel inicioModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dataInicioSpinner = new JSpinner(inicioModel);
        dataInicioSpinner.setEditor(new JSpinner.DateEditor(dataInicioSpinner, "dd/MM/yyyy"));
        dataInicioSpinner.addChangeListener(e -> dateFilterTimer.restart());

        SpinnerDateModel fimModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dataFimSpinner = new JSpinner(fimModel);
        dataFimSpinner.setEditor(new JSpinner.DateEditor(dataFimSpinner, "dd/MM/yyyy"));
        dataFimSpinner.addChangeListener(e -> dateFilterTimer.restart());

        JButton clearFilterButton = new JButton("Limpar Filtros");
        clearFilterButton.addActionListener(e -> {
            filterField.setText("");
            dataInicioSpinner.setValue(new Date());
            dataFimSpinner.setValue(new Date());
            listener.aoCarregarMovimentos();
        });

        dateFilterPanel.add(new JLabel("De:"));
        dateFilterPanel.add(dataInicioSpinner);
        dateFilterPanel.add(new JLabel("Até:"));
        dateFilterPanel.add(dataFimSpinner);
        dateFilterPanel.add(clearFilterButton);

        topPanel.add(textFilterPanel, BorderLayout.WEST);
        topPanel.add(dateFilterPanel, BorderLayout.EAST);

        return topPanel;
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
    public LocalDate getDataInicio() {
        Date date = (Date) dataInicioSpinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public LocalDate getDataFim() {
        Date date = (Date) dataFimSpinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void refreshData() {
        if (listener != null) {
            listener.aoCarregarMovimentos();
        }
    }

    @Override
    public void setListener(MovimentoViewListener listener) {
        this.listener = listener;
    }
}