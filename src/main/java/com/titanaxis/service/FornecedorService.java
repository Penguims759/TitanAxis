// src/main/java/com/titanaxis/service/FornecedorService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Fornecedor;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.FornecedorRepository;
import java.util.List;
import java.util.Optional;

public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final TransactionService transactionService;

    @Inject
    public FornecedorService(FornecedorRepository fornecedorRepository, TransactionService transactionService) {
        this.fornecedorRepository = fornecedorRepository;
        this.transactionService = transactionService;
    }

    public Fornecedor salvar(Fornecedor fornecedor, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException, NomeDuplicadoException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        try {
            return transactionService.executeInTransactionWithResult(em -> {
                Optional<Fornecedor> existente = fornecedorRepository.findByNome(fornecedor.getNome(), em);
                if (existente.isPresent() && existente.get().getId() != fornecedor.getId()) {
                    throw new RuntimeException("Já existe um fornecedor com o nome '" + fornecedor.getNome() + "'.");
                }
                return fornecedorRepository.save(fornecedor, ator, em);
            });
        } catch (RuntimeException e) {
            if(e.getMessage().contains("Já existe um fornecedor com o nome")) {
                throw new NomeDuplicadoException(e.getMessage());
            }
            throw new PersistenciaException(e.getMessage(), e);
        }
    }

    public void deletar(int id, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        transactionService.executeInTransaction(em -> fornecedorRepository.deleteById(id, ator, em));
    }

    public List<Fornecedor> listarTodos() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(fornecedorRepository::findAll);
    }

    public Optional<Fornecedor> buscarPorId(int id) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> fornecedorRepository.findById(id, em));
    }

    public List<Fornecedor> buscarPorNome(String termo) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> fornecedorRepository.findByNomeContaining(termo, em));
    }
}