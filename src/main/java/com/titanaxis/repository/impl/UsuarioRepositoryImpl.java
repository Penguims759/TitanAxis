package com.titanaxis.repository.impl;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioRepositoryImpl implements UsuarioRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository;

    public UsuarioRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Usuario save(Usuario usuario, Usuario ator) {
        boolean isUpdate = usuario.getId() != 0;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Usuario usuarioSalvo = em.merge(usuario); // merge() serve para criar e atualizar
            em.getTransaction().commit();

            if (ator != null) {
                String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
                String detalhes = String.format("Usuário '%s' (ID: %d) foi %s. Nível de Acesso: %s.",
                        usuarioSalvo.getNomeUsuario(), usuarioSalvo.getId(), isUpdate ? "atualizado" : "criado", usuarioSalvo.getNivelAcesso().getNome());
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Usuário", detalhes);
            }
            return usuarioSalvo;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.log(Level.SEVERE, "Erro ao salvar usuário: " + e.getMessage(), e);
            return null;
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public void deleteById(Integer id, Usuario ator) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, id);
            if (usuario != null) {
                em.remove(usuario);
                em.getTransaction().commit();

                if (ator != null) {
                    String detalhes = String.format("Usuário '%s' (ID: %d) foi eliminado.", usuario.getNomeUsuario(), id);
                    auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO", "Usuário", detalhes);
                }
            } else {
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.log(Level.SEVERE, "Erro ao deletar usuário ID: " + id, e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Optional<Usuario> findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Usuario.class, id));
        } finally {
            if (em.isOpen()) em.close();
        }
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
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u ORDER BY u.nomeUsuario", Usuario.class);
            return query.getResultList();
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

    // Métodos antigos que delegam para os novos
    @Override
    public Usuario save(Usuario usuario) {
        return save(usuario, null);
    }

    @Override
    public void deleteById(Integer id) {
        deleteById(id, null);
    }
}