package com.titanaxis.repository.impl;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class ProdutoRepositoryImpl implements ProdutoRepository {
    private final AuditoriaRepository auditoriaRepository;

    public ProdutoRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Produto save(Produto produto, Usuario ator, EntityManager em) {
        Produto produtoSalvo = em.merge(produto);
        if (ator != null) {
            String acao = produto.getId() != 0 ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", "Produto " + produtoSalvo.getNome() + " salvo.");
        }
        return produtoSalvo;
    }

    @Override
    public Lote saveLote(Lote lote, Usuario ator, EntityManager em) {
        Lote loteSalvo = em.merge(lote);
        if (ator != null) {
            String acao = lote.getId() != 0 ? "ATUALIZAÇÃO DE LOTE" : "ENTRADA DE LOTE";
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Estoque", "Lote " + loteSalvo.getNumeroLote() + " salvo.");
        }
        return loteSalvo;
    }

    @Override
    public void updateStatusAtivo(int produtoId, boolean ativo, Usuario ator, EntityManager em) {
        Produto produto = em.find(Produto.class, produtoId);
        if (produto != null) {
            produto.setAtivo(ativo);
            if (ator != null) {
                String acao = ativo ? "REATIVAÇÃO" : "INATIVAÇÃO";
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", "Status do produto " + produto.getNome() + " alterado.");
            }
        }
    }

    @Override
    public void deleteById(Integer id, Usuario ator, EntityManager em) {
        Produto produto = em.find(Produto.class, id);
        if (produto != null) {
            if (ator != null) {
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO FÍSICA", "Produto", "Produto " + produto.getNome() + " eliminado.");
            }
            em.remove(produto);
        }
    }

    @Override
    public void deleteLoteById(int loteId, Usuario ator, EntityManager em) {
        Lote lote = em.find(Lote.class, loteId);
        if (lote != null) {
            if (ator != null) {
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO DE LOTE", "Estoque", "Lote " + lote.getNumeroLote() + " removido.");
            }
            em.remove(lote);
        }
    }

    @Override
    public List<Produto> findAll() { return findProductsByStatus(true); }
    @Override
    public List<Produto> findAllIncludingInactive() { return findProductsByStatus(false); }

    private List<Produto> findProductsByStatus(boolean onlyActive) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT DISTINCT p FROM Produto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.lotes";
            if (onlyActive) jpql += " WHERE p.ativo = true";
            jpql += " ORDER BY p.nome";
            return em.createQuery(jpql, Produto.class).getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Optional<Produto> findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Produto> query = em.createQuery("SELECT p FROM Produto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.lotes WHERE p.id = :id", Produto.class);
            query.setParameter("id", id);
            return Optional.ofNullable(query.getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Optional<Lote> findLoteById(int loteId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Lote.class, loteId));
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Lote> findLotesByProdutoId(int produtoId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Lote> query = em.createQuery("SELECT l FROM Lote l WHERE l.produto.id = :produtoId ORDER BY l.dataValidade ASC", Lote.class);
            query.setParameter("produtoId", produtoId);
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Optional<Produto> findByNome(String nome) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Produto> query = em.createQuery("SELECT p FROM Produto p WHERE p.nome = :nome", Produto.class);
            query.setParameter("nome", nome);
            return Optional.of(query.getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Produto> findByNomeContaining(String termo) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Produto> query = em.createQuery("SELECT p FROM Produto p WHERE LOWER(p.nome) LIKE LOWER(:termo)", Produto.class);
            query.setParameter("termo", "%" + termo + "%");
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}