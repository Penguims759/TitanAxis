package com.titanaxis.repository;

import com.titanaxis.model.MovimentoEstoque;
import jakarta.persistence.EntityManager;
import java.util.List;

public interface MovimentoRepository {
    List<MovimentoEstoque> findAll(EntityManager em);
}