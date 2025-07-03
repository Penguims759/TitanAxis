package com.titanaxis.repository.impl;

import com.titanaxis.model.*;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VendaRepositoryImpl implements VendaRepository {
    private static final Logger logger = AppLogger.getLogger();
    private final AuditoriaRepository auditoriaRepository;

    public VendaRepositoryImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Venda save(Venda venda, Usuario ator) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Atualiza o stock de cada lote
            for (VendaItem item : venda.getItens()) {
                Lote lote = em.find(Lote.class, item.getLote().getId());
                if (lote == null || lote.getQuantidade() < item.getQuantidade()) {
                    throw new SQLException("Stock insuficiente para o lote ID: " + item.getLote().getId());
                }
                lote.setQuantidade(lote.getQuantidade() - item.getQuantidade());
                em.merge(lote);
            }

            // O JPA/Hibernate trata de salvar a venda e os seus itens em cascata
            Venda vendaSalva = em.merge(venda);

            // Regista os movimentos de estoque (ainda usando uma query nativa para simplicidade)
            for (VendaItem item : venda.getItens()) {
                Query movimentoQuery = em.createNativeQuery("INSERT INTO movimentos_estoque (produto_id, lote_id, tipo_movimento, quantidade, usuario_id) VALUES (?, ?, ?, ?, ?)");
                movimentoQuery.setParameter(1, item.getLote().getProduto().getId());
                movimentoQuery.setParameter(2, item.getLote().getId());
                movimentoQuery.setParameter(3, "VENDA");
                movimentoQuery.setParameter(4, item.getQuantidade());
                movimentoQuery.setParameter(5, ator != null ? ator.getId() : null);
                movimentoQuery.executeUpdate();
            }

            em.getTransaction().commit();
            logger.info("Venda ID " + vendaSalva.getId() + " salva com sucesso.");

            // Lógica de auditoria
            if (ator != null) {
                String nomeCliente = vendaSalva.getCliente() != null ? vendaSalva.getCliente().getNome() : "N/A";
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                String detalhes = String.format("Venda #%d finalizada para o cliente '%s'. Valor total: %s.",
                        vendaSalva.getId(), nomeCliente, currencyFormat.format(vendaSalva.getValorTotal()));
                auditoriaRepository.registrarAcao(ator.getId(), ator.getNomeUsuario(), "FINALIZAÇÃO DE VENDA", "Venda", detalhes);
            }

            return vendaSalva;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro na transação de venda. Rollback acionado.", e);
            // Re-lança a exceção para que a camada de serviço/UI possa apanhá-la
            throw new RuntimeException("Erro ao processar a venda: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Venda> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // LEFT JOIN FETCH para carregar o cliente e o usuário na mesma query para evitar N+1 selects
            TypedQuery<Venda> query = em.createQuery(
                    "SELECT v FROM Venda v LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.usuario ORDER BY v.dataVenda DESC", Venda.class);
            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
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

    // --- Métodos não implementados/necessários ---

    @Override
    public Venda save(Venda entity) {
        return save(entity, null);
    }

    @Override
    public void deleteById(Integer id) {
        // A lógica de apagar vendas geralmente é desaconselhada.
        // Se necessário, implementar aqui.
        logger.warning("A função de apagar vendas não está implementada.");
    }
}