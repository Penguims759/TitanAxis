package com.titanaxis.repository;

import com.titanaxis.model.Cliente;
import jakarta.persistence.EntityManager;
import java.util.List;

public interface ClienteRepository extends Repository<Cliente, Integer> {
    // ALTERADO: Agora recebe um EntityManager
    List<Cliente> findByNomeContaining(String nome, EntityManager em);
}