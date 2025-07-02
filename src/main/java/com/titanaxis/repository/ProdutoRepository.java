package com.titanaxis.repository;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario; // NOVO IMPORT
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends Repository<Produto, Integer> {

    List<Produto> findByNomeContaining(String nome);
    Optional<Produto> findByNome(String nome);
    List<Produto> findAllIncludingInactive();

    // ALTERAÇÃO: Método para atualizar o estado de um produto com auditoria
    boolean updateStatusAtivo(int produtoId, boolean ativo, Usuario ator);

    // Métodos específicos para Lotes
    List<Lote> findLotesByProdutoId(int produtoId);
    Lote saveLote(Lote lote); // Método antigo sem auditoria
    Lote saveLote(Lote lote, Usuario ator); // NOVO com auditoria
    Optional<Lote> findLoteById(int loteId);
    void deleteLoteById(int loteId, Usuario ator); // NOVO com auditoria
}