package com.titanaxis.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Vector;

public interface AuditoriaRepository {
    /**
     * Registra uma ação na base de dados de auditoria usando um EntityManager existente.
     * A operação de registo fará parte da transação gerida pela camada de serviço.
     *
     * @param usuarioId   ID do utilizador que realizou a ação.
     * @param usuarioNome Nome do utilizador.
     * @param acao        Ação realizada (ex: CRIAÇÃO, ATUALIZAÇÃO).
     * @param entidade    A entidade que foi afetada (ex: Cliente, Produto).
     * @param detalhes    Uma descrição da ação.
     * @param em          O EntityManager da transação atual.
     */
    void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, String detalhes, EntityManager em);

    // Métodos para buscar logs de auditoria
    List<Vector<Object>> getAuditoriaAcoes(EntityManager em);
    List<Vector<Object>> getAuditoriaAcesso(EntityManager em);
}