// src/main/java/com/titanaxis/repository/VendaRepository.java
package com.titanaxis.repository;

import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import jakarta.persistence.EntityManager;
import java.util.List;

public interface VendaRepository extends Repository<Venda, Integer> {
    /**
     * Busca todos os itens de venda de todas as vendas registadas.
     * Este método é útil para análises de dados, como encontrar os produtos mais vendidos.
     *
     * @param em O EntityManager da transação atual.
     * @return Uma lista de todos os VendaItem.
     */
    List<VendaItem> findAllItems(EntityManager em);

    // NOVO
    List<Venda> findVendasByClienteId(int clienteId, EntityManager em);
}