package com.titanaxis.presenter;

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
        carregarDadosIniciais();
    }

    private void carregarDadosIniciais() {
        List<Cliente> clientes = clienteService.listarTodos();
        view.setClientesNaTabela(clientes);
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
            clienteService.salvar(cliente, ator);
            view.mostrarMensagem("Sucesso", "Cliente " + (isUpdate ? "atualizado" : "adicionado") + " com sucesso!", false);
            aoLimpar();
            carregarDadosIniciais();
        } catch (Exception e) {
            view.mostrarMensagem("Erro", "Erro ao salvar cliente: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoApagar() {
        if (view.getId().isEmpty()) {
            view.mostrarMensagem("Aviso", "Selecione um cliente para eliminar.", true);
            return;
        }

        if (!view.mostrarConfirmacao("Confirmar Eliminação", "Tem certeza que deseja eliminar este cliente?")) {
            return;
        }

        int id = Integer.parseInt(view.getId());
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        try {
            clienteService.deletar(id, ator);
            view.mostrarMensagem("Sucesso", "Cliente eliminado com sucesso!", false);
            aoLimpar();
            carregarDadosIniciais();
        } catch (Exception e) {
            view.mostrarMensagem("Erro", "Erro ao eliminar cliente: " + e.getMessage(), true);
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
        String termo = view.getTermoBusca();
        if (termo != null && !termo.trim().isEmpty()) {
            view.setClientesNaTabela(clienteService.buscarPorNome(termo));
        } else {
            carregarDadosIniciais();
        }
    }

    @Override
    public void aoLimparBusca() {
        view.setTermoBusca("");
        carregarDadosIniciais();
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