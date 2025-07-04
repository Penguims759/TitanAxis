package com.titanaxis.repository.impl;

import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.DatabaseConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditoriaRepositoryImpl implements AuditoriaRepository {
    private static final Logger logger = AppLogger.getLogger();

    @Override
    public void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, String detalhes) {
        // ... (método existente sem alterações)
    }

    // ADICIONADO: Implementação da busca de logs de ações
    @Override
    public List<Vector<Object>> getAuditoriaAcoes(EntityManager em) {
        String sql = "SELECT data_evento, usuario_nome, acao, entidade, detalhes FROM auditoria_logs " +
                "WHERE acao NOT LIKE 'LOGIN_%' ORDER BY id DESC";
        return fetchAuditoriaData(em, sql);
    }

    // ADICIONADO: Implementação da busca de logs de acesso
    @Override
    public List<Vector<Object>> getAuditoriaAcesso(EntityManager em) {
        String sql = "SELECT data_evento, usuario_nome, acao, entidade, detalhes FROM auditoria_logs " +
                "WHERE acao LIKE 'LOGIN_%' ORDER BY id DESC";
        return fetchAuditoriaData(em, sql);
    }

    private List<Vector<Object>> fetchAuditoriaData(EntityManager em, String sql) {
        List<Vector<Object>> data = new ArrayList<>();
        Query query = em.createNativeQuery(sql);
        List<Object[]> resultList = query.getResultList();

        for (Object[] record : resultList) {
            Vector<Object> row = new Vector<>();
            row.add(record[0] != null ? record[0].toString() : ""); // data_evento
            row.add(record[1] != null ? record[1].toString() : ""); // usuario_nome

            String acao = record[2] != null ? record[2].toString() : "";
            if (acao.equals("LOGIN_SUCESSO")) row.add("SUCESSO");
            else if (acao.equals("LOGIN_FALHA")) row.add("FALHA");
            else row.add(acao);

            row.add(record[3] != null ? record[3].toString() : ""); // entidade
            row.add(record[4] != null ? record[4].toString() : ""); // detalhes
            data.add(row);
        }
        return data;
    }
}