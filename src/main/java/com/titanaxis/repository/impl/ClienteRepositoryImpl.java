package com.titanaxis.repository.impl;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteRepositoryImpl implements ClienteRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository;

    public ClienteRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Cliente save(Cliente cliente, Usuario ator) {
        boolean isUpdate = cliente.getId() != 0;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Cliente clienteSalvo = em.merge(cliente);
            em.getTransaction().commit();

            if (ator != null) {
                String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
                String detalhes = String.format("Cliente '%s' (ID: %d) foi %s.",
                        clienteSalvo.getNome(), clienteSalvo.getId(), isUpdate ? "atualizado" : "criado");
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Cliente", detalhes);
            }
            return clienteSalvo;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.log(Level.SEVERE, "Erro ao salvar cliente: " + e.getMessage(), e);
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
            Cliente cliente = em.find(Cliente.class, id);
            if (cliente != null) {
                em.remove(cliente);
                em.getTransaction().commit();

                if (ator != null) {
                    String detalhes = String.format("Cliente '%s' (ID: %d) foi eliminado.", cliente.getNome(), id);
                    auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "EXCLUSÃO", "Cliente", detalhes);
                }
            } else {
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.log(Level.SEVERE, "Erro ao deletar cliente ID: " + id, e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Optional<Cliente> findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Cliente.class, id));
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Cliente> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery("SELECT c FROM Cliente c ORDER BY c.nome", Cliente.class);
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Cliente> findByNomeContaining(String nome) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(:nome) ORDER BY c.nome", Cliente.class);
            query.setParameter("nome", "%" + nome + "%");
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Métodos antigos que delegam para os novos
    @Override
    public Cliente save(Cliente cliente) {
        return save(cliente, null);
    }

    @Override
    public void deleteById(Integer id) {
        deleteById(id, null);
    }
}