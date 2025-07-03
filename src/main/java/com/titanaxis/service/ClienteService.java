package com.titanaxis.service;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ClienteRepository;

import java.util.List;

public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final TransactionService transactionService;

    public ClienteService(ClienteRepository clienteRepository, TransactionService transactionService) {
        this.clienteRepository = clienteRepository;
        this.transactionService = transactionService;
    }

    public List<Cliente> listarTodos() { return clienteRepository.findAll(); }
    public List<Cliente> listarTodosParaVenda() { return clienteRepository.findAll(); }
    public List<Cliente> buscarPorNome(String nome) { return clienteRepository.findByNomeContaining(nome); }

    public Cliente salvar(Cliente cliente, Usuario usuarioLogado) throws Exception {
        if (usuarioLogado == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        // CORREÇÃO: Chamamos o método que retorna um resultado.
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