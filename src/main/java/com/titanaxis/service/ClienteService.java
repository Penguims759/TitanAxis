package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ClienteRepository;

import java.util.List;
import java.util.Optional;

public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final TransactionService transactionService;

    @Inject
    public ClienteService(ClienteRepository clienteRepository, TransactionService transactionService) {
        this.clienteRepository = clienteRepository;
        this.transactionService = transactionService;
    }

    // ALTERADO: Adicionada a declaração "throws"
    public List<Cliente> listarTodos() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                clienteRepository.findAll(em)
        );
    }

    // ALTERADO: Adicionada a declaração "throws"
    public Optional<Cliente> buscarPorId(int id) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                clienteRepository.findById(id, em)
        );
    }

    // ALTERADO: Adicionada a declaração "throws"
    public List<Cliente> buscarPorNome(String nome) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                clienteRepository.findByNomeContaining(nome, em)
        );
    }

    public Cliente salvar(Cliente cliente, Usuario usuarioLogado) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (usuarioLogado == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        return transactionService.executeInTransactionWithResult(em ->
                clienteRepository.save(cliente, usuarioLogado, em)
        );
    }

    public void deletar(int id, Usuario usuarioLogado) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (usuarioLogado == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        transactionService.executeInTransaction(em ->
                clienteRepository.deleteById(id, usuarioLogado, em)
        );
    }
}