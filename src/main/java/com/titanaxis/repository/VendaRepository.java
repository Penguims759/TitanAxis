package com.titanaxis.repository;

import com.titanaxis.model.Usuario;
import com.titanaxis.model.Venda;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public interface VendaRepository extends Repository<Venda, Integer> {

    /**
     * Salva uma entidade Venda usando um EntityManager fornecido externamente,
     * permitindo que a operação faça parte de uma transação maior.
     * @param venda A entidade Venda a ser salva.
     * @param ator O utilizador que está a realizar a operação.
     * @param em O EntityManager da transação atual.
     * @return A entidade Venda salva.
     */
    Venda save(Venda venda, Usuario ator, EntityManager em);
}