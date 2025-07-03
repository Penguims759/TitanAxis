package com.titanaxis.repository.impl;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

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
    public Categoria save(Categoria categoria, Usuario usuarioLogado) {
        boolean isUpdate = categoria.getId() != 0;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Categoria categoriaSalva;
            if (isUpdate) {
                categoriaSalva = em.merge(categoria); // 'merge' para atualizar uma entidade existente
            } else {
                em.persist(categoria); // 'persist' para salvar uma nova entidade
                categoriaSalva = categoria;
            }

            em.getTransaction().commit();

            // A lógica de auditoria permanece, mas é executada após a transação ser bem-sucedida
            if (usuarioLogado != null) {
                String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
                String detalhes = String.format("Categoria '%s' (ID: %d) foi %s.",
                        categoriaSalva.getNome(), categoriaSalva.getId(), isUpdate ? "atualizada" : "criada");
                auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), acao, "Categoria", detalhes);
            }
            return categoriaSalva;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao salvar categoria: " + e.getMessage(), e);
            // Poderia ser lançada uma exceção específica da aplicação aqui
            return null;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void deleteById(Integer id, Usuario usuarioLogado) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Categoria categoria = em.find(Categoria.class, id);
            if (categoria != null) {
                em.remove(categoria);
                em.getTransaction().commit();

                // Lógica de auditoria
                if (usuarioLogado != null) {
                    String detalhes = String.format("Categoria '%s' (ID: %d) foi eliminada.", categoria.getNome(), id);
                    auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), "EXCLUSÃO", "Categoria", detalhes);
                }
            } else {
                // Se a categoria não for encontrada, apenas faz rollback da transação vazia
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao deletar categoria ID: " + id, e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Optional<Categoria> findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // O método find é a forma mais simples de buscar uma entidade pela sua chave primária
            return Optional.ofNullable(em.find(Categoria.class, id));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Optional<Categoria> findByNome(String nome) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Usamos uma TypedQuery com JPQL para buscar por um campo que não é a chave primária
            TypedQuery<Categoria> query = em.createQuery("SELECT c FROM Categoria c WHERE c.nome = :nome", Categoria.class);
            query.setParameter("nome", nome);
            // getSingleResult lança uma NoResultException se nada for encontrado, que é apanhada abaixo
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty(); // Retorna um Optional vazio se nenhuma categoria for encontrada
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Categoria> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // JPQL (Java Persistence Query Language) é semelhante a SQL mas opera sobre entidades e os seus campos
            String jpql = "SELECT c FROM Categoria c ORDER BY c.nome";
            return em.createQuery(jpql, Categoria.class).getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Categoria> findAllWithProductCount() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // A query foi atualizada para usar a relação `produtos` mapeada na entidade `Categoria`.
            // `SIZE(c.produtos)` é a forma JPQL de obter o tamanho de uma coleção associada.
            String jpql = "SELECT new com.titanaxis.model.Categoria(c.id, c.nome, SIZE(c.produtos)) " +
                    "FROM Categoria c ORDER BY c.nome";
            return em.createQuery(jpql, Categoria.class).getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Categoria> findByNomeContainingWithProductCount(String termo) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT new com.titanaxis.model.Categoria(c.id, c.nome, SIZE(c.produtos)) " +
                    "FROM Categoria c WHERE LOWER(c.nome) LIKE LOWER(:termo) ORDER BY c.nome";
            TypedQuery<Categoria> query = em.createQuery(jpql, Categoria.class);
            query.setParameter("termo", "%" + termo + "%");
            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    // --- Métodos antigos (deprecated) que delegam para os novos com auditoria ---
    @Override
    public Categoria save(Categoria categoria) {
        logger.warning("O método 'save' sem auditoria foi chamado. A operação será registada sem um ator.");
        return this.save(categoria, null);
    }

    @Override
    public void deleteById(Integer id) {
        logger.warning("O método 'deleteById' sem auditoria foi chamado. A operação será registada sem um ator.");
        this.deleteById(id, null);
    }
}