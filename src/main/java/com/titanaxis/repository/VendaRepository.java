package com.titanaxis.repository;

import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public interface VendaRepository extends Repository<Venda, Integer> {

    List<VendaItem> findAllItems(EntityManager em);

    List<Venda> findVendasByClienteId(int clienteId, EntityManager em);

    List<Venda> findVendasBetweenDates(LocalDateTime start, LocalDateTime end, EntityManager em);
}