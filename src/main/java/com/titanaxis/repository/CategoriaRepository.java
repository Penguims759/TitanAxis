package com.titanaxis.repository;

import com.titanaxis.model.Categoria;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends Repository<Categoria, Integer> {
    List<Categoria> findAllWithProductCount();
    List<Categoria> findByNomeContainingWithProductCount(String termo);
    Optional<Categoria> findByNome(String nome);
}