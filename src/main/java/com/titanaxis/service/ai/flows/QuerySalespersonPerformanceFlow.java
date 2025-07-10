package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MetaVenda;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.FinanceiroService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class QuerySalespersonPerformanceFlow extends AbstractConversationFlow {

    private final AuthService authService;
    private final AnalyticsService analyticsService;
    private final FinanceiroService financeiroService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Inject
    public QuerySalespersonPerformanceFlow(AuthService authService, AnalyticsService analyticsService, FinanceiroService financeiroService) {
        this.authService = authService;
        this.analyticsService = analyticsService;
        this.financeiroService = financeiroService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_SALESPERSON_PERFORMANCE;
    }

    @Override
    protected void defineSteps() {
        steps.put("username", new Step(
                "Claro. Qual o nome do utilizador que gostaria de analisar?",
                this::isUserValid,
                "Utilizador não encontrado. Por favor, verifique o nome."
        ));
    }

    private boolean isUserValid(String username) {
        try {
            return authService.listarUsuarios().stream().anyMatch(u -> u.getNomeUsuario().equalsIgnoreCase(username));
        } catch (PersistenciaException e) {
            return false;
        }
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String username = (String) conversationData.get("username");

        try {
            Optional<Usuario> userOpt = authService.listarUsuarios().stream()
                    .filter(u -> u.getNomeUsuario().equalsIgnoreCase(username))
                    .findFirst();

            if (userOpt.isEmpty()) {
                return new AssistantResponse("Não consegui encontrar o utilizador '" + username + "'.");
            }

            Usuario user = userOpt.get();
            YearMonth currentMonth = YearMonth.now();
            LocalDate startOfMonth = currentMonth.atDay(1);
            LocalDate endOfMonth = currentMonth.atEndOfMonth();

            double valorVendido = analyticsService.getVendasPorVendedorNoPeriodo(user.getId(), startOfMonth, endOfMonth);

            // Buscar a meta do utilizador para o mês corrente
            String anoMes = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Optional<MetaVenda> metaOpt = financeiroService.listarMetas().stream()
                    .filter(m -> m.getUsuario().getId() == user.getId() && m.getAnoMes().equals(anoMes))
                    .findFirst();

            StringBuilder response = new StringBuilder();
            response.append(String.format("Desempenho de '%s' para %s:\n", user.getNomeUsuario(), currentMonth.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", new Locale("pt", "BR")))));
            response.append(String.format("- Total Vendido: %s", currencyFormat.format(valorVendido)));

            if (metaOpt.isPresent()) {
                MetaVenda meta = metaOpt.get();
                double metaValue = meta.getValorMeta();
                double progresso = (metaValue > 0) ? (valorVendido / metaValue) * 100 : 0;
                response.append(String.format("\n- Meta do Mês: %s", currencyFormat.format(metaValue)));
                response.append(String.format("\n- Progresso da Meta: %.1f%%", progresso));
            } else {
                response.append("\n- Nenhuma meta de vendas foi definida para este utilizador no período.");
            }

            return new AssistantResponse(response.toString());

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar os dados de desempenho.");
        }
    }
}