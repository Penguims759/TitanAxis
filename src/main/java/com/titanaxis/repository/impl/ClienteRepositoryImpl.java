package com.titanaxis.repository.impl;

import com.titanaxis.model.Cliente;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteRepositoryImpl implements ClienteRepository {
    private static final Logger logger = AppLogger.getLogger();

    @Override
    public Cliente save(Cliente cliente) {
        if (cliente.getId() == 0) {
            return insert(cliente);
        } else {
            return update(cliente);
        }
    }

    private Cliente insert(Cliente cliente) {
        String sql = "INSERT INTO clientes (nome, contato, endereco) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getContato());
            ps.setString(3, cliente.getEndereco());
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        cliente.setId(rs.getInt(1));
                        return cliente;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao inserir cliente: " + cliente.getNome(), e);
        }
        return null;
    }

    private Cliente update(Cliente cliente) {
        String sql = "UPDATE clientes SET nome = ?, contato = ?, endereco = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getContato());
            ps.setString(3, cliente.getEndereco());
            ps.setInt(4, cliente.getId());
            if (ps.executeUpdate() > 0) {
                return cliente;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar cliente ID: " + cliente.getId(), e);
        }
        return null;
    }

    @Override
    public Optional<Cliente> findById(Integer id) {
        String sql = "SELECT id, nome, contato, endereco FROM clientes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Cliente(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("contato"),
                            rs.getString("endereco")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar cliente por ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Cliente> findAll() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, nome, contato, endereco FROM clientes ORDER BY nome";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(new Cliente(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("contato"),
                        rs.getString("endereco")
                ));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar clientes", e);
        }
        return clientes;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao deletar cliente ID: " + id, e);
        }
    }

    @Override
    public List<Cliente> findByNomeContaining(String nome) {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, nome, contato, endereco FROM clientes WHERE LOWER(nome) LIKE LOWER(?) ORDER BY nome";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nome + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientes.add(new Cliente(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("contato"),
                            rs.getString("endereco")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar clientes por nome: " + nome, e);
        }
        return clientes;
    }
}