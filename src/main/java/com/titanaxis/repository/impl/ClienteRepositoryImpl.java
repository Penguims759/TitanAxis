package com.titanaxis.repository.impl;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
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
    private final AuditoriaRepository auditoriaRepository = new AuditoriaRepositoryImpl();

    @Override
    public Cliente save(Cliente cliente) {
        return this.save(cliente, null);
    }

    @Override
    public Cliente save(Cliente cliente, Usuario ator) {
        boolean isUpdate = cliente.getId() != 0;
        Cliente clienteSalvo = isUpdate ? update(cliente) : insert(cliente);

        if (clienteSalvo != null && ator != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            String detalhes = String.format("Cliente '%s' (ID: %d) foi %s.",
                    clienteSalvo.getNome(), clienteSalvo.getId(), isUpdate ? "atualizado" : "criado");
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Cliente", detalhes);
        }
        return clienteSalvo;
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
        this.deleteById(id, null);
    }

    @Override
    public void deleteById(Integer id, Usuario ator) {
        // Primeiro, busca o cliente para obter o nome para o log
        Optional<Cliente> clienteOpt = findById(id);

        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0 && ator != null && clienteOpt.isPresent()) {
                String detalhes = String.format("Cliente '%s' (ID: %d) foi eliminado.", clienteOpt.get().getNome(), id);
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO", "Cliente", detalhes);
            }

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