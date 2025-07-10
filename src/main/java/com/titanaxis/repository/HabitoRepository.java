package com.titanaxis.repository;

import com.titanaxis.model.auditoria.Habito;
import jakarta.persistence.EntityManager;
import java.util.List;

public interface HabitoRepository {
    Habito save(Habito habito, EntityManager em);
    void delete(int habitoId, EntityManager em);
    List<Habito> findByUsuario(int usuarioId, EntityManager em);
}