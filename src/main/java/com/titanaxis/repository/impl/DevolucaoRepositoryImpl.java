package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.Devolucao;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.DevolucaoRepository;
import com.titanaxis.util.I18n; // Importado
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class DevolucaoRepositoryImpl implements DevolucaoRepository {

    private final AuditoriaRepository auditoriaRepository;

    @Inject
    public DevolucaoRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Devolucao save(Devolucao devolucao, Usuario usuarioLogado, EntityManager em) {
        Devolucao devolucaoSalva = em.merge(devolucao);
        if (usuarioLogado != null) {
            
            String detalhes = I18n.getString("log.return.registered",
                    devolucaoSalva.getId(), devolucao.getVenda().getId(), devolucao.getValorEstornado());
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), usuarioLogado.getNomeUsuario(), "REGISTRO DE DEVOLUÇÃO", "Venda", devolucao.getVenda().getId(), detalhes, em);
        }
        return devolucaoSalva;
    }

    @Override
    public void deleteById(Integer id, Usuario usuarioLogado, EntityManager em) {
        // Deletar devoluções não é uma prática comum
    }

    @Override
    public Optional<Devolucao> findById(Integer id, EntityManager em) {
        return Optional.ofNullable(em.find(Devolucao.class, id));
    }

    @Override
    public List<Devolucao> findAll(EntityManager em) {
        return em.createQuery("SELECT d FROM Devolucao d ORDER BY d.dataDevolucao DESC", Devolucao.class).getResultList();
    }

    @Override
    public List<Devolucao> findAllWithDetails(EntityManager em) {
        return em.createQuery("SELECT d FROM Devolucao d LEFT JOIN FETCH d.venda LEFT JOIN FETCH d.usuario ORDER BY d.dataDevolucao DESC", Devolucao.class).getResultList();
    }
}