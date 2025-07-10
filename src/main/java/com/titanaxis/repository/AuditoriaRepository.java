package com.titanaxis.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Vector;

public interface AuditoriaRepository {
    void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, String detalhes, EntityManager em);
    void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, Integer entidadeId, String detalhes, EntityManager em);
    List<Vector<Object>> getAuditoriaAcoes(EntityManager em);
    List<Vector<Object>> getAuditoriaAcesso(EntityManager em);
    List<Object[]> findRecentActivity(int limit, EntityManager em);
    List<Object[]> findUserActionsForHabitAnalysis(int usuarioId, int days, EntityManager em);
}