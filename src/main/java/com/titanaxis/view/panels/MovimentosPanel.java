// src/main/java/com/titanaxis/view/panels/MovimentosPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class MovimentosPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton refreshButton;

    public MovimentosPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Histórico de Movimentos de Estoque"));

        // Painel de Filtro e Refresh
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filtrar:"));
        JTextField filterField = new JTextField(30);
        filterPanel.add(filterField);
        topPanel.add(filterPanel, BorderLayout.WEST);

        refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> loadMovimentos());
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
                String text = filterField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        loadMovimentos();
    }

    private void loadMovimentos() {
        String sql = "SELECT m.data_movimento, p.nome as nome_produto, l.numero_lote, m.tipo_movimento, m.quantidade, u.nome_usuario " +
                "FROM movimentos_estoque m " +
                "JOIN produtos p ON m.produto_id = p.id " +
                "LEFT JOIN estoque_lotes l ON m.lote_id = l.id " +
                "LEFT JOIN usuarios u ON m.usuario_id = u.id " +
                "ORDER BY m.id DESC"; // Ordena pelo ID para ver os mais recentes primeiro

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("data_movimento"));
                row.add(rs.getString("nome_produto"));
                row.add(rs.getString("numero_lote"));
                row.add(rs.getString("tipo_movimento"));
                row.add(rs.getInt("quantidade"));
                row.add(rs.getString("nome_usuario"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar o histórico de movimentos.", "Erro de Base de Dados", JOptionPane.ERROR_MESSAGE);
        }
    }
}