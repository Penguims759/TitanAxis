package com.titanaxis.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Vector;

public interface AuditoriaRepository {
    void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, String detalhes);

    // ADICIONADO: Métodos para buscar logs de auditoria
    List<Vector<Object>> getAuditoriaAcoes(EntityManager em);
    List<Vector<Object>> getAuditoriaAcesso(EntityManager em);
}