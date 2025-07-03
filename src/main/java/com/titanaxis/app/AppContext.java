// penguims759/titanaxis/Penguims759-TitanAxis-7ba36152a6e3502010a8be48ce02c9ed9fcd8bf0/src/main/java/com/titanaxis/app/AppContext.java
package com.titanaxis.app;

import com.titanaxis.repository.*;
import com.titanaxis.repository.impl.*;
import com.titanaxis.service.*;

/**
 * Contexto da Aplicação (Inversion of Control / Dependency Injection Container).
 * Responsável por criar e gerir as instâncias de todos os repositórios e serviços,
 * injetando as dependências necessárias (como repositórios nos serviços).
 */
public class AppContext {

    // Serviços
    private final AuthService authService;
    private final CategoriaService categoriaService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final RelatorioService relatorioService;
    private final AlertaService alertaService;

    public AppContext() {
        // 1. Criação dos Repositórios (as dependências)
        final AuditoriaRepository auditoriaRepository = new AuditoriaRepositoryImpl();
        final UsuarioRepository usuarioRepository = new UsuarioRepositoryImpl(auditoriaRepository);
        final CategoriaRepository categoriaRepository = new CategoriaRepositoryImpl(auditoriaRepository);
        final ClienteRepository clienteRepository = new ClienteRepositoryImpl(auditoriaRepository);
        final ProdutoRepository produtoRepository = new ProdutoRepositoryImpl(auditoriaRepository);
        final VendaRepository vendaRepository = new VendaRepositoryImpl(auditoriaRepository);

        // 2. Criação e Injeção dos Serviços
        // As dependências (repositórios) são passadas via construtor.
        this.authService = new AuthService(usuarioRepository, auditoriaRepository);
        this.categoriaService = new CategoriaService(categoriaRepository);
        this.clienteService = new ClienteService(clienteRepository);
        this.produtoService = new ProdutoService(produtoRepository);
        this.vendaService = new VendaService(vendaRepository);
        this.relatorioService = new RelatorioService(produtoRepository, vendaRepository);
        this.alertaService = new AlertaService(produtoRepository);
    }

    // Getters para que o resto da aplicação possa aceder aos serviços
    public AuthService getAuthService() { return authService; }
    public CategoriaService getCategoriaService() { return categoriaService; }
    public ClienteService getClienteService() { return clienteService; }
    public ProdutoService getProdutoService() { return produtoService; }
    public VendaService getVendaService() { return vendaService; }
    public RelatorioService getRelatorioService() { return relatorioService; }
    public AlertaService getAlertaService() { return alertaService; }
}