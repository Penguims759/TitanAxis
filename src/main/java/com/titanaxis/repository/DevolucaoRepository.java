package com.titanaxis.repository;

import com.titanaxis.model.Devolucao;
import jakarta.persistence.EntityManager;
import java.util.List;

public interface DevolucaoRepository extends Repository<Devolucao, Integer> {
    List<Devolucao> findAllWithDetails(EntityManager em);
}