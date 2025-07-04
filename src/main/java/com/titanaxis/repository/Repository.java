package com.titanaxis.repository;

import com.titanaxis.model.Usuario;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interface genérica para repositórios.
 * TODOS os métodos recebem um EntityManager para garantir que as transações
 * sejam controladas exclusivamente pela camada de serviço.
 */
public interface Repository<T, ID> {

    T save(T entity, Usuario usuarioLogado, EntityManager em);

    void deleteById(ID id, Usuario usuarioLogado, EntityManager em);

    // ALTERADO: Agora recebe um EntityManager
    Optional<T> findById(ID id, EntityManager em);

    // ALTERADO: Agora recebe um EntityManager
    List<T> findAll(EntityManager em);
}