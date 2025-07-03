package com.titanaxis.app;

import com.titanaxis.repository.*;
import com.titanaxis.repository.impl.*;
import com.titanaxis.service.*;

public class AppContext {

    private final AuthService authService;
    private final CategoriaService categoriaService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final RelatorioService relatorioService;
    private final AlertaService alertaService;
    private final TransactionService transactionService;

    public AppContext() {
        final AuditoriaRepository auditoriaRepository = new AuditoriaRepositoryImpl();
        final UsuarioRepository usuarioRepository = new UsuarioRepositoryImpl(auditoriaRepository);
        final CategoriaRepository categoriaRepository = new CategoriaRepositoryImpl(auditoriaRepository);
        final ClienteRepository clienteRepository = new ClienteRepositoryImpl(auditoriaRepository);
        final ProdutoRepository produtoRepository = new ProdutoRepositoryImpl(auditoriaRepository);
        final VendaRepository vendaRepository = new VendaRepositoryImpl(auditoriaRepository);

        this.transactionService = new TransactionService();
        this.authService = new AuthService(usuarioRepository, auditoriaRepository, transactionService);
        this.categoriaService = new CategoriaService(categoriaRepository, transactionService);
        this.clienteService = new ClienteService(clienteRepository, transactionService);
        this.produtoService = new ProdutoService(produtoRepository, transactionService);
        this.vendaService = new VendaService(vendaRepository, transactionService);
        this.relatorioService = new RelatorioService(produtoRepository, vendaRepository);
        this.alertaService = new AlertaService(produtoRepository);
    }

    public AuthService getAuthService() { return authService; }
    public CategoriaService getCategoriaService() { return categoriaService; }
    public ClienteService getClienteService() { return clienteService; }
    public ProdutoService getProdutoService() { return produtoService; }
    public VendaService getVendaService() { return vendaService; }
    public RelatorioService getRelatorioService() { return relatorioService; }
    public AlertaService getAlertaService() { return alertaService; }
}