package com.titanaxis.repository;

import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public interface VendaRepository extends Repository<Venda, Integer> {

    List<VendaItem> findAllItems(EntityManager em);

    // NOVO: Retorna todos os itens de venda dentro de um período
    List<VendaItem> findVendaItensBetweenDates(LocalDateTime start, LocalDateTime end, EntityManager em);

    List<Venda> findVendasByClienteId(int clienteId, EntityManager em);

    List<Venda> findVendasBetweenDates(LocalDateTime start, LocalDateTime end, EntityManager em);
}