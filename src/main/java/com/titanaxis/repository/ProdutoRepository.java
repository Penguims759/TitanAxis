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

    // Métodos de escrita que aceitam um EntityManager para transações controladas pelo serviço
    Produto save(Produto produto, Usuario ator, EntityManager em);
    Lote saveLote(Lote lote, Usuario ator, EntityManager em);
    boolean updateStatusAtivo(int produtoId, boolean ativo, Usuario ator, EntityManager em);
    void deleteById(Integer id, Usuario ator, EntityManager em);
    void deleteLoteById(int loteId, Usuario ator, EntityManager em);
}