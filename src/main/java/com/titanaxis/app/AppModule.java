// src/main/java/com/titanaxis/app/AppModule.java
package com.titanaxis.app;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.titanaxis.repository.*;
import com.titanaxis.repository.impl.*;
import com.titanaxis.service.*;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // --- Repositórios ---
        bind(AuditoriaRepository.class).to(AuditoriaRepositoryImpl.class).in(Singleton.class);
        bind(UsuarioRepository.class).to(UsuarioRepositoryImpl.class).in(Singleton.class);
        bind(CategoriaRepository.class).to(CategoriaRepositoryImpl.class).in(Singleton.class);
        bind(ClienteRepository.class).to(ClienteRepositoryImpl.class).in(Singleton.class);
        bind(ProdutoRepository.class).to(ProdutoRepositoryImpl.class).in(Singleton.class);
        bind(VendaRepository.class).to(VendaRepositoryImpl.class).in(Singleton.class);
        bind(MovimentoRepository.class).to(MovimentoRepositoryImpl.class).in(Singleton.class);

        // --- Serviços ---
        bind(TransactionService.class).in(Singleton.class);
        bind(AuthService.class).in(Singleton.class);
        bind(CategoriaService.class).in(Singleton.class);
        bind(ClienteService.class).in(Singleton.class);
        bind(ProdutoService.class).in(Singleton.class);
        bind(VendaService.class).in(Singleton.class);
        bind(RelatorioService.class).in(Singleton.class);
        bind(AlertaService.class).in(Singleton.class);
        bind(MovimentoService.class).in(Singleton.class);
        bind(AIAssistantService.class).in(Singleton.class);
        bind(AnalyticsService.class).in(Singleton.class);

        // --- Contexto da Aplicação ---
        bind(AppContext.class).in(Singleton.class);
    }
}