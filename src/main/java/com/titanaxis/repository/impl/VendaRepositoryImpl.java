package com.titanaxis.repository.impl;

import com.google.inject.Inject;
import com.titanaxis.model.*;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.util.AppLogger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

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

        if (ator != null) {
            String nomeCliente = vendaSalva.getCliente() != null ? vendaSalva.getCliente().getNome() : "N/A";
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            String detalhes = String.format("Venda #%d finalizada para o cliente '%s'. Valor total: %s.",
                    vendaSalva.getId(), nomeCliente, currencyFormat.format(vendaSalva.getValorTotal()));
            auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "FINALIZAÇÃO DE VENDA", "Venda", detalhes, em);
        }

        return vendaSalva;
    }

    @Override
    public void deleteById(Integer id, Usuario ator, EntityManager em) {
        logger.warning("A função de apagar vendas não está implementada por razões de integridade de dados.");
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

    // MÉTODO CORRIGIDO
    @Override
    public Optional<Venda> findById(Integer id, EntityManager em) {
        try {
            // Esta query agora usa 'LEFT JOIN FETCH' para carregar todas as associações necessárias
            // (cliente, utilizador, itens, produto do item, lote do item) numa única consulta.
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
}