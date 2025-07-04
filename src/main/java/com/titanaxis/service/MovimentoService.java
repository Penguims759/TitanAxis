package com.titanaxis.service;

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

    public List<MovimentoEstoque> listarTodosMovimentos() {
        return transactionService.executeInTransactionWithResult(em ->
                movimentoRepository.findAll(em)
        );
    }
}