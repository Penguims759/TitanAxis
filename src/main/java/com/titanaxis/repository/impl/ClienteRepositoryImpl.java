// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/repository/impl/ClienteRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ClienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
            String detalhes = String.format("Cliente '%s' (ID: %d) foi %s.",
                    clienteSalvo.getNome(), clienteSalvo.getId(), isUpdate ? "atualizado" : "criado");
            // CORREÇÃO: Passar o EntityManager para o método de auditoria.
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), acao, "Cliente", detalhes, em);
        }
        return clienteSalvo;
    }

    @Override
    public void deleteById(Integer id, Usuario usuarioLogado, EntityManager em) {
        Optional<Cliente> clienteOpt = findById(id, em);
        clienteOpt.ifPresent(cliente -> {
            if (usuarioLogado != null) {
                String detalhes = String.format("Cliente '%s' (ID: %d) foi eliminado.", cliente.getNome(), id);
                // CORREÇÃO: Passar o EntityManager para o método de auditoria.
                auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), "EXCLUSÃO", "Cliente", detalhes, em);
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
}