package com.titanaxis.repository;

import com.titanaxis.model.MovimentoEstoque;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimentoRepository {
    List<MovimentoEstoque> findAll(EntityManager em);
    List<MovimentoEstoque> findBetweenDates(LocalDateTime inicio, LocalDateTime fim, EntityManager em);
}