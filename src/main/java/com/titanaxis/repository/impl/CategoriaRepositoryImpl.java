// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/repository/impl/CategoriaRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.util.I18n; // Importado
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class CategoriaRepositoryImpl implements CategoriaRepository {
    private final AuditoriaRepository auditoriaRepository;

    @Inject
    public CategoriaRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Categoria save(Categoria categoria, Usuario usuarioLogado, EntityManager em) {
        boolean isUpdate = categoria.getId() != 0;
        Categoria categoriaSalva = em.merge(categoria);

        if (usuarioLogado != null) {
            String acao = isUpdate ? "ATUALIZAÇÃO" : "CRIAÇÃO";
            
            String acaoDesc = isUpdate ? I18n.getString("log.action.updated") : I18n.getString("log.action.created");
            String detalhes = I18n.getString("log.category.saved", categoriaSalva.getNome(), categoriaSalva.getId(), acaoDesc);
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), acao, "Categoria", categoriaSalva.getId(), detalhes, em);
        }
        return categoriaSalva;
    }

    @Override
    public void deleteById(Integer id, Usuario usuarioLogado, EntityManager em) {
        Optional<Categoria> categoriaOpt = findById(id, em);
        categoriaOpt.ifPresent(categoria -> {
            if (usuarioLogado != null) {
                
                String detalhes = I18n.getString("log.category.deleted", categoria.getNome(), id);
                auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), "EXCLUSÃO", "Categoria", id, detalhes, em);
            }
            em.remove(categoria);
        });
    }

    @Override
    public Optional<Categoria> findById(Integer id, EntityManager em) {
        return Optional.ofNullable(em.find(Categoria.class, id));
    }

    @Override
    public Optional<Categoria> findByNome(String nome, EntityManager em) {
        try {
            TypedQuery<Categoria> query = em.createQuery("SELECT c FROM Categoria c LEFT JOIN FETCH c.produtos WHERE c.nome = :nome", Categoria.class);
            query.setParameter("nome", nome);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Categoria> findAll(EntityManager em) {
        return em.createQuery("SELECT c FROM Categoria c LEFT JOIN FETCH c.produtos ORDER BY c.nome", Categoria.class).getResultList();
    }

    @Override
    public List<Categoria> findAllWithProductCount(EntityManager em) {
        String jpql = "SELECT new com.titanaxis.model.Categoria(c.id, c.nome, SIZE(c.produtos)) " +
                "FROM Categoria c ORDER BY c.nome";
        return em.createQuery(jpql, Categoria.class).getResultList();
    }

    @Override
    public List<Categoria> findByNomeContainingWithProductCount(String termo, EntityManager em) {
        String jpql = "SELECT new com.titanaxis.model.Categoria(c.id, c.nome, SIZE(c.produtos)) " +
                "FROM Categoria c WHERE LOWER(c.nome) LIKE LOWER(:termo) ORDER BY c.nome";
        TypedQuery<Categoria> query = em.createQuery(jpql, Categoria.class);
        query.setParameter("termo", "%" + termo + "%");
        return query.getResultList();
    }
}