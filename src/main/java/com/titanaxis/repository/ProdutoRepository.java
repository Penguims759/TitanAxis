package com.titanaxis.repository;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends Repository<Produto, Integer> {
    List<Produto> findByNomeContaining(String nome);
    Optional<Produto> findByNome(String nome);
    List<Produto> findAllIncludingInactive();
    List<Lote> findLotesByProdutoId(int produtoId);
    Optional<Lote> findLoteById(int loteId);
    Lote saveLote(Lote lote, Usuario ator, EntityManager em);
    void updateStatusAtivo(int produtoId, boolean ativo, Usuario ator, EntityManager em);
    void deleteLoteById(int loteId, Usuario ator, EntityManager em);
}