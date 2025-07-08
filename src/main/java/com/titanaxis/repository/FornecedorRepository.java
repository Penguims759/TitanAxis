// src/main/java/com/titanaxis/repository/FornecedorRepository.java
package com.titanaxis.repository;

import com.titanaxis.model.Fornecedor;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public interface FornecedorRepository extends Repository<Fornecedor, Integer> {
    Optional<Fornecedor> findByNome(String nome, EntityManager em);
    List<Fornecedor> findByNomeContaining(String nome, EntityManager em);
}