package com.titanaxis.app;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.titanaxis.service.*;

/**
 * O AppContext agora atua como um 'Facade' ou um ponto de acesso central
 * aos serviços da aplicação, com as suas dependências a serem injetadas pelo Guice.
 */
@Singleton // Garante que o Guice só criará uma instância desta classe.
public class AppContext {

    private final AuthService authService;
    private final CategoriaService categoriaService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final RelatorioService relatorioService;
    private final AlertaService alertaService;
    private final MovimentoService movimentoService;

    // A anotação @Inject diz ao Guice para usar este construtor e
    // fornecer automaticamente as instâncias dos serviços necessários.
    @Inject
    public AppContext(AuthService authService, CategoriaService categoriaService,
                      ClienteService clienteService, ProdutoService produtoService,
                      VendaService vendaService, RelatorioService relatorioService,
                      AlertaService alertaService, MovimentoService movimentoService) {
        this.authService = authService;
        this.categoriaService = categoriaService;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.vendaService = vendaService;
        this.relatorioService = relatorioService;
        this.alertaService = alertaService;
        this.movimentoService = movimentoService;
    }

    // Os Getters permanecem exatamente os mesmos.
    public AuthService getAuthService() { return authService; }
    public CategoriaService getCategoriaService() { return categoriaService; }
    public ClienteService getClienteService() { return clienteService; }
    public ProdutoService getProdutoService() { return produtoService; }
    public VendaService getVendaService() { return vendaService; }
    public RelatorioService getRelatorioService() { return relatorioService; }
    public AlertaService getAlertaService() { return alertaService; }
    public MovimentoService getMovimentoService() { return movimentoService; }
}