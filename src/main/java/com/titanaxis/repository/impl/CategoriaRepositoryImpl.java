package com.titanaxis.repository.impl;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class CategoriaRepositoryImpl implements CategoriaRepository {
    private final AuditoriaRepository auditoriaRepository;

    public CategoriaRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Categoria save(Categoria categoria, Usuario usuarioLogado, EntityManager em) {
        boolean isUpdate = categoria.getId() != 0;
        Categoria categoriaSalva = em.merge(categoria);

        if (usuarioLogado != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            String detalhes = String.format("Categoria '%s' (ID: %d) foi %s.",
                    categoriaSalva.getNome(), categoriaSalva.getId(), isUpdate ? "atualizada" : "criada");
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), acao, "Categoria", detalhes);
        }
        return categoriaSalva;
    }

    @Override
    public void deleteById(Integer id, Usuario usuarioLogado, EntityManager em) {
        Categoria categoria = em.find(Categoria.class, id);
        if (categoria != null) {
            if (usuarioLogado != null) {
                String detalhes = String.format("Categoria '%s' (ID: %d) foi eliminada.", categoria.getNome(), id);
                auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), "EXCLUSÃO", "Categoria", detalhes);
            }
            em.remove(categoria);
        }
    }

    @Override
    public Optional<Categoria> findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Categoria.class, id));
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Optional<Categoria> findByNome(String nome) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Categoria> query = em.createQuery("SELECT c FROM Categoria c WHERE c.nome = :nome", Categoria.class);
            query.setParameter("nome", nome);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Categoria> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Categoria c ORDER BY c.nome", Categoria.class).getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Categoria> findAllWithProductCount() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT new com.titanaxis.model.Categoria(c.id, c.nome, SIZE(c.produtos)) " +
                    "FROM Categoria c ORDER BY c.nome";
            return em.createQuery(jpql, Categoria.class).getResultList();
        } finally {
            if (em.isOpen()) em.close();
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
            if (em.isOpen()) em.close();
        }
    }
}