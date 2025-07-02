// src/main/java/com/titanaxis/repository/impl/VendaRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VendaRepositoryImpl implements VendaRepository {
    private static final Logger logger = AppLogger.getLogger();
    private static final DateTimeFormatter DB_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Venda save(Venda venda) {
        String sqlVenda = "INSERT INTO vendas (cliente_id, usuario_id, valor_total) VALUES (?, ?, ?)";
        String sqlItem = "INSERT INTO venda_itens (venda_id, produto_id, lote_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?, ?)";
        String sqlUpdateLote = "UPDATE estoque_lotes SET quantidade = quantidade - ? WHERE id = ? AND quantidade >= ?";
        String sqlMovimento = "INSERT INTO movimentos_estoque (produto_id, lote_id, tipo_movimento, quantidade, usuario_id) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int vendaId;
            try (PreparedStatement psVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                psVenda.setInt(1, venda.getClienteId());
                psVenda.setInt(2, venda.getUsuarioId());
                psVenda.setDouble(3, venda.getValorTotal());
                psVenda.executeUpdate();

                try (ResultSet rs = psVenda.getGeneratedKeys()) {
                    if (rs.next()) {
                        vendaId = rs.getInt(1);
                        venda.setId(vendaId);
                    } else {
                        throw new SQLException("Falha ao obter o ID da venda.");
                    }
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(sqlItem);
                 PreparedStatement psUpdateLote = conn.prepareStatement(sqlUpdateLote);
                 PreparedStatement psMovimento = conn.prepareStatement(sqlMovimento)) {

                for (VendaItem item : venda.getItens()) {
                    psUpdateLote.setInt(1, item.getQuantidade());
                    psUpdateLote.setInt(2, item.getLote().getId());
                    psUpdateLote.setInt(3, item.getQuantidade());
                    if (psUpdateLote.executeUpdate() == 0) {
                        throw new SQLException("Falha ao atualizar estoque para o lote ID: " + item.getLote().getId() + ". Stock insuficiente ou lote não encontrado.");
                    }

                    psItem.setInt(1, vendaId);
                    psItem.setInt(2, item.getLote().getProdutoId());
                    psItem.setInt(3, item.getLote().getId());
                    psItem.setInt(4, item.getQuantidade());
                    psItem.setDouble(5, item.getPrecoUnitario());
                    psItem.addBatch();

                    psMovimento.setInt(1, item.getLote().getProdutoId());
                    psMovimento.setInt(2, item.getLote().getId());
                    psMovimento.setString(3, "VENDA");
                    psMovimento.setInt(4, item.getQuantidade());
                    psMovimento.setInt(5, venda.getUsuarioId());
                    psMovimento.addBatch();
                }
                psItem.executeBatch();
                psMovimento.executeBatch();
            }

            conn.commit();
            logger.info("Venda ID " + vendaId + " salva com sucesso.");
            return venda;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro na transação de venda. Rollback acionado.", e);
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { logger.log(Level.SEVERE, "Falha no rollback.", ex); } }
            return null;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { logger.log(Level.SEVERE, "Falha ao fechar conexão.", e); } }
        }
    }

    @Override
    public List<Venda> findAll() {
        List<Venda> vendas = new ArrayList<>();
        String sql = "SELECT v.id, v.cliente_id, v.usuario_id, v.data_venda, v.valor_total, c.nome AS nome_cliente " +
                "FROM vendas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "ORDER BY v.data_venda DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                vendas.add(mapRowToVenda(rs));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar vendas.", e);
        }
        return vendas;
    }

    private Venda mapRowToVenda(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int clienteId = rs.getInt("cliente_id");
        int usuarioId = rs.getInt("usuario_id");
        LocalDateTime dataVenda = LocalDateTime.parse(rs.getString("data_venda"), DB_DATE_TIME_FORMATTER);
        double valorTotal = rs.getDouble("valor_total");
        String nomeCliente = rs.getString("nome_cliente");

        return new Venda(id, clienteId, usuarioId, dataVenda, valorTotal, nomeCliente);
    }

    @Override public Optional<Venda> findById(Integer id) { return Optional.empty(); }
    @Override public void deleteById(Integer id) {}
}