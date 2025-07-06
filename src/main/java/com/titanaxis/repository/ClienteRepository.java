// src/main/java/com/titanaxis/repository/ClienteRepository.java
package com.titanaxis.repository;

import com.titanaxis.model.Cliente;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional; // NOVO

public interface ClienteRepository extends Repository<Cliente, Integer> {
    // ALTERADO: Agora recebe um EntityManager
    List<Cliente> findByNomeContaining(String nome, EntityManager em);
    Optional<Cliente> findByNome(String nome, EntityManager em); // NOVO
}