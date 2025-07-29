package com.titanaxis.util;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;

public class DatabaseConnection {
    private static final Logger logger = AppLogger.getLogger();
    private static final Properties properties = new Properties();
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                String errorMessage = "Ficheiro de configuração 'config.properties' não encontrado no classpath.";
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            properties.load(input);
            URL = properties.getProperty("database.url");
            USER = properties.getProperty("database.user");
            PASSWORD = properties.getProperty("database.password");
            logger.info("Configuração da base de dados carregada com sucesso.");
        } catch (IOException ex) {
            logger.error("Erro ao carregar o ficheiro de configuração.", ex);
            throw new RuntimeException("Falha crítica ao ler a configuração da base de dados.", ex);
        }
    }

    public static void initializeDatabase() {
        Flyway flyway = Flyway.configure()
                .dataSource(URL, USER, PASSWORD)
                .locations("classpath:db/migration")
                .load();

        try {
            logger.info("A verificar e executar migrações da base de dados com o Flyway...");
            flyway.migrate();
            logger.info("Migrações da base de dados concluídas com sucesso!");
            ensureInitialDataExists();
        } catch (Exception e) {
            logger.error("Erro ao executar as migrações do Flyway: " + e.getMessage(), e);
            throw new RuntimeException("Falha crítica ao migrar a base de dados.", e);
        }
    }

    private static void ensureInitialDataExists() {
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();
            em.getTransaction().begin();

            // Verificar se o utilizador 'admin' existe usando JPA
            TypedQuery<Long> userQuery = em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.nomeUsuario = :nome", Long.class);
            userQuery.setParameter("nome", "admin");
            if (userQuery.getSingleResult() == 0) {
                String adminPassword = "admin";
                String hashedAdminPassword = PasswordUtil.hashPassword(adminPassword);
                Usuario adminUser = new Usuario("admin", hashedAdminPassword, NivelAcesso.ADMIN);
                em.persist(adminUser);
                logger.info("Utilizador 'admin' padrão criado com sucesso.");
            }

            // Verificar se a categoria 'Geral' existe usando JPA
            TypedQuery<Long> categoryQuery = em.createQuery("SELECT COUNT(c) FROM Categoria c WHERE c.nome = :nome", Long.class);
            categoryQuery.setParameter("nome", "Geral");
            if (categoryQuery.getSingleResult() == 0) {
                Categoria defaultCategory = new Categoria("Geral");
                em.persist(defaultCategory);
                logger.info("Categoria 'Geral' padrão criada com sucesso.");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Erro ao verificar ou inserir dados iniciais com JPA.", e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}