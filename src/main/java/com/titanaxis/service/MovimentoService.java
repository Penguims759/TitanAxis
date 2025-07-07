package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.repository.MovimentoRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MovimentoService {
    private final MovimentoRepository movimentoRepository;
    private final TransactionService transactionService;

    @Inject
    public MovimentoService(MovimentoRepository movimentoRepository, TransactionService transactionService) {
        this.movimentoRepository = movimentoRepository;
        this.transactionService = transactionService;
    }

    public List<MovimentoEstoque> listarTodosMovimentos() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(movimentoRepository::findAll);
    }

    public List<MovimentoEstoque> listarMovimentosPorPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        if (inicio == null || fim == null) {
            return listarTodosMovimentos();
        }
        return transactionService.executeInTransactionWithResult(em ->
                movimentoRepository.findBetweenDates(inicio.atStartOfDay(), fim.atTime(LocalTime.MAX), em));
    }
}