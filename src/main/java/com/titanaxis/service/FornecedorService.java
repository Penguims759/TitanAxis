// src/main/java/com/titanaxis/service/FornecedorService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Fornecedor;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.FornecedorRepository;
import com.titanaxis.util.I18n; // Importado
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
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
        }
        try {
            return transactionService.executeInTransactionWithResult(em -> {
                Optional<Fornecedor> existente = fornecedorRepository.findByNome(fornecedor.getNome(), em);
                if (existente.isPresent() && existente.get().getId() != fornecedor.getId()) {
                    throw new RuntimeException(I18n.getString("service.supplier.error.nameExists", fornecedor.getNome())); // ALTERADO
                }
                return fornecedorRepository.save(fornecedor, ator, em);
            });
        } catch (RuntimeException e) {
            if(e.getMessage().contains(I18n.getString("service.supplier.error.nameExists.check"))) { // ALTERADO
                throw new NomeDuplicadoException(e.getMessage());
            }
            throw new PersistenciaException(e.getMessage(), e);
        }
    }

    public void deletar(int id, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
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