// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/repository/impl/MovimentoRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.repository.MovimentoRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

public class MovimentoRepositoryImpl implements MovimentoRepository {

    @Inject
    public MovimentoRepositoryImpl() {
        // No-arg constructor for Guice.
        // This repository does not have direct dependencies injected through its constructor.
    }

    @Override
    public List<MovimentoEstoque> findAll(EntityManager em) {
        String jpql = "SELECT m FROM MovimentoEstoque m " +
                "LEFT JOIN FETCH m.produto p " +
                "LEFT JOIN FETCH m.lote l " +
                "LEFT JOIN FETCH m.usuario u " +
                "ORDER BY m.id DESC";
        return em.createQuery(jpql, MovimentoEstoque.class).getResultList();
    }
}