// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/repository/impl/ClienteRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.util.I18n; // Importado
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ClienteRepositoryImpl implements ClienteRepository {

    private final AuditoriaRepository auditoriaRepository;

    @Inject
    public ClienteRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Cliente save(Cliente cliente, Usuario usuarioLogado, EntityManager em) {
        boolean isUpdate = cliente.getId() != 0;
        Cliente clienteSalvo = em.merge(cliente);

        if (usuarioLogado != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            // ALTERADO
            String acaoDesc = isUpdate ? I18n.getString("log.action.updated") : I18n.getString("log.action.created");
            String detalhes = I18n.getString("log.client.saved", clienteSalvo.getNome(), clienteSalvo.getId(), acaoDesc);
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), acao, "Cliente", clienteSalvo.getId(), detalhes, em);
        }
        return clienteSalvo;
    }

    @Override
    public void deleteById(Integer id, Usuario usuarioLogado, EntityManager em) {
        Optional<Cliente> clienteOpt = findById(id, em);
        clienteOpt.ifPresent(cliente -> {
            if (usuarioLogado != null) {
                // ALTERADO
                String detalhes = I18n.getString("log.client.deleted", cliente.getNome(), id);
                auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), "EXCLUSÃO", "Cliente", id, detalhes, em);
            }
            em.remove(cliente);
        });
    }

    @Override
    public Optional<Cliente> findById(Integer id, EntityManager em) {
        return Optional.ofNullable(em.find(Cliente.class, id));
    }

    @Override
    public List<Cliente> findAll(EntityManager em) {
        return em.createQuery("SELECT c FROM Cliente c ORDER BY c.nome", Cliente.class).getResultList();
    }

    @Override
    public List<Cliente> findByNomeContaining(String nome, EntityManager em) {
        TypedQuery<Cliente> query = em.createQuery("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(:nome)", Cliente.class);
        query.setParameter("nome", "%" + nome + "%");
        return query.getResultList();
    }

    @Override
    public Optional<Cliente> findByNome(String nome, EntityManager em) {
        TypedQuery<Cliente> query = em.createQuery("SELECT c FROM Cliente c WHERE c.nome = :nome", Cliente.class);
        query.setParameter("nome", nome);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public long countNewClientesBetweenDates(LocalDateTime start, LocalDateTime end, EntityManager em) {
        Query query = em.createNativeQuery(
                "SELECT COUNT(DISTINCT entidade_id) FROM auditoria_logs WHERE acao = 'CRIAÇÃO' AND entidade = 'Cliente' AND data_evento BETWEEN ? AND ?");
        query.setParameter(1, start);
        query.setParameter(2, end);
        Object result = query.getSingleResult();
        return (result instanceof Number) ? ((Number) result).longValue() : 0L;
    }
}