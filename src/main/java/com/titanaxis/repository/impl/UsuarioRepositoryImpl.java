package com.titanaxis.repository.impl;

import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class UsuarioRepositoryImpl implements UsuarioRepository {
    private final AuditoriaRepository auditoriaRepository;

    public UsuarioRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Usuario save(Usuario usuario, Usuario ator, EntityManager em) {
        boolean isUpdate = usuario.getId() != 0;
        Usuario usuarioSalvo = em.merge(usuario);
        if (ator != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Usuário", "Usuário " + usuarioSalvo.getNomeUsuario() + " salvo.");
        }
        return usuarioSalvo;
    }

    @Override
    public void deleteById(Integer id, Usuario ator, EntityManager em) {
        Usuario usuario = em.find(Usuario.class, id);
        if (usuario != null) {
            if (ator != null) {
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO", "Usuário", "Usuário " + usuario.getNomeUsuario() + " eliminado.");
            }
            em.remove(usuario);
        }
    }

    @Override
    public Optional<Usuario> findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try { return Optional.ofNullable(em.find(Usuario.class, id)); } finally { if (em.isOpen()) em.close(); }
    }

    @Override
    public Optional<Usuario> findByNomeUsuario(String nomeUsuario) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE u.nomeUsuario = :nomeUsuario", Usuario.class);
            query.setParameter("nomeUsuario", nomeUsuario);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Usuario> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u ORDER BY u.nomeUsuario", Usuario.class).getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Usuario> findByNomeContaining(String nome) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE LOWER(u.nomeUsuario) LIKE LOWER(:nome)", Usuario.class);
            query.setParameter("nome", "%" + nome + "%");
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}