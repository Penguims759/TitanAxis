package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.*;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n; // Importado
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;

public class VendaRepositoryImpl implements VendaRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository;

    @Inject
    public VendaRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Venda save(Venda venda, Usuario ator, EntityManager em) {
        Venda vendaSalva = em.merge(venda);

        String acao;
        // ALTERADO
        if (venda.getStatus() == VendaStatus.FINALIZADA) {
            acao = I18n.getString("log.sale.action.finalize");
            for (VendaItem item : venda.getItens()) {
                Query movimentoQuery = em.createNativeQuery("INSERT INTO movimentos_estoque (produto_id, lote_id, tipo_movimento, quantidade, usuario_id, venda_id) VALUES (?, ?, ?, ?, ?, ?)");
                movimentoQuery.setParameter(1, item.getLote().getProduto().getId());
                movimentoQuery.setParameter(2, item.getLote().getId());
                movimentoQuery.setParameter(3, "VENDA");
                movimentoQuery.setParameter(4, item.getQuantidade());
                movimentoQuery.setParameter(5, ator != null ? ator.getId() : null);
                movimentoQuery.setParameter(6, vendaSalva.getId());
                movimentoQuery.executeUpdate();
            }
        } else if (venda.getStatus() == VendaStatus.ORCAMENTO) {
            acao = I18n.getString("log.sale.action.createQuote");
        } else {
            acao = I18n.getString("log.sale.action.cancel");
        }

        if (ator != null) {
            String nomeCliente = vendaSalva.getCliente() != null ? vendaSalva.getCliente().getNome() : I18n.getString("general.notAvailable"); // ALTERADO
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            // ALTERADO
            String detalhes = I18n.getString("log.sale.details",
                    vendaSalva.getId(), venda.getStatus().getDescricao(), nomeCliente, currencyFormat.format(vendaSalva.getValorTotal()));
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), acao, "Venda", vendaSalva.getId(), detalhes, em);
        }

        return vendaSalva;
    }

    @Override
    public void deleteById(Integer id, Usuario ator, EntityManager em) {
        logger.warn(I18n.getString("log.sale.deleteNotImplemented")); // ALTERADO
    }

    @Override
    public List<Venda> findAll(EntityManager em) {
        TypedQuery<Venda> query = em.createQuery(
                "SELECT v FROM Venda v LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.usuario ORDER BY v.dataVenda DESC", Venda.class);
        return query.getResultList();
    }

    @Override
    public List<VendaItem> findAllItems(EntityManager em) {
        TypedQuery<VendaItem> query = em.createQuery(
                "SELECT i FROM VendaItem i JOIN FETCH i.produto", VendaItem.class);
        return query.getResultList();
    }

    @Override
    public Optional<Venda> findById(Integer id, EntityManager em) {
        try {
            TypedQuery<Venda> query = em.createQuery(
                    "SELECT v FROM Venda v " +
                            "LEFT JOIN FETCH v.cliente " +
                            "LEFT JOIN FETCH v.usuario " +
                            "LEFT JOIN FETCH v.itens i " +
                            "LEFT JOIN FETCH i.produto " +
                            "LEFT JOIN FETCH i.lote " +
                            "WHERE v.id = :id", Venda.class);
            query.setParameter("id", id);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Venda> findVendasByClienteId(int clienteId, EntityManager em) {
        TypedQuery<Venda> query = em.createQuery(
                "SELECT v FROM Venda v LEFT JOIN FETCH v.itens i LEFT JOIN FETCH i.produto p WHERE v.cliente.id = :clienteId ORDER BY v.dataVenda DESC", Venda.class);
        query.setParameter("clienteId", clienteId);
        return query.getResultList();
    }

    @Override
    public List<Venda> findVendasBetweenDates(LocalDateTime start, LocalDateTime end, EntityManager em) {
        TypedQuery<Venda> query = em.createQuery(
                "SELECT v FROM Venda v LEFT JOIN FETCH v.cliente WHERE v.dataVenda BETWEEN :start AND :end", Venda.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public List<VendaItem> findVendaItensBetweenDates(LocalDateTime start, LocalDateTime end, EntityManager em) {
        TypedQuery<VendaItem> query = em.createQuery(
                "SELECT i FROM VendaItem i JOIN FETCH i.venda v JOIN FETCH i.produto p WHERE v.dataVenda BETWEEN :start AND :end", VendaItem.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public List<Venda> findWithFilters(LocalDateTime start, LocalDateTime end, VendaStatus status, String clienteNome, EntityManager em) {
        StringBuilder jpql = new StringBuilder("SELECT v FROM Venda v LEFT JOIN FETCH v.cliente c LEFT JOIN FETCH v.usuario u WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (start != null) {
            jpql.append(" AND v.dataVenda >= :start");
            params.put("start", start);
        }
        if (end != null) {
            jpql.append(" AND v.dataVenda <= :end");
            params.put("end", end);
        }
        if (status != null) {
            jpql.append(" AND v.status = :status");
            params.put("status", status); // <<== LINHA CORRIGIDA
        }
        if (clienteNome != null && !clienteNome.isEmpty()) {
            jpql.append(" AND LOWER(c.nome) LIKE LOWER(:clienteNome)");
            params.put("clienteNome", "%" + clienteNome + "%");
        }

        jpql.append(" ORDER BY v.dataVenda DESC");

        TypedQuery<Venda> query = em.createQuery(jpql.toString(), Venda.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }
}