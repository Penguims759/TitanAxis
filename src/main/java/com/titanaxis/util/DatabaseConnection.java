package com.titanaxis.util;

import com.titanaxis.model.NivelAcesso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:titanaxis.db";
    private static final Logger logger = AppLogger.getLogger();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Tabela de usuários
            stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome_usuario TEXT NOT NULL UNIQUE," +
                    "senha_hash TEXT NOT NULL," +
                    "nivel_acesso TEXT NOT NULL DEFAULT 'padrao'" +
                    ")");
            logger.info("Tabela 'usuarios' verificada/criada.");

            // Tabela de categorias
            stmt.execute("CREATE TABLE IF NOT EXISTS categorias (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome TEXT NOT NULL UNIQUE" +
                    ")");
            logger.info("Tabela 'categorias' verificada/criada.");

            // Tabela de produtos com a nova coluna 'ativo'
            stmt.execute("CREATE TABLE IF NOT EXISTS produtos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome TEXT NOT NULL," +
                    "descricao TEXT," +
                    "preco REAL NOT NULL," +
                    "categoria_id INTEGER," +
                    "ativo INTEGER NOT NULL DEFAULT 1," +
                    "data_adicao TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "data_ultima_atualizacao TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL" +
                    ")");
            logger.info("Tabela 'produtos' refatorada com sucesso.");

            // Tabela de lotes em estoque
            stmt.execute("CREATE TABLE IF NOT EXISTS estoque_lotes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "produto_id INTEGER NOT NULL," +
                    "numero_lote TEXT NOT NULL," +
                    "quantidade INTEGER NOT NULL," +
                    "data_validade TEXT," +
                    "data_entrada TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE," +
                    "UNIQUE(produto_id, numero_lote)" +
                    ")");
            logger.info("Tabela 'estoque_lotes' verificada/criada.");

            // Trigger para produtos
            stmt.execute("CREATE TRIGGER IF NOT EXISTS trg_produtos_update " +
                    "AFTER UPDATE ON produtos " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "UPDATE produtos SET data_ultima_atualizacao = CURRENT_TIMESTAMP WHERE id = OLD.id;" +
                    "END;");
            logger.info("Trigger 'trg_produtos_update' verificada/criada.");

            // Tabela de movimentos de estoque
            stmt.execute("CREATE TABLE IF NOT EXISTS movimentos_estoque (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "produto_id INTEGER NOT NULL," +
                    "lote_id INTEGER," +
                    "tipo_movimento TEXT NOT NULL," +
                    "quantidade INTEGER NOT NULL," +
                    "data_movimento TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "usuario_id INTEGER," +
                    "FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (lote_id) REFERENCES estoque_lotes(id) ON DELETE SET NULL," +
                    "FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL" +
                    ")");
            logger.info("Tabela 'movimentos_estoque' verificada/criada.");

            // Tabela de clientes
            stmt.execute("CREATE TABLE IF NOT EXISTS clientes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome TEXT NOT NULL," +
                    "contato TEXT," +
                    "endereco TEXT" +
                    ")");
            logger.info("Tabela 'clientes' verificada/criada.");

            // Tabela de vendas
            stmt.execute("CREATE TABLE IF NOT EXISTS vendas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "cliente_id INTEGER," +
                    "usuario_id INTEGER," +
                    "data_venda TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "valor_total REAL NOT NULL," +
                    "FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL," +
                    "FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL" +
                    ")");
            logger.info("Tabela 'vendas' verificada/criada.");

            // Tabela de itens de venda
            stmt.execute("CREATE TABLE IF NOT EXISTS venda_itens (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "venda_id INTEGER NOT NULL," +
                    "produto_id INTEGER NOT NULL," +
                    "lote_id INTEGER," +
                    "quantidade INTEGER NOT NULL," +
                    "preco_unitario REAL NOT NULL," +
                    "FOREIGN KEY (venda_id) REFERENCES vendas(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE RESTRICT," +
                    "FOREIGN KEY (lote_id) REFERENCES estoque_lotes(id) ON DELETE RESTRICT" +
                    ")");
            logger.info("Tabela 'venda_itens' verificada/criada.");

            // Tabela de auditoria
            stmt.execute("CREATE TABLE IF NOT EXISTS auditoria_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "data_evento TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "usuario_id INTEGER," +
                    "usuario_nome TEXT," +
                    "acao TEXT NOT NULL," +
                    "entidade TEXT NOT NULL," +
                    "detalhes TEXT" +
                    ")");
            logger.info("Tabela 'auditoria_logs' verificada/criada.");


            logger.info("Banco de dados inicializado com sucesso!");

            if (!userExists("admin")) {
                String adminPassword = "admin";
                String hashedAdminPassword = PasswordUtil.hashPassword(adminPassword);
                String insertSql = "INSERT INTO usuarios (nome_usuario, senha_hash, nivel_acesso) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, hashedAdminPassword);
                    ps.setString(3, NivelAcesso.ADMIN.getNome());
                    ps.executeUpdate();
                    logger.info("Usuário 'admin' criado com senha inicial.");
                }
            }
            if (!categoryExists("Geral")) {
                String insertCategorySql = "INSERT INTO categorias (nome) VALUES (?)";
                try (PreparedStatement ps = conn.prepareStatement(insertCategorySql)) {
                    ps.setString(1, "Geral");
                    ps.executeUpdate();
                    logger.info("Categoria 'Geral' criada.");
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao inicializar o banco de dados: " + e.getMessage(), e);
            throw new RuntimeException("Falha crítica ao inicializar a base de dados.", e);
        }
    }

    private static boolean userExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE nome_usuario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static boolean categoryExists(String categoryName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categorias WHERE nome = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}