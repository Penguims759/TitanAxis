// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/service/MovimentoService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.repository.MovimentoRepository;
import java.util.List;

public class MovimentoService {
    private final MovimentoRepository movimentoRepository;
    private final TransactionService transactionService;

    @Inject
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