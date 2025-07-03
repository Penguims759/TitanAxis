package com.titanaxis.repository;

import com.titanaxis.model.Usuario;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {

    T save(T entity, Usuario usuarioLogado, EntityManager em);

    void deleteById(ID id, Usuario usuarioLogado, EntityManager em);

    Optional<T> findById(ID id);

    List<T> findAll();
}