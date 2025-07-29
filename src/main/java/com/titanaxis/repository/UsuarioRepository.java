package com.titanaxis.repository;

import com.titanaxis.model.Usuario;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends Repository<Usuario, Integer> {
    
    Optional<Usuario> findByNomeUsuario(String nomeUsuario, EntityManager em);
    List<Usuario> findByNomeContaining(String nome, EntityManager em);
}