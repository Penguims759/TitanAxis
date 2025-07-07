package com.titanaxis.repository;

import com.titanaxis.model.Cliente;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends Repository<Cliente, Integer> {
    List<Cliente> findByNomeContaining(String nome, EntityManager em);
    Optional<Cliente> findByNome(String nome, EntityManager em);
    long countNewClientesBetweenDates(LocalDateTime start, LocalDateTime end, EntityManager em);
}