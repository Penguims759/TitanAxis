// penguims759/titanaxis/Penguims759-TitanAxis-7ba36152a6e3502010a8be48ce02c9ed9fcd8bf0/src/main/java/com/titanaxis/repository/impl/ProdutoRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProdutoRepositoryImpl implements ProdutoRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository;
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProdutoRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Produto save(Produto produto) {
        return this.save(produto, null);
    }

    @Override
    public Produto save(Produto produto, Usuario ator) {
        boolean isUpdate = produto.getId() != 0;
        Produto produtoAntigo = isUpdate ? findById(produto.getId()).orElse(null) : null;
        Produto produtoSalvo = isUpdate ? update(produto) : insert(produto);

        if (produtoSalvo != null && ator != null) {
            String detalhes;
            String acao;
            if (isUpdate && produtoAntigo != null) {
                acao = "ATUALIZAÇÃO";
                detalhes = String.format("Produto '%s' (ID: %d) foi atualizado. Nome: '%s' -> '%s', Preço: %.2f -> %.2f, Categoria ID: %d -> %d.",
                        produtoAntigo.getNome(), produtoSalvo.getId(),
                        produtoAntigo.getNome(), produtoSalvo.getNome(),
                        produtoAntigo.getPreco(), produtoSalvo.getPreco(),
                        produtoAntigo.getCategoriaId(), produtoSalvo.getCategoriaId());
            } else {
                acao = "CRIAÇÃO";
                detalhes = String.format("Produto '%s' (ID: %d) foi criado.",
                        produtoSalvo.getNome(), produtoSalvo.getId());
            }
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", detalhes);
        }
        return produtoSalvo;
    }

    private Produto insert(Produto produto) {
        String sql = "INSERT INTO produtos (nome, descricao, preco, categoria_id, ativo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getDescricao());
            ps.setDouble(3, produto.getPreco());
            ps.setInt(4, produto.getCategoriaId());
            ps.setBoolean(5, produto.isAtivo());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        produto.setId(rs.getInt(1));
                        return produto;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao inserir produto: " + produto.getNome(), e);
        }
        return null;
    }

    private Produto update(Produto produto) {
        String sql = "UPDATE produtos SET nome = ?, descricao = ?, preco = ?, categoria_id = ?, ativo = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getDescricao());
            ps.setDouble(3, produto.getPreco());
            ps.setInt(4, produto.getCategoriaId());
            ps.setBoolean(5, produto.isAtivo());
            ps.setInt(6, produto.getId());

            if (ps.executeUpdate() > 0) {
                return produto;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar produto ID: " + produto.getId(), e);
        }
        return null;
    }

    @Override
    public boolean updateStatusAtivo(int produtoId, boolean ativo, Usuario ator) {
        String sql = "UPDATE produtos SET ativo = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, ativo);
            ps.setInt(2, produtoId);
            boolean sucesso = ps.executeUpdate() > 0;
            if (sucesso && ator != null) {
                findById(produtoId).ifPresent(produto -> {
                    String acao = ativo ? "REATIVAÇÃO" : "INATIVAÇÃO";
                    String detalhes = String.format("Produto '%s' (ID: %d) foi %s.",
                            produto.getNome(), produto.getId(), ativo ? "reativado" : "inativado");
                    auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", detalhes);
                });
            }
            return sucesso;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar status do produto ID: " + produtoId, e);
            return false;
        }
    }

    @Override
    public List<Produto> findAll() {
        return findProductsByStatus(true);
    }

    @Override
    public List<Produto> findAllIncludingInactive() {
        return findProductsByStatus(false);
    }

    private List<Produto> findProductsByStatus(boolean onlyActive) {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT p.*, c.nome AS nome_categoria, " +
                "COALESCE((SELECT SUM(el.quantidade) FROM estoque_lotes el WHERE el.produto_id = p.id), 0) as quantidade_total " +
                "FROM produtos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                (onlyActive ? "WHERE p.ativo = 1 " : "") +
                "ORDER BY p.nome";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Produto produto = mapRowToProduto(rs);
                produto.setQuantidadeTotal(rs.getInt("quantidade_total"));
                produtos.add(produto);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar produtos.", e);
        }
        return produtos;
    }

    @Override
    public List<Lote> findLotesByProdutoId(int produtoId) {
        List<Lote> lotes = new ArrayList<>();
        String sql = "SELECT * FROM estoque_lotes WHERE produto_id = ? ORDER BY data_validade ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, produtoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lotes.add(mapRowToLote(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar lotes para o produto ID: " + produtoId, e);
        }
        return lotes;
    }

    @Override
    public Lote saveLote(Lote lote) {
        return this.saveLote(lote, null);
    }

    @Override
    public Lote saveLote(Lote lote, Usuario ator) {
        boolean isUpdate = lote.getId() != 0;
        String sqlLote = isUpdate
                ? "UPDATE estoque_lotes SET produto_id = ?, numero_lote = ?, quantidade = ?, data_validade = ? WHERE id = ?"
                : "INSERT INTO estoque_lotes (produto_id, numero_lote, quantidade, data_validade) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psLote = conn.prepareStatement(sqlLote, Statement.RETURN_GENERATED_KEYS)) {
                psLote.setInt(1, lote.getProdutoId());
                psLote.setString(2, lote.getNumeroLote());
                psLote.setInt(3, lote.getQuantidade());
                psLote.setString(4, lote.getDataValidade() != null ? lote.getDataValidade().format(DB_DATE_FORMATTER) : null);
                if (isUpdate) {
                    psLote.setInt(5, lote.getId());
                }

                if (psLote.executeUpdate() == 0) throw new SQLException("Falha ao salvar o lote.");

                if (!isUpdate) {
                    try (ResultSet generatedKeys = psLote.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            lote.setId(generatedKeys.getInt(1));
                        }
                    }
                }
            }

            if (ator != null) {
                findById(lote.getProdutoId()).ifPresent(produto -> {
                    String acao = isUpdate ? "ATUALIZAÇÃO DE LOTE" : "ENTRADA DE LOTE";
                    String detalhes = String.format("Ação no produto '%s' (ID %d). Lote: '%s' (ID: %d), Qtd: %d.",
                            produto.getNome(), lote.getProdutoId(), lote.getNumeroLote(), lote.getId(), lote.getQuantidade());
                    auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Estoque", detalhes);
                });
            }

            conn.commit();
            return lote;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro na transação ao salvar lote. Rollback acionado.", e);
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { logger.log(Level.SEVERE, "Falha no rollback.", ex); } }
            return null;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { logger.log(Level.SEVERE, "Falha ao fechar conexão.", e); } }
        }
    }

    private Produto mapRowToProduto(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getDouble("preco"),
                rs.getInt("categoria_id"),
                rs.getString("nome_categoria"),
                rs.getBoolean("ativo")
        );
    }

    private Lote mapRowToLote(ResultSet rs) throws SQLException {
        String dataValidadeStr = rs.getString("data_validade");
        LocalDate dataValidade = (dataValidadeStr != null && !dataValidadeStr.isEmpty()) ? LocalDate.parse(dataValidadeStr, DB_DATE_FORMATTER) : null;

        return new Lote(
                rs.getInt("id"),
                rs.getInt("produto_id"),
                rs.getString("numero_lote"),
                rs.getInt("quantidade"),
                dataValidade
        );
    }

    @Override
    public Optional<Produto> findById(Integer id) {
        String sql = "SELECT p.*, c.nome AS nome_categoria, " +
                "COALESCE((SELECT SUM(el.quantidade) FROM estoque_lotes el WHERE el.produto_id = p.id), 0) as quantidade_total " +
                "FROM produtos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Produto produto = mapRowToProduto(rs);
                    produto.setQuantidadeTotal(rs.getInt("quantidade_total"));
                    return Optional.of(produto);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar produto por ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Produto> findByNome(String nome) {
        String sql = "SELECT p.*, c.nome AS nome_categoria, " +
                "COALESCE((SELECT SUM(el.quantidade) FROM estoque_lotes el WHERE el.produto_id = p.id), 0) as quantidade_total " +
                "FROM produtos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "WHERE p.nome = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Produto produto = mapRowToProduto(rs);
                    produto.setQuantidadeTotal(rs.getInt("quantidade_total"));
                    return Optional.of(produto);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar produto por nome: " + nome, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Produto> findByNomeContaining(String termo) {
        return this.findAll().stream()
                .filter(p -> p.getNome().toLowerCase().contains(termo.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer id) {
        this.deleteById(id, null);
    }

    @Override
    public void deleteById(Integer id, Usuario ator) {
        findById(id).ifPresent(produto -> {
            String sql = "DELETE FROM produtos WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                if (ps.executeUpdate() > 0 && ator != null) {
                    String detalhes = String.format("Produto '%s' (ID: %d) e todos os seus lotes foram eliminados (AÇÃO NÃO RECOMENDADA).", produto.getNome(), id);
                    auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO FÍSICA", "Produto", detalhes);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erro ao deletar produto ID: " + id, e);
            }
        });
    }

    @Override
    public Optional<Lote> findLoteById(int loteId) {
        String sql = "SELECT * FROM estoque_lotes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToLote(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar lote por ID: " + loteId, e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteLoteById(int loteId, Usuario ator) {
        findLoteById(loteId).ifPresent(lote -> {
            String sql = "DELETE FROM estoque_lotes WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, loteId);
                if (ps.executeUpdate() > 0 && ator != null) {
                    findById(lote.getProdutoId()).ifPresent(produto -> {
                        String detalhes = String.format("Lote '%s' (ID: %d) do produto '%s' (ID: %d) foi removido (Qtd: %d).",
                                lote.getNumeroLote(), loteId, produto.getNome(), lote.getProdutoId(), lote.getQuantidade());
                        auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO DE LOTE", "Estoque", detalhes);
                    });
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erro ao deletar lote ID: " + loteId, e);
            }
        });
    }
}