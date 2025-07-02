package com.titanaxis.repository;

import com.titanaxis.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends Repository<Usuario, Integer> {
    Optional<Usuario> findByNomeUsuario(String nomeUsuario);
    List<Usuario> findByNomeContaining(String nome);
}