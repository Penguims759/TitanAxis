package com.titanaxis.repository;

import com.titanaxis.model.Categoria;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends Repository<Categoria, Integer> {
    // ALTERADO: Agora recebem um EntityManager
    List<Categoria> findAllWithProductCount(EntityManager em);
    List<Categoria> findByNomeContainingWithProductCount(String termo, EntityManager em);
    Optional<Categoria> findByNome(String nome, EntityManager em);
}