// penguims759/titanaxis/Penguims759-TitanAxis-7ba36152a6e3502010a8be48ce02c9ed9fcd8bf0/src/main/java/com/titanaxis/repository/impl/CategoriaRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoriaRepositoryImpl implements CategoriaRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository;

    public CategoriaRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Categoria save(Categoria categoria) {
        // Este método agora existe por compatibilidade com a interface, mas não deve ser usado para operações auditáveis.
        logger.warning("Método save sem auditoria foi chamado. A operação não será registada.");
        boolean isUpdate = categoria.getId() != 0;
        return isUpdate ? update(categoria) : insert(categoria);
    }

    // ALTERAÇÃO: Implementação do método que aceita o utilizador logado.
    @Override
    public Categoria save(Categoria categoria, Usuario usuarioLogado) {
        boolean isUpdate = categoria.getId() != 0;
        Categoria categoriaSalva = isUpdate ? update(categoria) : insert(categoria);

        if (categoriaSalva != null && usuarioLogado != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            String detalhes = String.format("Categoria '%s' (ID: %d) foi %s.",
                    categoriaSalva.getNome(), categoriaSalva.getId(), isUpdate ? "atualizada" : "criada");
            // ALTERAÇÃO: Usa os dados dinâmicos do utilizador logado
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), acao, "Categoria", detalhes);
        }
        return categoriaSalva;
    }

    private Categoria insert(Categoria categoria) {
        String sql = "INSERT INTO categorias (nome) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoria.getNome());
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        categoria.setId(rs.getInt(1));
                        return categoria;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao inserir categoria: " + categoria.getNome(), e);
        }
        return null;
    }

    private Categoria update(Categoria categoria) {
        String sql = "UPDATE categorias SET nome = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNome());
            ps.setInt(2, categoria.getId());
            if (ps.executeUpdate() > 0) {
                return categoria;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar categoria ID: " + categoria.getId(), e);
        }
        return null;
    }

    @Override
    public void deleteById(Integer id) {
        // Método por compatibilidade, não deve ser usado para operações auditáveis.
        logger.warning("Método deleteById sem auditoria foi chamado. A operação não será registada.");
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao deletar categoria ID: " + id, e);
        }
    }

    // ALTERAÇÃO: Implementação do método que aceita o utilizador logado.
    @Override
    public void deleteById(Integer id, Usuario usuarioLogado) {
        findById(id).ifPresent(categoria -> {
            String sql = "DELETE FROM categorias WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                if (ps.executeUpdate() > 0 && usuarioLogado != null) {
                    String detalhes = String.format("Categoria '%s' (ID: %d) foi eliminada.", categoria.getNome(), id);
                    // ALTERAÇÃO: Usa os dados dinâmicos do utilizador logado
                    auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), "EXCLUSÃO", "Categoria", detalhes);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erro ao deletar categoria ID: " + id, e);
            }
        });
    }

    @Override
    public Optional<Categoria> findById(Integer id) {
        String sql = "SELECT id, nome FROM categorias WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Categoria(rs.getInt("id"), rs.getString("nome")));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar categoria por ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Categoria> findByNome(String nome) {
        String sql = "SELECT id, nome FROM categorias WHERE nome = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Categoria(rs.getInt("id"), rs.getString("nome")));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar categoria por nome: " + nome, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Categoria> findAll() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id, nome FROM categorias ORDER BY nome";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(new Categoria(rs.getInt("id"), rs.getString("nome")));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar categorias", e);
        }
        return categorias;
    }

    @Override
    public List<Categoria> findAllWithProductCount() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT c.id, c.nome, COUNT(p.id) AS total_produtos " +
                "FROM categorias c LEFT JOIN produtos p ON c.id = p.categoria_id " +
                "GROUP BY c.id, c.nome ORDER BY c.nome";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(new Categoria(rs.getInt("id"), rs.getString("nome"), rs.getInt("total_produtos")));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar categorias com contagem de produtos.", e);
        }
        return categorias;
    }

    @Override
    public List<Categoria> findByNomeContainingWithProductCount(String termo) {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT c.id, c.nome, COUNT(p.id) AS total_produtos " +
                "FROM categorias c LEFT JOIN produtos p ON c.id = p.categoria_id " +
                "WHERE LOWER(c.nome) LIKE LOWER(?) " +
                "GROUP BY c.id, c.nome ORDER BY c.nome";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + termo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categorias.add(new Categoria(rs.getInt("id"), rs.getString("nome"), rs.getInt("total_produtos")));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar categorias por nome com contagem.", e);
        }
        return categorias;
    }
}