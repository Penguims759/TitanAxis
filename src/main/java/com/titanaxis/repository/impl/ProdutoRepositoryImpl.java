package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.util.I18n; // Importado
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class ProdutoRepositoryImpl implements ProdutoRepository {
    private final AuditoriaRepository auditoriaRepository;

    @Inject
    public ProdutoRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Produto save(Produto produto, Usuario ator, EntityManager em) {
        Produto produtoSalvo = em.merge(produto);
        if (ator != null) {
            String acao = produto.getId() != 0 ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            // ALTERADO
            String detalhes = I18n.getString("log.product.saved", produtoSalvo.getNome());
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", produtoSalvo.getId(), detalhes, em);
        }
        return produtoSalvo;
    }

    @Override
    public Lote saveLote(Lote lote, Usuario ator, EntityManager em) {
        Lote loteSalvo = em.merge(lote);
        if (ator != null) {
            String acao = lote.getId() != 0 ? "ATUALIZAÇÃO DE LOTE" : "ENTRADA DE LOTE";
            // ALTERADO
            String detalhes = I18n.getString("log.batch.saved", loteSalvo.getNumeroLote(), lote.getProduto().getNome());
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Estoque", loteSalvo.getProdutoId(), detalhes, em);
        }
        return loteSalvo;
    }

    @Override
    public void updateStatusAtivo(int produtoId, boolean ativo, Usuario ator, EntityManager em) {
        Optional<Produto> produtoOpt = findById(produtoId, em);
        produtoOpt.ifPresent(produto -> {
            produto.setAtivo(ativo);
            if (ator != null) {
                String acao = ativo ? "REATIVAÇÃO" : "INATIVAÇÃO";
                // ALTERADO
                String detalhes = I18n.getString("log.product.statusChanged", produto.getNome());
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", produto.getId(), detalhes, em);
            }
        });
    }

    @Override
    public void deleteById(Integer id, Usuario ator, EntityManager em) {
        Optional<Produto> produtoOpt = findById(id, em);
        produtoOpt.ifPresent(produto -> {
            if (ator != null) {
                // ALTERADO
                String detalhes = I18n.getString("log.product.deleted", produto.getNome());
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO FÍSICA", "Produto", produto.getId(), detalhes, em);
            }
            em.remove(produto);
        });
    }

    @Override
    public void deleteLoteById(int loteId, Usuario ator, EntityManager em) {
        Optional<Lote> loteOpt = findLoteById(loteId, em);
        loteOpt.ifPresent(lote -> {
            if (ator != null) {
                // ALTERADO
                String detalhes = I18n.getString("log.batch.removed", lote.getNumeroLote());
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO DE LOTE", "Estoque", lote.getProdutoId(), detalhes, em);
            }
            em.remove(lote);
        });
    }

    @Override
    public List<Produto> findAll(EntityManager em) {
        return findProductsByStatus(em, true);
    }

    @Override
    public List<Produto> findAllIncludingInactive(EntityManager em) {
        return findProductsByStatus(em, false);
    }

    private List<Produto> findProductsByStatus(EntityManager em, boolean onlyActive) {
        String jpql = "SELECT DISTINCT p FROM Produto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.lotes";
        if (onlyActive) {
            jpql += " WHERE p.ativo = true";
        }
        jpql += " ORDER BY p.nome";
        return em.createQuery(jpql, Produto.class).getResultList();
    }

    @Override
    public Optional<Produto> findById(Integer id, EntityManager em) {
        try {
            TypedQuery<Produto> query = em.createQuery("SELECT p FROM Produto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.lotes WHERE p.id = :id", Produto.class);
            query.setParameter("id", id);
            return Optional.ofNullable(query.getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Lote> findLoteById(int loteId, EntityManager em) {
        TypedQuery<Lote> query = em.createQuery("SELECT l FROM Lote l LEFT JOIN FETCH l.produto WHERE l.id = :loteId", Lote.class);
        query.setParameter("loteId", loteId);
        try {
            return Optional.ofNullable(query.getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Lote> findLotesByProdutoId(int produtoId, EntityManager em) {
        TypedQuery<Lote> query = em.createQuery("SELECT l FROM Lote l LEFT JOIN FETCH l.produto WHERE l.produto.id = :produtoId ORDER BY l.dataValidade ASC", Lote.class);
        query.setParameter("produtoId", produtoId);
        return query.getResultList();
    }

    @Override
    public Optional<Produto> findByNome(String nome, EntityManager em) {
        try {
            TypedQuery<Produto> query = em.createQuery("SELECT p FROM Produto p LEFT JOIN FETCH p.lotes WHERE p.nome = :nome", Produto.class);
            query.setParameter("nome", nome);
            return Optional.of(query.getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Produto> findByNomeContaining(String termo, EntityManager em) {
        TypedQuery<Produto> query = em.createQuery("SELECT p FROM Produto p LEFT JOIN FETCH p.lotes WHERE LOWER(p.nome) LIKE LOWER(:termo)", Produto.class);
        query.setParameter("termo", "%" + termo + "%");
        return query.getResultList();
    }
}