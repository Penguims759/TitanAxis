// src/main/java/com/titanaxis/app/AppContext.java
package com.titanaxis.app;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.titanaxis.service.*;

@Singleton
public class AppContext {

    private final AuthService authService;
    private final CategoriaService categoriaService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final RelatorioService relatorioService;
    private final AlertaService alertaService;
    private final MovimentoService movimentoService;
    private final AIAssistantService aiAssistantService;
    private final AnalyticsService analyticsService;

    @Inject
    public AppContext(AuthService authService, CategoriaService categoriaService,
                      ClienteService clienteService, ProdutoService produtoService,
                      VendaService vendaService, RelatorioService relatorioService,
                      AlertaService alertaService, MovimentoService movimentoService,
                      AIAssistantService aiAssistantService, AnalyticsService analyticsService) {
        this.authService = authService;
        this.categoriaService = categoriaService;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.vendaService = vendaService;
        this.relatorioService = relatorioService;
        this.alertaService = alertaService;
        this.movimentoService = movimentoService;
        this.aiAssistantService = aiAssistantService;
        this.analyticsService = analyticsService;
    }

    public AuthService getAuthService() { return authService; }
    public CategoriaService getCategoriaService() { return categoriaService; }
    public ClienteService getClienteService() { return clienteService; }
    public ProdutoService getProdutoService() { return produtoService; }
    public VendaService getVendaService() { return vendaService; }
    public RelatorioService getRelatorioService() { return relatorioService; }
    public AlertaService getAlertaService() { return alertaService; }
    public MovimentoService getMovimentoService() { return movimentoService; }
    public AIAssistantService getAIAssistantService() { return aiAssistantService; }
    public AnalyticsService getAnalyticsService() { return analyticsService; }
}