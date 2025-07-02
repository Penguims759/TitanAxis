package com.titanaxis.repository;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends Repository<Categoria, Integer> {

    // A interface Repository já define os métodos save e deleteById com sobrecarga.
    // Apenas precisamos de nos certificar de que a implementação os utiliza.

    List<Categoria> findAllWithProductCount();
    List<Categoria> findByNomeContainingWithProductCount(String termo);
    Optional<Categoria> findByNome(String nome);
}