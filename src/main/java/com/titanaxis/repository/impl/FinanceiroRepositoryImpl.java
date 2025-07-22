package com.titanaxis.repository.impl;

import com.titanaxis.model.ContasAReceber;
import com.titanaxis.model.MetaVenda;
import com.titanaxis.repository.FinanceiroRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class FinanceiroRepositoryImpl implements FinanceiroRepository {

    @Override
    public ContasAReceber saveContaAReceber(ContasAReceber conta, EntityManager em) {
        return em.merge(conta);
    }

    @Override
    public Optional<ContasAReceber> findContaAReceberById(int id, EntityManager em) {
        return Optional.ofNullable(em.find(ContasAReceber.class, id));
    }

    @Override
    public List<ContasAReceber> findContasAReceber(boolean apenasPendentes, EntityManager em) {
        String jpql = "SELECT c FROM ContasAReceber c JOIN FETCH c.venda v JOIN FETCH v.cliente WHERE 1=1 ";
        if (apenasPendentes) {
            jpql += "AND c.status <> 'Pago' ";
        }
        jpql += "ORDER BY c.dataVencimento ASC";
        return em.createQuery(jpql, ContasAReceber.class).getResultList();
    }

    @Override
    public MetaVenda saveMeta(MetaVenda meta, EntityManager em) {
        return em.merge(meta);
    }

    @Override
    public List<MetaVenda> findAllMetas(EntityManager em) {
        return em.createQuery("SELECT m FROM MetaVenda m JOIN FETCH m.usuario ORDER BY m.dataInicio DESC, m.usuario.nomeUsuario ASC", MetaVenda.class).getResultList();
    }

    @Override
    public Optional<MetaVenda> findById(int id, EntityManager em) {
        return Optional.ofNullable(em.find(MetaVenda.class, id));
    }

    @Override
    public void deleteMetaById(int id, EntityManager em) {
        findById(id, em).ifPresent(em::remove);
    }
}