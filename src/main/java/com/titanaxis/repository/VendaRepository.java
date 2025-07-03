package com.titanaxis.repository;

import com.titanaxis.model.Venda;
// O import do EntityManager e Usuario já não são necessários aqui,
// pois são herdados da interface Repository.

public interface VendaRepository extends Repository<Venda, Integer> {
    // A interface pode ficar vazia por agora,
    // pois todos os métodos necessários (save, deleteById, findById, findAll)
    // já estão definidos na interface genérica Repository.
    // No futuro, se precisar de um método específico para Vendas (ex: findByCliente),
    // ele seria adicionado aqui.
}