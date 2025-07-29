package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.util.AppLogger;
import com.titanaxis.view.dialogs.VendaDetalhesDialog;
import com.titanaxis.view.interfaces.MovimentoView;
import com.titanaxis.view.renderer.MovimentoTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;

public class MovimentosPanel extends JPanel implements MovimentoView {
    private MovimentoViewListener listener;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final Logger logger = AppLogger.getLogger();

    private JSpinner dataInicioSpinner;
    private JSpinner dataFimSpinner;
    private final Timer dateFilterTimer;
    private AppContext appContext; // Injetado via setter
    private final Frame owner;

    public MovimentosPanel(Frame owner) {
        this.owner = owner;

        tableModel = new DefaultTableModel(new String[]{
                I18n.getString("movement.table.header.date"),
                I18n.getString("movement.table.header.product"),
                I18n.getString("movement.table.header.batch"),
                I18n.getString("movement.table.header.type"),
                I18n.getString("movement.table.header.quantity"),
                I18n.getString("movement.table.header.user"),
                I18n.getString("movement.table.header.saleId")
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);

        initComponents();

        dateFilterTimer = new Timer(500, e -> listener.aoFiltrarPorData());
        dateFilterTimer.setRepeats(false);
    }

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(I18n.getString("movement.panel.title")));

        add(createTopPanel(), BorderLayout.NORTH);

        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setWidth(0);

        table.setDefaultRenderer(Object.class, new MovimentoTableCellRenderer());

        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    if ("VENDA".equals(tableModel.getValueAt(modelRow, 3))) {
                        Integer vendaId = (Integer) tableModel.getValueAt(modelRow, 6);
                        if (vendaId != null) {
                            abrirDetalhesVenda(vendaId);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        southPanel.add(new JLabel(I18n.getString("movement.panel.tip")));
        add(southPanel, BorderLayout.SOUTH);
    }

    private void abrirDetalhesVenda(int vendaId) {
        if (appContext == null) return;
        try {
            appContext.getVendaService().buscarVendaCompletaPorId(vendaId)
                    .ifPresentOrElse(
                            venda -> {
                                VendaDetalhesDialog dialog = new VendaDetalhesDialog(owner, venda, appContext.getRelatorioService());
                                dialog.setVisible(true);
                            },
                            () -> UIMessageUtil.showWarningMessage(this, I18n.getString("movement.error.detailsNotFound"), I18n.getString("warning.title"))
                    );
        } catch (Exception e) {
            logger.error("Erro ao abrir detalhes de venda.", e);
            UIMessageUtil.showErrorMessage(this, I18n.getString("movement.error.unexpectedDetails", e.getMessage()), I18n.getString("error.critical.title"));
        }
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel textFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textFilterPanel.add(new JLabel(I18n.getString("movement.label.filterByText")));
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

        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        SpinnerDateModel inicioModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dataInicioSpinner = new JSpinner(inicioModel);
        dataInicioSpinner.setEditor(new JSpinner.DateEditor(dataInicioSpinner, "dd/MM/yyyy"));
        dataInicioSpinner.addChangeListener(e -> dateFilterTimer.restart());

        SpinnerDateModel fimModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dataFimSpinner = new JSpinner(fimModel);
        dataFimSpinner.setEditor(new JSpinner.DateEditor(dataFimSpinner, "dd/MM/yyyy"));
        dataFimSpinner.addChangeListener(e -> dateFilterTimer.restart());

        JButton refreshButton = new JButton(I18n.getString("button.refresh"));
        refreshButton.addActionListener(e -> listener.aoCarregarMovimentos());

        JButton clearFilterButton = new JButton(I18n.getString("button.clearFilters"));
        clearFilterButton.addActionListener(e -> {
            filterField.setText("");
            dataInicioSpinner.setValue(new Date());
            dataFimSpinner.setValue(new Date());
            listener.aoCarregarMovimentos();
        });

        dateFilterPanel.add(new JLabel(I18n.getString("movement.label.from")));
        dateFilterPanel.add(dataInicioSpinner);
        dateFilterPanel.add(new JLabel(I18n.getString("movement.label.to")));
        dateFilterPanel.add(dataFimSpinner);
        dateFilterPanel.add(clearFilterButton);
        dateFilterPanel.add(refreshButton);

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
                    m.getNomeUsuario(),
                    m.getVendaId()
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