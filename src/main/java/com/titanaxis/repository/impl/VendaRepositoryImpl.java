package com.titanaxis.repository.impl;

import com.titanaxis.model.*;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class VendaRepositoryImpl implements VendaRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository;

    public VendaRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Venda save(Venda venda, Usuario ator, EntityManager em) {
        // Persiste a entidade Venda. O JPA/Hibernate trata de salvar os VendaItems em cascata.
        Venda vendaSalva = em.merge(venda);

        // Regista os movimentos de estoque usando o mesmo EntityManager
        for (VendaItem item : venda.getItens()) {
            Query movimentoQuery = em.createNativeQuery("INSERT INTO movimentos_estoque (produto_id, lote_id, tipo_movimento, quantidade, usuario_id) VALUES (?, ?, ?, ?, ?)");
            movimentoQuery.setParameter(1, item.getLote().getProduto().getId());
            movimentoQuery.setParameter(2, item.getLote().getId());
            movimentoQuery.setParameter(3, "VENDA");
            movimentoQuery.setParameter(4, item.getQuantidade());
            movimentoQuery.setParameter(5, ator != null ? ator.getId() : null);
            movimentoQuery.executeUpdate();
        }

        // A lógica de auditoria também usa a mesma transação
        if (ator != null) {
            String nomeCliente = vendaSalva.getCliente() != null ? vendaSalva.getCliente().getNome() : "N/A";
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            String detalhes = String.format("Venda #%d finalizada para o cliente '%s'. Valor total: %s.",
                    vendaSalva.getId(), nomeCliente, currencyFormat.format(vendaSalva.getValorTotal()));
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "FINALIZAÇÃO DE VENDA", "Venda", detalhes);
        }

        return vendaSalva;
    }

    @Override
    public List<Venda> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Venda> query = em.createQuery(
                    "SELECT v FROM Venda v LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.usuario ORDER BY v.dataVenda DESC", Venda.class);
            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    // --- Métodos de compatibilidade ou não implementados ---

    @Override
    public Venda save(Venda venda, Usuario ator) {
        throw new UnsupportedOperationException("O método save deve ser chamado com um EntityManager para garantir a atomicidade da transação.");
    }

    @Override
    public Optional<Venda> findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Venda.class, id));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Venda save(Venda entity) {
        return save(entity, null);
    }

    @Override
    public void deleteById(Integer id) {
        logger.warning("A função de apagar vendas não está implementada por razões de integridade de dados.");
    }
}