package com.titanaxis.app;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.titanaxis.repository.*;
import com.titanaxis.repository.impl.*;
import com.titanaxis.service.*;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.service.ai.FlowValidationService; // NOVO IMPORT
import com.titanaxis.service.ai.NLPIntentService;
import com.titanaxis.service.ai.NerService;
import com.titanaxis.service.ai.flows.*;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        // Repositórios
        bind(AuditoriaRepository.class).to(AuditoriaRepositoryImpl.class).in(Singleton.class);
        bind(UsuarioRepository.class).to(UsuarioRepositoryImpl.class).in(Singleton.class);
        bind(CategoriaRepository.class).to(CategoriaRepositoryImpl.class).in(Singleton.class);
        bind(ClienteRepository.class).to(ClienteRepositoryImpl.class).in(Singleton.class);
        bind(ProdutoRepository.class).to(ProdutoRepositoryImpl.class).in(Singleton.class);
        bind(VendaRepository.class).to(VendaRepositoryImpl.class).in(Singleton.class);
        bind(MovimentoRepository.class).to(MovimentoRepositoryImpl.class).in(Singleton.class);
        bind(FornecedorRepository.class).to(FornecedorRepositoryImpl.class).in(Singleton.class);
        bind(DevolucaoRepository.class).to(DevolucaoRepositoryImpl.class).in(Singleton.class);
        bind(FinanceiroRepository.class).to(FinanceiroRepositoryImpl.class).in(Singleton.class);
        bind(HabitoRepository.class).to(HabitoRepositoryImpl.class).in(Singleton.class);

        // Serviços
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
        bind(FornecedorService.class).in(Singleton.class);
        bind(DevolucaoService.class).in(Singleton.class);
        bind(FinanceiroService.class).in(Singleton.class);
        bind(UserHabitService.class).in(Singleton.class);

        // --- Configuração da IA ---
        bind(NLPIntentService.class).in(Singleton.class);
        bind(NerService.class).in(Singleton.class);
        bind(AIAssistantService.class).in(Singleton.class);
        bind(FlowValidationService.class).in(Singleton.class); // NOVO BIND

        Multibinder<ConversationFlow> flowBinder = Multibinder.newSetBinder(binder(), ConversationFlow.class);
        flowBinder.addBinding().to(StartSaleFlow.class);
        flowBinder.addBinding().to(ExecuteFullSaleFlow.class);
        flowBinder.addBinding().to(CreateUserFlow.class);
        flowBinder.addBinding().to(CreateProductFlow.class);
        flowBinder.addBinding().to(CreateCategoryFlow.class);
        flowBinder.addBinding().to(CreateClientFlow.class);
        flowBinder.addBinding().to(CreateFornecedorFlow.class);
        flowBinder.addBinding().to(CreatePurchaseOrderFlow.class);
        flowBinder.addBinding().to(ManageStockFlow.class);
        flowBinder.addBinding().to(AdjustStockFlow.class);
        flowBinder.addBinding().to(AdjustStockPercentageFlow.class);
        flowBinder.addBinding().to(UpdateProductFlow.class);
        flowBinder.addBinding().to(UpdateLoteFlow.class);
        flowBinder.addBinding().to(QueryStockFlow.class);
        flowBinder.addBinding().to(QueryClientDetailsFlow.class);
        flowBinder.addBinding().to(QueryProductLotsFlow.class);
        flowBinder.addBinding().to(QueryMovementHistoryFlow.class);
        flowBinder.addBinding().to(GenerateReportFlow.class);
        flowBinder.addBinding().to(QueryTopProductFlow.class);
        flowBinder.addBinding().to(QueryClientHistoryFlow.class);
        flowBinder.addBinding().to(QueryLowStockFlow.class);
        flowBinder.addBinding().to(QueryTopClientsFlow.class);
        flowBinder.addBinding().to(QueryExpiringLotsFlow.class);
        flowBinder.addBinding().to(QueryFinancialSummaryFlow.class);
        flowBinder.addBinding().to(QuerySalespersonPerformanceFlow.class);
        flowBinder.addBinding().to(QuerySalesComparisonFlow.class);
        flowBinder.addBinding().to(QuerySystemInsightsFlow.class);
        flowBinder.addBinding().to(QueryClientCreditFlow.class);
        flowBinder.addBinding().to(QueryUserHabitsFlow.class);
        flowBinder.addBinding().to(CreateManualHabitFlow.class);

        bind(AppContext.class).in(Singleton.class);
    }
}