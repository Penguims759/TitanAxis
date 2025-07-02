// src/main/java/com/titanaxis/repository/AuditoriaRepository.java
package com.titanaxis.repository;

public interface AuditoriaRepository {
    void registrarAcao(Integer usuarioId, String usuarioNome, String acao, String entidade, String detalhes);
}