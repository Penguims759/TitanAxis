package com.titanaxis.service;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.repository.MovimentoRepository;
import java.util.List;

public class MovimentoService {
    private final MovimentoRepository movimentoRepository;
    private final TransactionService transactionService;

    public MovimentoService(MovimentoRepository movimentoRepository, TransactionService transactionService) {
        this.movimentoRepository = movimentoRepository;
        this.transactionService = transactionService;
    }

    // ALTERADO: Adicionada a declaração "throws"
    public List<MovimentoEstoque> listarTodosMovimentos() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                movimentoRepository.findAll(em)
        );
    }
}