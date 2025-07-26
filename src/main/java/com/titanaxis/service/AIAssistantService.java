package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.model.ai.ConversationContext;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.service.ai.NLPIntentService;
import com.titanaxis.service.ai.NerService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.StringUtil;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class AIAssistantService {
    private final Set<ConversationFlow> conversationFlows;
    private final ConversationContext context = new ConversationContext();
    private final NLPIntentService nlpIntentService;
    private final NerService nerService;
    private final AuthService authService;
    private final AnalyticsService analyticsService;

    private static final Pattern QUERY_STOCK_PATTERN = Pattern.compile("^(qual o estoque do produto|ver o stock de|estoque de|qual o estoque de)\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUERY_PRODUCT_LOTS_PATTERN = Pattern.compile("^(quais os lotes da|ver lotes do produto|lotes do produto)\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAVIGATION_PATTERN = Pattern.compile("^(ir para|navegar para|abrir o painel de|me leve para)\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXECUTE_SALE_PATTERN = Pattern.compile("vender\\s+(\\d+)\\s+(?:unidades de|de)?\\s*'?([^']*)'?\\s*(?:do lote\\s*'([^']*)')?\\s*(?:para o cliente\\s*'([^']*)')?", Pattern.CASE_INSENSITIVE);

    @Inject
    public AIAssistantService(Set<ConversationFlow> conversationFlows, NLPIntentService nlpIntentService, NerService nerService, AuthService authService, AnalyticsService analyticsService) {
        this.conversationFlows = conversationFlows;
        this.nlpIntentService = nlpIntentService;
        this.nerService = nerService;
        this.authService = authService;
        this.analyticsService = analyticsService;
    }

    public ConversationContext getContext() {
        return context;
    }

    public AssistantResponse processQuery(String query) {
        String normalizedQuery = StringUtil.normalize(query);

        if (isCancelCommand(normalizedQuery)) {
            context.fullReset();
            return new AssistantResponse(I18n.getString("flow.generic.actionCanceled"));
        }

        if (context.isAwaitingInfo()) {
            return handleOngoingConversation(query);
        }

        return handleNewCommand(query);
    }

    private boolean isCancelCommand(String normalizedQuery) {
        return nlpIntentService.getIntent(normalizedQuery) == Intent.DENY;
    }

    private AssistantResponse handleOngoingConversation(String originalQuery) {
        Optional<Action> proactiveActionOpt = context.getPendingProactiveAction();
        if (proactiveActionOpt.isPresent()) {
            return handleProactiveConfirmation(originalQuery, proactiveActionOpt.get());
        }

        ConversationFlow handler = context.getCurrentFlow();
        AssistantResponse response = handler.process(originalQuery, context.getCollectedData());

        if (response.getAction() != Action.AWAITING_INFO) {
            Optional.ofNullable(context.getCollectedData().get("foundEntity")).ifPresent(entity -> {
                context.setLastEntity(entity);
                context.setLastIntent((Intent) context.getCollectedData().get("intent"));
            });
            context.resetFlow();
        }
        return response;
    }

    private AssistantResponse handleProactiveConfirmation(String userInput, Action proactiveAction) {
        Intent userIntent = nlpIntentService.getIntent(StringUtil.normalize(userInput));
        Map<String, Object> params = context.getProactiveActionParams();
        context.resetFlow();

        if (userIntent == Intent.CONFIRM) {
            String entityName = (String) params.get("nome");
            switch (proactiveAction) {
                case PROACTIVE_SUGGEST_ADD_LOTE:
                    return startConversationFlow(Intent.MANAGE_STOCK, entityName);
                case PROACTIVE_SUGGEST_START_SALE:
                    return startConversationFlow(Intent.START_SALE, entityName);
                case PROACTIVE_SUGGEST_CREATE_PURCHASE_ORDER:
                    return startConversationFlow(Intent.CREATE_PURCHASE_ORDER, "para o fornecedor " + entityName);
                default:
                    return new AssistantResponse(I18n.getString("flow.proactive.error.unknown"));
            }
        } else {
            return new AssistantResponse(I18n.getString("flow.proactive.declined"));
        }
    }

    private AssistantResponse handleNewCommand(String originalQuery) {
        String normalizedQuery = StringUtil.normalize(originalQuery);
        Intent intent = nlpIntentService.getIntent(normalizedQuery);

        if (intent == Intent.GREETING || intent == Intent.UNKNOWN) {
            context.fullReset();
        }

        switch (intent) {
            case GREETING:
                return new AssistantResponse(I18n.getString("dashboard.assistant.howCanIHelp"));
            case CHANGE_THEME:
                return handleChangeTheme(normalizedQuery);
            case NAVIGATE_TO:
                return handleNavigation(originalQuery);
            case GUIDE_ADD_LOTE:
                return new AssistantResponse(I18n.getString("flow.guide.addLote"), Action.GUIDE_NAVIGATE_TO_ADD_LOTE, null);
            case GUIDE_ADD_PRODUCT:
                return new AssistantResponse(I18n.getString("flow.guide.addProduct"), Action.GUIDE_NAVIGATE_TO_ADD_PRODUCT, null);
            case UNKNOWN:
                return new AssistantResponse(I18n.getString("flow.error.unknownIntent"), Action.AWAITING_INFO, null);
            default:
                return startConversationFlow(intent, originalQuery);
        }
    }

    public AssistantResponse getInitialGreetingWithInsights() {
        String userName = authService.getUsuarioLogado().map(Usuario::getNomeUsuario).orElse("");
        String greeting = getGreetingByTimeOfDay() + " " + userName + "!";

        try {
            List<String> insights = analyticsService.getSystemInsightsSummaryText();
            if (insights.isEmpty()) {
                return new AssistantResponse(greeting + " " + I18n.getString("dashboard.assistant.howCanIHelp"));
            } else {
                String insightsText = String.join("\n", insights);
                return new AssistantResponse(greeting + "\n" + I18n.getString("dashboard.assistant.insightsFound") + "\n" + insightsText);
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse(greeting + " " + I18n.getString("dashboard.assistant.insightsError"));
        }
    }

    private String getGreetingByTimeOfDay() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.NOON)) {
            return I18n.getString("dashboard.greeting.morning");
        } else if (now.isBefore(LocalTime.of(18, 0))) {
            return I18n.getString("dashboard.greeting.afternoon");
        } else {
            return I18n.getString("dashboard.greeting.evening");
        }
    }

    private AssistantResponse startConversationFlow(Intent intent, String originalQuery) {
        Optional<ConversationFlow> handlerOpt = findHandlerFor(intent);

        if (handlerOpt.isPresent()) {
            ConversationFlow handler = handlerOpt.get();
            context.startFlow(handler);
            context.getCollectedData().put("intent", intent);

            Map<String, String> extractedEntities = nerService.extractEntities(originalQuery);
            if (!extractedEntities.isEmpty()) {
                if (extractedEntities.containsKey("quantidade")) {
                    try {
                        context.getCollectedData().put("quantidade", Integer.parseInt(extractedEntities.get("quantidade")));
                    } catch (NumberFormatException e) {
                        // ignora
                    }
                }
                extractedEntities.forEach((key, value) -> {
                    if (!key.equals("quantidade")) {
                        context.getCollectedData().put(key, value);
                    }
                });
            } else {
                String extractedEntity = cleanAndExtractEntity(originalQuery, intent);
                if (extractedEntity != null && !extractedEntity.isEmpty()) {
                    context.getCollectedData().put("entity", extractedEntity);
                }
            }

            if (!context.getCollectedData().containsKey("entity") && !context.getCollectedData().containsKey("produto") && !context.getCollectedData().containsKey("cliente")) {
                context.getLastEntity().ifPresent(lastEntity -> {
                    if (lastEntity instanceof Produto && isProductQuery(intent)) {
                        context.getCollectedData().put("entity", ((Produto) lastEntity).getNome());
                    } else if (lastEntity instanceof Cliente && isClientQuery(intent)) {
                        context.getCollectedData().put("entity", ((Cliente) lastEntity).getNome());
                    }
                });
            }

            AssistantResponse response = handler.process(originalQuery, context.getCollectedData());

            if (response.getAction() != Action.AWAITING_INFO) {
                Optional.ofNullable(context.getCollectedData().get("foundEntity")).ifPresent(entity -> {
                    context.setLastEntity(entity);
                    context.setLastIntent((Intent) context.getCollectedData().get("intent"));
                });
                context.resetFlow();
            }

            return response;
        }

        return new AssistantResponse(I18n.getString("flow.error.unhandledIntent", intent.name()));
    }

    private String cleanAndExtractEntity(String query, Intent intent) {
        String cleanedQuery = query.replaceAll("[?.,!]$", "").trim();
        switch(intent) {
            case QUERY_STOCK:
                return QUERY_STOCK_PATTERN.matcher(cleanedQuery).replaceAll("");
            case QUERY_PRODUCT_LOTS:
                return QUERY_PRODUCT_LOTS_PATTERN.matcher(cleanedQuery).replaceAll("");
            case NAVIGATE_TO:
                return NAVIGATION_PATTERN.matcher(cleanedQuery).replaceAll("");
            default:
                return StringUtil.extractValueAfter(cleanedQuery, new String[]{"de", "do", "da", "para", "para o", "para a"});
        }
    }

    private Optional<ConversationFlow> findHandlerFor(Intent intent) {
        return conversationFlows.stream().filter(flow -> flow.canHandle(intent)).findFirst();
    }

    private boolean isProductQuery(Intent intent) {
        return intent.name().startsWith("QUERY_PRODUCT") || intent.name().contains("STOCK") || intent == Intent.UPDATE_PRODUCT || intent == Intent.MANAGE_STOCK;
    }

    private boolean isClientQuery(Intent intent) {
        return intent.name().startsWith("QUERY_CLIENT") || intent == Intent.START_SALE;
    }

    private AssistantResponse handleChangeTheme(String query) {
        if (query.contains("claro"))
            return new AssistantResponse(I18n.getString("flow.changeTheme.toLight"), Action.UI_CHANGE_THEME, Map.of("theme", "light"));
        if (query.contains("escuro"))
            return new AssistantResponse(I18n.getString("flow.changeTheme.toDark"), Action.UI_CHANGE_THEME, Map.of("theme", "dark"));
        return new AssistantResponse(I18n.getString("flow.changeTheme.misunderstood"));
    }

    private AssistantResponse handleNavigation(String query) {
        String destination = cleanAndExtractEntity(query, Intent.NAVIGATE_TO);
        if (destination == null || destination.isEmpty()) {
            return new AssistantResponse(I18n.getString("flow.navigate.misunderstood"));
        }

        String targetPanel = null;
        String normalizedDestination = StringUtil.normalize(destination);

        if (normalizedDestination.contains("venda")) targetPanel = I18n.getString("dashboard.tab.sales");
        else if (normalizedDestination.contains("cliente")) targetPanel = I18n.getString("dashboard.tab.clients");
            // ALTERADO AQUI para usar a chave de tradução
        else if (normalizedDestination.contains("produto") || normalizedDestination.contains("estoque")) targetPanel = I18n.getString("dashboard.tab.productsAndStock");
        else if (normalizedDestination.contains("relatorio")) targetPanel = I18n.getString("dashboard.tab.reports");
        else if (normalizedDestination.contains("categoria")) targetPanel = I18n.getString("dashboard.tab.categories");
        else if (normalizedDestination.contains("alerta")) targetPanel = I18n.getString("dashboard.tab.stockAlerts");
        else if (normalizedDestination.contains("movimento")) targetPanel = I18n.getString("dashboard.tab.movementHistory");
        else if (normalizedDestination.contains("usuario") || normalizedDestination.contains("utilizador")) targetPanel = I18n.getString("dashboard.tab.userManagement");
        else if (normalizedDestination.contains("auditoria") || normalizedDestination.contains("log")) targetPanel = I18n.getString("dashboard.tab.auditLogs");
        else if (normalizedDestination.contains("inicio") || normalizedDestination.contains("home")) targetPanel = I18n.getString("dashboard.tab.home");
        else if (normalizedDestination.contains("financeiro")) targetPanel = I18n.getString("dashboard.tab.financial");

        if (targetPanel != null) {
            return new AssistantResponse(I18n.getString("flow.navigate.navigatingTo", targetPanel), Action.UI_NAVIGATE, Map.of("destination", targetPanel));
        }
        return new AssistantResponse(I18n.getString("flow.navigate.panelNotFound", destination));
    }
}