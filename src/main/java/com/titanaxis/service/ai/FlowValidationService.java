package com.titanaxis.service.ai;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.service.*;

@Singleton
public class FlowValidationService {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final FornecedorService fornecedorService;
    private final AuthService authService;
    private final ClienteService clienteService;

    @Inject
    public FlowValidationService(ProdutoService produtoService, CategoriaService categoriaService, FornecedorService fornecedorService, AuthService authService, ClienteService clienteService) {
        this.produtoService = produtoService;
        this.categoriaService = categoriaService;
        this.fornecedorService = fornecedorService;
        this.authService = authService;
        this.clienteService = clienteService;
    }

    public boolean isProdutoValido(String nomeProduto) {
        try {
            return produtoService.produtoExiste(nomeProduto);
        } catch (PersistenciaException e) {
            return false;
        }
    }

    public boolean isLoteValido(String nomeProduto, String numeroLote) {
        try {
            return produtoService.loteExiste(nomeProduto, numeroLote);
        } catch (PersistenciaException e) {
            return false;
        }
    }

    public boolean isCategoriaValida(String nomeCategoria) {
        try {
            return categoriaService.listarTodasCategorias().stream()
                    .anyMatch(c -> c.getNome().equalsIgnoreCase(nomeCategoria));
        } catch (PersistenciaException e) {
            return false;
        }
    }

    public boolean isFornecedorValido(String nomeFornecedor) {
        try {
            return fornecedorService.listarTodos().stream()
                    .anyMatch(f -> f.getNome().equalsIgnoreCase(nomeFornecedor));
        } catch (PersistenciaException e) {
            return false;
        }
    }

    public boolean isUserValido(String username) {
        try {
            return authService.listarUsuarios().stream()
                    .anyMatch(u -> u.getNomeUsuario().equalsIgnoreCase(username));
        } catch (PersistenciaException e) {
            return false;
        }
    }

    public boolean isClienteValido(String nomeCliente) {
        try {
            return clienteService.listarTodos().stream()
                    .anyMatch(c -> c.getNome().equalsIgnoreCase(nomeCliente));
        } catch (PersistenciaException e) {
            return false;
        }
    }
}