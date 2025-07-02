// src/main/java/com/titanaxis/repository/impl/AuditoriaRepositoryImpl.java
package com.titanaxis.repository.impl;

import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditoriaRepositoryImpl implements AuditoriaRepository {
    private static final Logger logger = AppLogger.getLogger();

    @Override
    public void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, String detalhes) {
        String sql = "INSERT INTO auditoria_logs (usuario_id, usuario_nome, acao, entidade, detalhes) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (usuarioId != null) {
                ps.setInt(1, usuarioId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            ps.setString(2, usuarioNome);
            ps.setString(3, acao);
            ps.setString(4, entidade);
            ps.setString(5, detalhes);

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao registrar log de auditoria.", e);
        }
    }
}