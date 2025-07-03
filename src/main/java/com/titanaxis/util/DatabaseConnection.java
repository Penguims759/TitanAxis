package com.titanaxis.util;

import com.titanaxis.model.NivelAcesso;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger logger = AppLogger.getLogger();
    private static final Properties properties = new Properties();
    private static final String URL;

    // Bloco estático para carregar a configuração do ficheiro .properties
    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                String errorMessage = "Ficheiro de configuração 'config.properties' não encontrado no classpath.";
                logger.severe(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            properties.load(input);
            URL = properties.getProperty("database.url");
            logger.info("Configuração da base de dados carregada com sucesso.");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Erro ao carregar o ficheiro de configuração.", ex);
            throw new RuntimeException("Falha crítica ao ler a configuração da base de dados.", ex);
        }
    }

    /**
     * Obtém uma nova conexão com a base de dados.
     * @return Uma instância de Connection.
     * @throws SQLException se ocorrer um erro ao conectar.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Inicializa a base de dados, executando as migrações pendentes com o Flyway
     * e garantindo que os dados iniciais essenciais existam.
     */
    public static void initializeDatabase() {
        // Configura e executa as migrações do Flyway
        Flyway flyway = Flyway.configure()
                .dataSource(URL, null, null) // Para SQLite, user e password podem ser nulos
                .locations("classpath:db/migration") // Diz ao Flyway onde encontrar os scripts SQL
                .baselineOnMigrate(true) // <<< --- ESTA É A LINHA ADICIONADA
                .load();

        try {
            logger.info("A verificar e executar migrações da base de dados com o Flyway...");
            flyway.migrate();
            logger.info("Migrações da base de dados concluídas com sucesso!");

            // Após garantir que o esquema está atualizado, verifica/insere os dados iniciais.
            ensureInitialDataExists();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao executar as migrações do Flyway: " + e.getMessage(), e);
            throw new RuntimeException("Falha crítica ao migrar a base de dados.", e);
        }
    }

    /**
     * Garante que os dados essenciais para o primeiro arranque (como o utilizador admin) existam.
     * Esta verificação é feita após as migrações do esquema.
     */
    private static void ensureInitialDataExists() {
        try (Connection conn = getConnection()) {
            if (!userExists(conn, "admin")) {
                String adminPassword = "admin";
                String hashedAdminPassword = PasswordUtil.hashPassword(adminPassword);
                String insertSql = "INSERT INTO usuarios (nome_usuario, senha_hash, nivel_acesso) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, hashedAdminPassword);
                    ps.setString(3, NivelAcesso.ADMIN.getNome());
                    ps.executeUpdate();
                    logger.info("Utilizador 'admin' padrão criado com sucesso.");
                }
            }
            if (!categoryExists(conn, "Geral")) {
                String insertCategorySql = "INSERT INTO categorias (nome) VALUES (?)";
                try (PreparedStatement ps = conn.prepareStatement(insertCategorySql)) {
                    ps.setString(1, "Geral");
                    ps.executeUpdate();
                    logger.info("Categoria 'Geral' padrão criada com sucesso.");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao verificar ou inserir dados iniciais.", e);
        }
    }

    private static boolean userExists(Connection conn, String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE nome_usuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static boolean categoryExists(Connection conn, String categoryName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categorias WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}