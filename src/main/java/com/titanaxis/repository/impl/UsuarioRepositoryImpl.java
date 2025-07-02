package com.titanaxis.repository.impl;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioRepositoryImpl implements UsuarioRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository = new AuditoriaRepositoryImpl();

    @Override
    public Usuario save(Usuario usuario) {
        logger.warning("Método save de Usuario sem auditoria foi chamado.");
        boolean isUpdate = usuario.getId() != 0;
        return isUpdate ? update(usuario) : insert(usuario);
    }

    @Override
    public Usuario save(Usuario usuario, Usuario ator) {
        boolean isUpdate = usuario.getId() != 0;
        Usuario usuarioSalvo = isUpdate ? update(usuario) : insert(usuario);

        if (usuarioSalvo != null && ator != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            String detalhes = String.format("Usuário '%s' (ID: %d) foi %s. Nível de Acesso: %s.",
                    usuarioSalvo.getNomeUsuario(), usuarioSalvo.getId(), usuarioSalvo.getNivelAcesso().getNome());
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Usuário", detalhes);
        }
        return usuarioSalvo;
    }

    private Usuario insert(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome_usuario, senha_hash, nivel_acesso) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNomeUsuario());
            ps.setString(2, usuario.getSenhaHash());
            ps.setString(3, usuario.getNivelAcesso().getNome());
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setId(rs.getInt(1));
                        return usuario;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao inserir usuário: " + usuario.getNomeUsuario(), e);
        }
        return null;
    }

    private Usuario update(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome_usuario = ?, senha_hash = ?, nivel_acesso = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNomeUsuario());
            ps.setString(2, usuario.getSenhaHash());
            ps.setString(3, usuario.getNivelAcesso().getNome());
            ps.setInt(4, usuario.getId());
            if (ps.executeUpdate() > 0) {
                return usuario;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar usuário ID: " + usuario.getId(), e);
        }
        return null;
    }

    @Override
    public void deleteById(Integer id) {
        logger.warning("Método deleteById de Usuario sem auditoria foi chamado.");
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao deletar usuário ID: " + id, e);
        }
    }

    @Override
    public void deleteById(Integer id, Usuario ator) {
        findById(id).ifPresent(usuario -> {
            String sql = "DELETE FROM usuarios WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                if(ps.executeUpdate() > 0 && ator != null){
                    String detalhes = String.format("Usuário '%s' (ID: %d) foi eliminado.", usuario.getNomeUsuario(), id);
                    auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO", "Usuário", detalhes);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erro ao deletar usuário ID: " + id, e);
            }
        });
    }

    @Override
    public Optional<Usuario> findById(Integer id) {
        String sql = "SELECT id, nome_usuario, senha_hash, nivel_acesso FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar usuário por ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findByNomeUsuario(String nomeUsuario) {
        String sql = "SELECT id, nome_usuario, senha_hash, nivel_acesso FROM usuarios WHERE nome_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar usuário por nome: " + nomeUsuario, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nome_usuario, senha_hash, nivel_acesso FROM usuarios ORDER BY nome_usuario";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapRowToUsuario(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar usuários.", e);
        }
        return usuarios;
    }

    @Override
    public List<Usuario> findByNomeContaining(String nome) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nome_usuario, senha_hash, nivel_acesso FROM usuarios WHERE LOWER(nome_usuario) LIKE LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nome + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapRowToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar usuários por nome contendo: " + nome, e);
        }
        return usuarios;
    }

    private Usuario mapRowToUsuario(ResultSet rs) throws SQLException {
        NivelAcesso nivel = NivelAcesso.valueOf(rs.getString("nivel_acesso").toUpperCase());

        return new Usuario(
                rs.getInt("id"),
                rs.getString("nome_usuario"),
                rs.getString("senha_hash"),
                nivel
        );
    }
}