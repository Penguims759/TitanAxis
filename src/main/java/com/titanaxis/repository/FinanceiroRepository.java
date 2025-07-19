package com.titanaxis.repository;

import com.titanaxis.model.ContasAReceber;
import com.titanaxis.model.MetaVenda;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public interface FinanceiroRepository {
    // Contas a Receber
    ContasAReceber saveContaAReceber(ContasAReceber conta, EntityManager em);
    Optional<ContasAReceber> findContaAReceberById(int id, EntityManager em);
    List<ContasAReceber> findContasAReceber(boolean apenasPendentes, EntityManager em);

    // Metas
    MetaVenda saveMeta(MetaVenda meta, EntityManager em);
    Optional<MetaVenda> findMetaByUsuarioAndPeriodo(int usuarioId, String anoMes, EntityManager em);
    List<MetaVenda> findAllMetas(EntityManager em);
    void deleteMetaById(int id, EntityManager em); // NOVO MÉTODO
}