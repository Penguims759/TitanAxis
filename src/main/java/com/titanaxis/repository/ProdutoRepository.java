package com.titanaxis.repository;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends Repository<Produto, Integer> {
    // ALTERADO: Agora recebem um EntityManager
    List<Produto> findByNomeContaining(String nome, EntityManager em);
    Optional<Produto> findByNome(String nome, EntityManager em);
    List<Produto> findAllIncludingInactive(EntityManager em);
    List<Lote> findLotesByProdutoId(int produtoId, EntityManager em);
    Optional<Lote> findLoteById(int loteId, EntityManager em);
    Lote saveLote(Lote lote, Usuario ator, EntityManager em);
    void updateStatusAtivo(int produtoId, boolean ativo, Usuario ator, EntityManager em);
    void deleteLoteById(int loteId, Usuario ator, EntityManager em);
}