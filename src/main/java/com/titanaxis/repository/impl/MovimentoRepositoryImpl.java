package com.titanaxis.repository.impl;

import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.repository.MovimentoRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

public class MovimentoRepositoryImpl implements MovimentoRepository {
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