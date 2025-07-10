package com.titanaxis.repository.impl;

import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.util.AppLogger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

public class AuditoriaRepositoryImpl implements AuditoriaRepository {
    private static final Logger logger = AppLogger.getLogger();

    @Override
    public void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, String detalhes, EntityManager em) {
        registrarAcao(usuarioId, usuarioNome, acao, entidade, null, detalhes, em);
    }

    @Override
    public void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, Integer entidadeId, String detalhes, EntityManager em) {
        try {
            Query query = em.createNativeQuery("INSERT INTO auditoria_logs (data_evento, usuario_id, usuario_nome, acao, entidade, entidade_id, detalhes) VALUES (?, ?, ?, ?, ?, ?, ?)");
            query.setParameter(1, Timestamp.valueOf(LocalDateTime.now()));
            query.setParameter(2, usuarioId);
            query.setParameter(3, usuarioNome);
            query.setParameter(4, acao);
            query.setParameter(5, entidade);
            query.setParameter(6, entidadeId);
            query.setParameter(7, detalhes);
            query.executeUpdate();
        } catch (Exception e) {
            throw new PersistenceException("Falha ao registrar ação de auditoria para o utilizador: " + usuarioNome, e);
        }
    }

    @Override
    public List<Object[]> findUserActionsForHabitAnalysis(int usuarioId, int days, EntityManager em) {
        String sql = "SELECT acao, data_evento FROM auditoria_logs WHERE usuario_id = :userId AND data_evento >= :startDate";
        Query query = em.createNativeQuery(sql);
        query.setParameter("userId", usuarioId);
        query.setParameter("startDate", LocalDateTime.now().minusDays(days));
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> findRecentActivity(int limit, EntityManager em) {
        String sql = "SELECT data_evento, usuario_nome, entidade, detalhes FROM auditoria_logs ORDER BY id DESC";
        return em.createNativeQuery(sql).setMaxResults(limit).getResultList();
    }

    @Override
    public List<Vector<Object>> getAuditoriaAcoes(EntityManager em) {
        String sql = "SELECT data_evento, usuario_nome, acao, entidade, detalhes FROM auditoria_logs " +
                "WHERE acao NOT LIKE 'LOGIN_%' ORDER BY id DESC";
        return fetchAuditoriaData(em, sql);
    }

    @Override
    public List<Vector<Object>> getAuditoriaAcesso(EntityManager em) {
        String sql = "SELECT data_evento, usuario_nome, acao, entidade, detalhes FROM auditoria_logs " +
                "WHERE acao LIKE 'LOGIN_%' ORDER BY id DESC";
        return fetchAuditoriaData(em, sql);
    }

    private List<Vector<Object>> fetchAuditoriaData(EntityManager em, String sql) {
        List<Vector<Object>> data = new ArrayList<>();
        Query query = em.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = query.getResultList();

        for (Object[] record : resultList) {
            Vector<Object> row = new Vector<>();
            Timestamp timestamp = (Timestamp) record[0];
            row.add(timestamp != null ? timestamp.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "");
            row.add(record[1] != null ? record[1].toString() : "Sistema");

            String acao = record[2] != null ? record[2].toString() : "";
            if (acao.equals("LOGIN_SUCESSO")) {
                row.add("SUCESSO");
            } else if (acao.equals("LOGIN_FALHA")) {
                row.add("FALHA");
            } else {
                row.add(acao);
            }

            row.add(record[3] != null ? record[3].toString() : "");
            row.add(record[4] != null ? record[4].toString() : "");
            data.add(row);
        }
        return data;
    }
}