package com.titanaxis.repository.impl;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class ClienteRepositoryImpl implements ClienteRepository {

    private final AuditoriaRepository auditoriaRepository;

    public ClienteRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Cliente save(Cliente cliente, Usuario usuarioLogado, EntityManager em) {
        boolean isUpdate = cliente.getId() != 0;
        Cliente clienteSalvo = em.merge(cliente);

        if (usuarioLogado != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            String detalhes = String.format("Cliente '%s' (ID: %d) foi %s.",
                    clienteSalvo.getNome(), clienteSalvo.getId(), isUpdate ? "atualizado" : "criado");
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), acao, "Cliente", detalhes);
        }
        return clienteSalvo;
    }

    @Override
    public void deleteById(Integer id, Usuario usuarioLogado, EntityManager em) {
        Cliente cliente = em.find(Cliente.class, id);
        if (cliente != null) {
            if (usuarioLogado != null) {
                String detalhes = String.format("Cliente '%s' (ID: %d) foi eliminado.", cliente.getNome(), id);
                auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), "EXCLUSÃO", "Cliente", detalhes);
            }
            em.remove(cliente);
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
            return em.createQuery("SELECT c FROM Cliente c ORDER BY c.nome", Cliente.class).getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Cliente> findByNomeContaining(String nome) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(:nome)", Cliente.class);
            query.setParameter("nome", "%" + nome + "%");
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}