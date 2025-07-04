package com.titanaxis.service;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ClienteRepository;

import java.util.List;
import java.util.Optional;

public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final TransactionService transactionService;

    public ClienteService(ClienteRepository clienteRepository, TransactionService transactionService) {
        this.clienteRepository = clienteRepository;
        this.transactionService = transactionService;
    }

    public List<Cliente> listarTodos() {
        return transactionService.executeInTransactionWithResult(em ->
                clienteRepository.findAll(em)
        );
    }

    public Optional<Cliente> buscarPorId(int id) {
        return transactionService.executeInTransactionWithResult(em ->
                clienteRepository.findById(id, em)
        );
    }

    public List<Cliente> buscarPorNome(String nome) {
        return transactionService.executeInTransactionWithResult(em ->
                clienteRepository.findByNomeContaining(nome, em)
        );
    }

    public Cliente salvar(Cliente cliente, Usuario usuarioLogado) throws Exception {
        if (usuarioLogado == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        return transactionService.executeInTransactionWithResult(em ->
                clienteRepository.save(cliente, usuarioLogado, em)
        );
    }

    public void deletar(int id, Usuario usuarioLogado) throws Exception {
        if (usuarioLogado == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        transactionService.executeInTransaction(em ->
                clienteRepository.deleteById(id, usuarioLogado, em)
        );
    }
}