package com.titanaxis.repository.impl;

import com.titanaxis.model.auditoria.Habito;
import com.titanaxis.repository.HabitoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class HabitoRepositoryImpl implements HabitoRepository {

    @Override
    public Habito save(Habito habito, EntityManager em) {
        return em.merge(habito);
    }

    @Override
    public void delete(int habitoId, EntityManager em) {
        Habito habito = em.find(Habito.class, habitoId);
        if (habito != null) {
            em.remove(habito);
        }
    }

    @Override
    public List<Habito> findByUsuario(int usuarioId, EntityManager em) {
        TypedQuery<Habito> query = em.createQuery(
                "SELECT h FROM Habito h WHERE h.usuario.id = :usuarioId ORDER BY h.diaDaSemana, h.acao",
                Habito.class
        );
        query.setParameter("usuarioId", usuarioId);
        return query.getResultList();
    }
}