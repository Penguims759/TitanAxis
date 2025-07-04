package com.titanaxis.repository.impl;

import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditoriaRepositoryImpl implements AuditoriaRepository {
    private static final Logger logger = AppLogger.getLogger();

    @Override
    public void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, String detalhes) {
        // Este método precisa de ser executado dentro de uma transação JÁ EXISTENTE
        // gerida pela camada de Serviço. Não deve controlar a sua própria transação aqui.
        // A lógica de registo foi movida para os repositórios específicos (ex: CategoriaRepositoryImpl)
        // para garantir que seja executada dentro da transação correta.
        // Esta implementação servirá como fallback ou para registos fora de um fluxo transacional normal,
        // embora essa não seja a prática ideal.
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();
            em.getTransaction().begin();

            Query query = em.createNativeQuery("INSERT INTO auditoria_logs (data_evento, usuario_id, usuario_nome, acao, entidade, detalhes) VALUES (?, ?, ?, ?, ?, ?)");
            query.setParameter(1, Timestamp.valueOf(LocalDateTime.now()));
            query.setParameter(2, usuarioId);
            query.setParameter(3, usuarioNome);
            query.setParameter(4, acao);
            query.setParameter(5, entidade);
            query.setParameter(6, detalhes);
            query.executeUpdate();

            em.getTransaction().commit();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Falha ao registrar ação de auditoria para o utilizador: " + usuarioNome, e);
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
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
            // Formatação da data para uma leitura mais fácil
            Timestamp timestamp = (Timestamp) record[0];
            row.add(timestamp != null ? timestamp.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : ""); // data_evento
            row.add(record[1] != null ? record[1].toString() : "Sistema"); // usuario_nome

            String acao = record[2] != null ? record[2].toString() : "";
            if (acao.equals("LOGIN_SUCESSO")) {
                row.add("SUCESSO");
            } else if (acao.equals("LOGIN_FALHA")) {
                row.add("FALHA");
            } else {
                row.add(acao);
            }

            row.add(record[3] != null ? record[3].toString() : ""); // entidade
            row.add(record[4] != null ? record[4].toString() : ""); // detalhes
            data.add(row);
        }
        return data;
    }
}