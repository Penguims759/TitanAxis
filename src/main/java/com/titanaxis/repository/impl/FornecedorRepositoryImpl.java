// src/main/java/com/titanaxis/repository/impl/FornecedorRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.Fornecedor;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.FornecedorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class FornecedorRepositoryImpl implements FornecedorRepository {

    private final AuditoriaRepository auditoriaRepository;

    @Inject
    public FornecedorRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Fornecedor save(Fornecedor fornecedor, Usuario usuarioLogado, EntityManager em) {
        boolean isUpdate = fornecedor.getId() != 0;
        Fornecedor fornecedorSalvo = em.merge(fornecedor);

        if (usuarioLogado != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            String detalhes = String.format("Fornecedor '%s' (ID: %d) foi %s.",
                    fornecedorSalvo.getNome(), fornecedorSalvo.getId(), isUpdate ? "atualizado" : "criado");
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), acao, "Fornecedor", fornecedorSalvo.getId(), detalhes, em);
        }
        return fornecedorSalvo;
    }

    @Override
    public void deleteById(Integer id, Usuario usuarioLogado, EntityManager em) {
        findById(id, em).ifPresent(fornecedor -> {
            if (usuarioLogado != null) {
                String detalhes = String.format("Fornecedor '%s' (ID: %d) foi eliminado.", fornecedor.getNome(), id);
                auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), "EXCLUSÃO", "Fornecedor", id, detalhes, em);
            }
            em.remove(fornecedor);
        });
    }

    @Override
    public Optional<Fornecedor> findById(Integer id, EntityManager em) {
        return Optional.ofNullable(em.find(Fornecedor.class, id));
    }

    @Override
    public List<Fornecedor> findAll(EntityManager em) {
        return em.createQuery("SELECT f FROM Fornecedor f ORDER BY f.nome", Fornecedor.class).getResultList();
    }

    @Override
    public Optional<Fornecedor> findByNome(String nome, EntityManager em) {
        TypedQuery<Fornecedor> query = em.createQuery("SELECT f FROM Fornecedor f WHERE f.nome = :nome", Fornecedor.class);
        query.setParameter("nome", nome);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Fornecedor> findByNomeContaining(String nome, EntityManager em) {
        TypedQuery<Fornecedor> query = em.createQuery("SELECT f FROM Fornecedor f WHERE LOWER(f.nome) LIKE LOWER(:nome)", Fornecedor.class);
        query.setParameter("nome", "%" + nome + "%");
        return query.getResultList();
    }
}