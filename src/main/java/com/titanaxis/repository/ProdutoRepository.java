// src/main/java/com/titanaxis/repository/ProdutoRepository.java
package com.titanaxis.repository;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends Repository<Produto, Integer> {

    List<Produto> findByNomeContaining(String nome);

    Optional<Produto> findByNome(String nome);

    // NOVO: Método para buscar todos os produtos, incluindo os inativos.
    List<Produto> findAllIncludingInactive();

    // NOVO: Método para atualizar o estado de um produto.
    boolean updateStatusAtivo(int produtoId, boolean ativo);

    // Métodos específicos para Lotes
    List<Lote> findLotesByProdutoId(int produtoId);

    Lote saveLote(Lote lote);

    Optional<Lote> findLoteById(int loteId);

    void deleteLoteById(int loteId);
}