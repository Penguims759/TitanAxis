package com.titanaxis.app;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.titanaxis.repository.*;
import com.titanaxis.repository.impl.*;
import com.titanaxis.service.*;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.service.ai.flows.*;

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
        bind(AnalyticsService.class).in(Singleton.class);

        // --- Lógica do Assistente IA ---
        bind(AIAssistantService.class).in(Singleton.class);

        Multibinder<ConversationFlow> flowBinder = Multibinder.newSetBinder(binder(), ConversationFlow.class);

        // NOVO FLUXO
        flowBinder.addBinding().to(StartSaleFlow.class);

        // Fluxos existentes
        flowBinder.addBinding().to(CreateUserFlow.class);
        flowBinder.addBinding().to(CreateProductFlow.class);
        flowBinder.addBinding().to(CreateCategoryFlow.class);
        flowBinder.addBinding().to(ManageStockFlow.class);
        flowBinder.addBinding().to(CreateClientFlow.class);
        flowBinder.addBinding().to(UpdateProductFlow.class);
        flowBinder.addBinding().to(QueryStockFlow.class);
        flowBinder.addBinding().to(QueryClientFlow.class);
        flowBinder.addBinding().to(QueryProductLotsFlow.class);
        flowBinder.addBinding().to(QueryMovementHistoryFlow.class);
        flowBinder.addBinding().to(GenerateReportFlow.class);
        flowBinder.addBinding().to(QueryTopProductFlow.class);
        flowBinder.addBinding().to(QueryClientHistoryFlow.class);
        flowBinder.addBinding().to(QueryLowStockFlow.class);
        flowBinder.addBinding().to(QueryTopClientsFlow.class);
        flowBinder.addBinding().to(QueryExpiringLotsFlow.class);

        // --- Contexto da Aplicação ---
        bind(AppContext.class).in(Singleton.class);
    }
}