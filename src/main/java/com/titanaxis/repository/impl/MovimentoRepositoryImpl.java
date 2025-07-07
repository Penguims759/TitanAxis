package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.repository.MovimentoRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class MovimentoRepositoryImpl implements MovimentoRepository {

    @Inject
    public MovimentoRepositoryImpl() {}

    @Override
    public List<MovimentoEstoque> findAll(EntityManager em) {
        String jpql = "SELECT m FROM MovimentoEstoque m " +
                "LEFT JOIN FETCH m.produto p " +
                "LEFT JOIN FETCH m.lote l " +
                "LEFT JOIN FETCH m.usuario u " +
                "ORDER BY m.id DESC";
        return em.createQuery(jpql, MovimentoEstoque.class).getResultList();
    }

    @Override
    public List<MovimentoEstoque> findBetweenDates(LocalDateTime inicio, LocalDateTime fim, EntityManager em) {
        String jpql = "SELECT m FROM MovimentoEstoque m " +
                "LEFT JOIN FETCH m.produto p " +
                "LEFT JOIN FETCH m.lote l " +
                "LEFT JOIN FETCH m.usuario u " +
                "WHERE m.dataMovimento BETWEEN :inicio AND :fim " +
                "ORDER BY m.id DESC";
        return em.createQuery(jpql, MovimentoEstoque.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList();
    }
}