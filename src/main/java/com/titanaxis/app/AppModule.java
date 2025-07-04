package com.titanaxis.app;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.titanaxis.repository.*;
import com.titanaxis.repository.impl.*;
import com.titanaxis.service.*;

/**
 * Módulo do Guice para configurar as injeções de dependência da aplicação.
 * Define como as interfaces devem ser mapeadas para as suas implementações concretas.
 */
public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // --- Repositórios ---
        // Quando alguém pedir uma AuditoriaRepository, o Guice irá fornecer uma instância de AuditoriaRepositoryImpl.
        bind(AuditoriaRepository.class).to(AuditoriaRepositoryImpl.class).in(Singleton.class);
        bind(UsuarioRepository.class).to(UsuarioRepositoryImpl.class).in(Singleton.class);
        bind(CategoriaRepository.class).to(CategoriaRepositoryImpl.class).in(Singleton.class);
        bind(ClienteRepository.class).to(ClienteRepositoryImpl.class).in(Singleton.class);
        bind(ProdutoRepository.class).to(ProdutoRepositoryImpl.class).in(Singleton.class);
        bind(VendaRepository.class).to(VendaRepositoryImpl.class).in(Singleton.class);
        bind(MovimentoRepository.class).to(MovimentoRepositoryImpl.class).in(Singleton.class);

        // --- Serviços ---
        // O Guice é inteligente o suficiente para ver que AuthService precisa de um UsuarioRepository,
        // AuditoriaRepository e TransactionService, e irá injetá-los automaticamente.
        bind(TransactionService.class).in(Singleton.class);
        bind(AuthService.class).in(Singleton.class);
        bind(CategoriaService.class).in(Singleton.class);
        bind(ClienteService.class).in(Singleton.class);
        bind(ProdutoService.class).in(Singleton.class);
        bind(VendaService.class).in(Singleton.class);
        bind(RelatorioService.class).in(Singleton.class);
        bind(AlertaService.class).in(Singleton.class);
        bind(MovimentoService.class).in(Singleton.class);

        // --- Contexto da Aplicação ---
        // O próprio AppContext pode ser gerido pelo Guice.
        bind(AppContext.class).in(Singleton.class);
    }
}