package com.titanaxis.repository.impl;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ProdutoRepositoryImpl implements ProdutoRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository;

    public ProdutoRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    // A lógica dos métodos de LEITURA (findAll, findById, etc.) permanece a mesma,
    // pois eles abrem e fecham as suas próprias conexões e não precisam de partilhar transações.
    @Override
    public List<Produto> findAll() {
        return findProductsByStatus(true);
    }

    @Override
    public List<Produto> findAllIncludingInactive() {
        return findProductsByStatus(false);
    }

    private List<Produto> findProductsByStatus(boolean onlyActive) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT DISTINCT p FROM Produto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.lotes";
            if (onlyActive) {
                jpql += " WHERE p.ativo = true";
            }
            jpql += " ORDER BY p.nome";
            return em.createQuery(jpql, Produto.class).getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // --- MÉTODOS DE ESCRITA REFATORADOS ---

    @Override
    public Produto save(Produto produto, Usuario ator, EntityManager em) {
        Produto produtoSalvo = em.merge(produto);
        // A lógica de auditoria acontece dentro da mesma transação.
        if (ator != null) {
            // (A lógica detalhada de auditoria para comparação de campos antigos/novos seria adicionada aqui)
            String acao = produto.getId() != 0 ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            String detalhes = String.format("Produto '%s' (ID: %d) foi %s.",
                    produtoSalvo.getNome(), produtoSalvo.getId(), acao.toLowerCase());
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", detalhes);
        }
        return produtoSalvo;
    }

    @Override
    public Lote saveLote(Lote lote, Usuario ator, EntityManager em) {
        boolean isUpdate = lote.getId() != 0;
        Lote loteSalvo = em.merge(lote);
        if (ator != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO DE LOTE" : "ENTRADA DE LOTE";
            String detalhes = String.format("Ação no produto '%s'. Lote: '%s' (ID: %d), Qtd: %d.",
                    loteSalvo.getProduto().getNome(), loteSalvo.getNumeroLote(), loteSalvo.getId(), loteSalvo.getQuantidade());
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Estoque", detalhes);
        }
        return loteSalvo;
    }

    @Override
    public boolean updateStatusAtivo(int produtoId, boolean ativo, Usuario ator, EntityManager em) {
        Produto produto = em.find(Produto.class, produtoId);
        if (produto != null) {
            produto.setAtivo(ativo);
            if (ator != null) {
                String acao = ativo ? "REATIVAÇÃO" : "INATIVAÇÃO";
                String detalhes = String.format("Produto '%s' (ID: %d) foi %s.",
                        produto.getNome(), produto.getId(), ativo ? "reativado" : "inativado");
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", detalhes);
            }
            return true;
        }
        return false;
    }

    @Override
    public void deleteLoteById(int loteId, Usuario ator, EntityManager em) {
        Lote lote = em.find(Lote.class, loteId);
        if (lote != null) {
            if (ator != null) {
                String detalhes = String.format("Lote '%s' (ID: %d) do produto '%s' foi removido.",
                        lote.getNumeroLote(), loteId, lote.getProduto().getNome());
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO DE LOTE", "Estoque", detalhes);
            }
            em.remove(lote);
        }
    }

    @Override
    public void deleteById(Integer id, Usuario ator, EntityManager em) {
        Produto produto = em.find(Produto.class, id);
        if (produto != null) {
            if (ator != null) {
                String detalhes = String.format("Produto '%s' (ID: %d) foi eliminado.", produto.getNome(), id);
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO FÍSICA", "Produto", detalhes);
            }
            em.remove(produto);
        }
    }

    // --- Métodos de Leitura (Finders) ---
    // (findAll, findAllIncludingInactive e findProductsByStatus já estão acima)

    @Override
    public Optional<Lote> findLoteById(int loteId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Lote.class, loteId));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Optional<Produto> findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Produto> query = em.createQuery(
                    "SELECT p FROM Produto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.lotes WHERE p.id = :id", Produto.class);
            query.setParameter("id", id);
            return Optional.ofNullable(query.getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // ... (outros finders como findByNome, findByNomeContaining, findLotesByProdutoId permanecem iguais)
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

    @Override
    public List<Lote> findLotesByProdutoId(int produtoId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Lote> query = em.createQuery(
                    "SELECT l FROM Lote l WHERE l.produto.id = :produtoId ORDER BY l.dataValidade ASC", Lote.class);
            query.setParameter("produtoId", produtoId);
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // --- Métodos de compatibilidade ---
    @Override
    public Produto save(Produto produto) {
        throw new UnsupportedOperationException("Use o método save que aceita um EntityManager.");
    }
    @Override
    public void deleteById(Integer id) {
        throw new UnsupportedOperationException("Use o método deleteById que aceita um EntityManager.");
    }
}