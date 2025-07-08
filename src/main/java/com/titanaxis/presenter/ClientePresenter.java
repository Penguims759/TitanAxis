package com.titanaxis.presenter;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ClienteService;
import com.titanaxis.view.interfaces.ClienteView;

import java.util.List;

public class ClientePresenter implements ClienteView.ClienteViewListener {

    private final ClienteView view;
    private final ClienteService clienteService;
    private final AuthService authService;

    public ClientePresenter(ClienteView view, ClienteService clienteService, AuthService authService) {
        this.view = view;
        this.clienteService = clienteService;
        this.authService = authService;
        this.view.setListener(this);
        aoCarregarDadosIniciais();
    }

    @Override
    public void aoCarregarDadosIniciais() {
        try {
            List<Cliente> clientes = clienteService.listarTodos();
            view.setClientesNaTabela(clientes);
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Falha ao carregar clientes: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoSalvar() {
        String nome = view.getNome().trim();
        if (nome.isEmpty()) {
            view.mostrarMensagem("Erro de Validação", "O nome do cliente é obrigatório.", true);
            return;
        }

        boolean isUpdate = !view.getId().isEmpty();
        int id = isUpdate ? Integer.parseInt(view.getId()) : 0;
        Cliente cliente = new Cliente(id, nome, view.getContato().trim(), view.getEndereco().trim());
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        try {
            Cliente clienteSalvo = clienteService.salvar(cliente, ator);
            if (isUpdate) {
                view.atualizarClienteNaTabela(clienteSalvo);
                view.mostrarMensagem("Sucesso", "Cliente atualizado com sucesso!", false);
            } else {
                view.adicionarClienteNaTabela(clienteSalvo);
                view.mostrarMensagem("Sucesso", "Cliente adicionado com sucesso!", false);
            }
            aoLimpar();
        } catch (UtilizadorNaoAutenticadoException e) {
            view.mostrarMensagem("Erro de Autenticação", e.getMessage(), true);
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Ocorreu um erro ao salvar o cliente: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoApagar() {
        if (view.getId().isEmpty()) {
            view.mostrarMensagem("Aviso", "Selecione um cliente para eliminar.", true);
            return;
        }

        String nomeCliente = view.getNome();
        String mensagem = String.format("Tem certeza que deseja eliminar o cliente '%s'?", nomeCliente);

        if (!view.mostrarConfirmacao("Confirmar Eliminação", mensagem)) {
            return;
        }
        int id = Integer.parseInt(view.getId());
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        try {
            clienteService.deletar(id, ator);
            view.removerClienteDaTabela(id);
            view.mostrarMensagem("Sucesso", "Cliente eliminado com sucesso!", false);
            aoLimpar();
        } catch (UtilizadorNaoAutenticadoException e) {
            view.mostrarMensagem("Erro de Autenticação", e.getMessage(), true);
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Ocorreu um erro ao eliminar o cliente: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoLimpar() {
        view.setId("");
        view.setNome("");
        view.setContato("");
        view.setEndereco("");
        view.clearTableSelection();
    }

    @Override
    public void aoBuscar() {
        try {
            String termo = view.getTermoBusca();
            if (termo != null && !termo.trim().isEmpty()) {
                view.setClientesNaTabela(clienteService.buscarPorNome(termo));
            } else {
                aoCarregarDadosIniciais();
            }
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Falha ao buscar clientes: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoLimparBusca() {
        view.setTermoBusca("");
        aoCarregarDadosIniciais();
    }

    @Override
    public void aoSelecionarCliente(Cliente cliente) {
        if (cliente != null) {
            view.setId(String.valueOf(cliente.getId()));
            view.setNome(cliente.getNome());
            view.setContato(cliente.getContato());
            view.setEndereco(cliente.getEndereco());
        }
    }
}