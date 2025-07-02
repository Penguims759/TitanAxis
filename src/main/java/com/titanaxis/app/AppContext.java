package com.titanaxis.app;

import com.titanaxis.repository.impl.*;
import com.titanaxis.service.*;

/**
 * Contexto da Aplicação (Service Locator / Dependency Injection Container).
 * Responsável por criar e gerir as instâncias de todos os serviços e repositórios,
 * garantindo que apenas uma instância de cada um exista (Singleton).
 */
public class AppContext {

    // Serviços - os repositórios agora são detalhes de implementação dos serviços
    private final AuthService authService;
    private final CategoriaService categoriaService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final RelatorioService relatorioService;
    private final AlertaService alertaService;


    public AppContext() {
        // Inicialização dos Serviços
        // Cada serviço é responsável por criar o seu próprio repositório
        this.authService = new AuthService();
        this.categoriaService = new CategoriaService();
        this.clienteService = new ClienteService();
        this.produtoService = new ProdutoService();
        this.vendaService = new VendaService();
        this.relatorioService = new RelatorioService();
        this.alertaService = new AlertaService();
    }

    // Getters para que o resto da aplicação possa aceder aos serviços
    public AuthService getAuthService() {
        return authService;
    }

    public CategoriaService getCategoriaService() {
        return categoriaService;
    }

    public ClienteService getClienteService() {
        return clienteService;
    }

    public ProdutoService getProdutoService() {
        return produtoService;
    }

    public VendaService getVendaService() {
        return vendaService;
    }

    public RelatorioService getRelatorioService() {
        return relatorioService;
    }

    public AlertaService getAlertaService() {
        return alertaService;
    }
}