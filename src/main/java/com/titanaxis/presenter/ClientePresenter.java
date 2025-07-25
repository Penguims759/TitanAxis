package com.titanaxis.presenter;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ClienteService;
import com.titanaxis.util.I18n;
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
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.client.error.load", e.getMessage()), true);
        }
    }

    @Override
    public void aoSalvar() {
        String nome = view.getNome().trim();
        if (nome.isEmpty()) {
            view.mostrarMensagem(I18n.getString("error.validation.title"), I18n.getString("presenter.client.error.nameRequired"), true);
            return;
        }

        boolean isUpdate = !view.getId().isEmpty();
        int id = isUpdate ? Integer.parseInt(view.getId()) : 0;
        Cliente cliente = new Cliente(id, nome, view.getContato().trim(), view.getEndereco().trim());
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        try {
            Cliente clienteSalvo = clienteService.salvar(cliente, ator);
            String successMessage = isUpdate ? I18n.getString("presenter.client.success.update") : I18n.getString("presenter.client.success.add");
            if (isUpdate) {
                view.atualizarClienteNaTabela(clienteSalvo);
                view.mostrarMensagem(I18n.getString("success.title"), successMessage, false);
            } else {
                view.adicionarClienteNaTabela(clienteSalvo);
                view.mostrarMensagem(I18n.getString("success.title"), successMessage, false);
            }
            aoLimpar();
        } catch (UtilizadorNaoAutenticadoException e) {
            view.mostrarMensagem(I18n.getString("error.auth.title"), e.getMessage(), true);
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.client.error.save", e.getMessage()), true);
        }
    }

    @Override
    public void aoApagar() {
        if (view.getId().isEmpty()) {
            view.mostrarMensagem(I18n.getString("warning.title"), I18n.getString("presenter.client.error.selectToDelete"), true);
            return;
        }

        String nomeCliente = view.getNome();
        String mensagem = I18n.getString("presenter.client.confirm.delete", nomeCliente);

        if (!view.mostrarConfirmacao(I18n.getString("presenter.client.confirm.delete.title"), mensagem)) {
            return;
        }
        int id = Integer.parseInt(view.getId());
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        try {
            clienteService.deletar(id, ator);
            view.removerClienteDaTabela(id);
            view.mostrarMensagem(I18n.getString("success.title"), I18n.getString("presenter.client.success.delete"), false);
            aoLimpar();
        } catch (UtilizadorNaoAutenticadoException e) {
            view.mostrarMensagem(I18n.getString("error.auth.title"), e.getMessage(), true);
        } catch (PersistenciaException e) {
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.client.error.delete", e.getMessage()), true);
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
            view.mostrarMensagem(I18n.getString("error.db.title"), I18n.getString("presenter.client.error.search", e.getMessage()), true);
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