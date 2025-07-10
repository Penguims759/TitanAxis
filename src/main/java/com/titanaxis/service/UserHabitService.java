package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.auditoria.HabitoUsuario;
import com.titanaxis.repository.AuditoriaRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class UserHabitService {

    private final AuditoriaRepository auditoriaRepository;
    private final TransactionService transactionService;

    @Inject
    public UserHabitService(AuditoriaRepository auditoriaRepository, TransactionService transactionService) {
        this.auditoriaRepository = auditoriaRepository;
        this.transactionService = transactionService;
    }

    public List<HabitoUsuario> findHabitsForToday(int usuarioId) throws PersistenciaException {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        // Busca o histórico de ações dos últimos 90 dias para ter uma base estatística.
        List<Object[]> historico = transactionService.executeInTransactionWithResult(em ->
                auditoriaRepository.findUserActionsForHabitAnalysis(usuarioId, 90, em)
        );

        // Agrupa as ações por [Ação, DiaDaSemana] e conta as ocorrências.
        return historico.stream()
                .collect(Collectors.groupingBy(
                        row -> new HabitoUsuario((String) row[0], ((java.sql.Timestamp) row[1]).toLocalDateTime().getDayOfWeek()),
                        Collectors.counting()
                ))
                .entrySet().stream()
                // Filtra apenas os hábitos que ocorreram hoje e que aconteceram mais de 2 vezes (para ser considerado um hábito).
                .filter(entry -> entry.getKey().getDiaDaSemana() == today && entry.getValue() > 2)
                .map(entry -> entry.getKey())
                .distinct()
                .collect(Collectors.toList());
    }
}