// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/repository/impl/UsuarioRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class UsuarioRepositoryImpl implements UsuarioRepository {
    private final AuditoriaRepository auditoriaRepository;

    @Inject
    public UsuarioRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Usuario save(Usuario usuario, Usuario ator, EntityManager em) {
        boolean isUpdate = usuario.getId() != 0;
        Usuario usuarioSalvo = em.merge(usuario);
        if (ator != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            // CORREÇÃO: Passar o EntityManager 'em'
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Usuário", "Usuário " + usuarioSalvo.getNomeUsuario() + " salvo.", em);
        }
        return usuarioSalvo;
    }

    @Override
    public void deleteById(Integer id, Usuario ator, EntityManager em) {
        Optional<Usuario> usuarioOpt = findById(id, em);
        usuarioOpt.ifPresent(usuario -> {
            if (ator != null) {
                // CORREÇÃO: Passar o EntityManager 'em'
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO", "Usuário", "Usuário " + usuario.getNomeUsuario() + " eliminado.", em);
            }
            em.remove(usuario);
        });
    }

    @Override
    public Optional<Usuario> findById(Integer id, EntityManager em) {
        return Optional.ofNullable(em.find(Usuario.class, id));
    }

    @Override
    public Optional<Usuario> findByNomeUsuario(String nomeUsuario, EntityManager em) {
        try {
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE u.nomeUsuario = :nomeUsuario", Usuario.class);
            query.setParameter("nomeUsuario", nomeUsuario);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Usuario> findAll(EntityManager em) {
        return em.createQuery("SELECT u FROM Usuario u ORDER BY u.nomeUsuario", Usuario.class).getResultList();
    }

    @Override
    public List<Usuario> findByNomeContaining(String nome, EntityManager em) {
        TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE LOWER(u.nomeUsuario) LIKE LOWER(:nome)", Usuario.class);
        query.setParameter("nome", "%" + nome + "%");
        return query.getResultList();
    }
}