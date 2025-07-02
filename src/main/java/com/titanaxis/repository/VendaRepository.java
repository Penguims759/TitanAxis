// src/main/java/com/titanaxis/repository/VendaRepository.java
package com.titanaxis.repository;

import com.titanaxis.model.Venda;
import java.util.List;
import java.util.Optional;

public interface VendaRepository extends Repository<Venda, Integer> {
    // A interface Repository já define save, findById, findAll, deleteById.
    // Poderemos adicionar métodos específicos de venda aqui no futuro,
    // como por exemplo, encontrar vendas por cliente ou por data.
}