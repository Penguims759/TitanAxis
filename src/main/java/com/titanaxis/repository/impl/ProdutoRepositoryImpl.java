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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProdutoRepositoryImpl implements ProdutoRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository;

    public ProdutoRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    // ... (os métodos save, deleteById, etc. que já corrigimos permanecem iguais) ...

    @Override
    public Produto save(Produto produto, Usuario ator) {
        boolean isUpdate = produto.getId() != 0;
        EntityManager em = JpaUtil.getEntityManager();

        Produto produtoAntigo = null;
        if (isUpdate) {
            produtoAntigo = em.find(Produto.class, produto.getId());
            if(produtoAntigo != null) em.detach(produtoAntigo);
        }

        try {
            em.getTransaction().begin();
            Produto produtoSalvo = em.merge(produto);
            em.getTransaction().commit();

            if (ator != null) {
                String detalhes;
                String acao;
                if (isUpdate && produtoAntigo != null) {
                    acao = "ATUALIZAÇÃO";
                    detalhes = String.format("Produto '%s' (ID: %d) atualizado. Nome: '%s'->'%s', Preço: %.2f->%.2f, Categoria: %s->%s.",
                            produtoAntigo.getNome(), produtoSalvo.getId(),
                            produtoAntigo.getNome(), produtoSalvo.getNome(),
                            produtoAntigo.getPreco(), produtoSalvo.getPreco(),
                            produtoAntigo.getCategoria() != null ? produtoAntigo.getCategoria().getNome() : "N/A",
                            produtoSalvo.getCategoria() != null ? produtoSalvo.getCategoria().getNome() : "N/A");
                } else {
                    acao = "CRIAÇÃO";
                    detalhes = String.format("Produto '%s' (ID: %d) criado.",
                            produtoSalvo.getNome(), produtoSalvo.getId());
                }
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", detalhes);
            }
            return produtoSalvo;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.log(Level.SEVERE, "Erro ao salvar produto: " + e.getMessage(), e);
            return null;
        } finally {
            if (em.isOpen()) em.close();
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
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // CORREÇÃO: Adicionamos 'LEFT JOIN FETCH p.categoria' para carregar a categoria na mesma query.
            // Usamos 'DISTINCT' para evitar produtos duplicados se um produto tiver múltiplos lotes.
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


    @Override
    public boolean updateStatusAtivo(int produtoId, boolean ativo, Usuario ator) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Produto produto = em.find(Produto.class, produtoId);
            if (produto != null) {
                produto.setAtivo(ativo);
                em.merge(produto);
                em.getTransaction().commit();

                if (ator != null) {
                    String acao = ativo ? "REATIVAÇÃO" : "INATIVAÇÃO";
                    String detalhes = String.format("Produto '%s' (ID: %d) foi %s.",
                            produto.getNome(), produto.getId(), ativo ? "reativado" : "inativado");
                    auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Produto", detalhes);
                }
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.log(Level.SEVERE, "Erro ao atualizar status do produto ID: " + produtoId, e);
            return false;
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Optional<Produto> findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // CORREÇÃO: Também usamos JOIN FETCH aqui para garantir que todas as relações sejam carregadas.
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

    // ... (os outros métodos permanecem como estavam na última versão) ...
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
    public void deleteById(Integer id, Usuario ator) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Produto produto = em.find(Produto.class, id);
            if (produto != null) {
                em.remove(produto);
                em.getTransaction().commit();

                if (ator != null) {
                    String detalhes = String.format("Produto '%s' (ID: %d) foi eliminado.", produto.getNome(), id);
                    auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO FÍSICA", "Produto", detalhes);
                }
            } else {
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.log(Level.SEVERE, "Erro ao deletar produto ID: " + id, e);
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

    @Override
    public Lote saveLote(Lote lote, Usuario ator) {
        boolean isUpdate = lote.getId() != 0;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Lote loteSalvo = em.merge(lote);
            em.getTransaction().commit();

            if (ator != null) {
                String acao = isUpdate ? "ATUALIZAÇÃO DE LOTE" : "ENTRADA DE LOTE";
                String detalhes = String.format("Ação no produto '%s' (ID %d). Lote: '%s' (ID: %d), Qtd: %d.",
                        loteSalvo.getProduto().getNome(), loteSalvo.getProduto().getId(), loteSalvo.getNumeroLote(), loteSalvo.getId(), loteSalvo.getQuantidade());
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Estoque", detalhes);
            }
            return loteSalvo;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.log(Level.SEVERE, "Erro ao salvar lote: " + e.getMessage(), e);
            return null;
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
    public void deleteLoteById(int loteId, Usuario ator) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Lote lote = em.find(Lote.class, loteId);
            if (lote != null) {
                String produtoNome = lote.getProduto().getNome();
                int produtoId = lote.getProduto().getId();
                String numeroLote = lote.getNumeroLote();
                int quantidade = lote.getQuantidade();

                em.remove(lote);
                em.getTransaction().commit();

                if (ator != null) {
                    String detalhes = String.format("Lote '%s' (ID: %d) do produto '%s' (ID: %d) foi removido (Qtd: %d).",
                            numeroLote, loteId, produtoNome, produtoId, quantidade);
                    auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO DE LOTE", "Estoque", detalhes);
                }
            } else {
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.log(Level.SEVERE, "Erro ao deletar lote ID: " + loteId, e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Lote saveLote(Lote lote) {
        return saveLote(lote, null);
    }
    @Override
    public Produto save(Produto entity) {
        return save(entity, null);
    }
    @Override
    public void deleteById(Integer id) {
        deleteById(id, null);
    }
}