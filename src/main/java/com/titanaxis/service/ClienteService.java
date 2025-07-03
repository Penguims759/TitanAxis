// penguims759/titanaxis/Penguims759-TitanAxis-7ba36152a6e3502010a8be48ce02c9ed9fcd8bf0/src/main/java/com/titanaxis/service/ClienteService.java
package com.titanaxis.service;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.repository.impl.ClienteRepositoryImpl;

import java.util.List;

public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public void salvar(Cliente cliente, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        clienteRepository.save(cliente, ator);
    }

    public void deletar(int id, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        clienteRepository.deleteById(id, ator);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContaining(nome);
    }

    // NOVO MÉTODO: Para ser usado pelo painel de vendas
    public List<Cliente> listarTodosParaVenda() {
        return clienteRepository.findAll();
    }
}