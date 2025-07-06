// src/main/java/com/titanaxis/service/AIAssistantService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.model.ai.ConversationState;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIAssistantService {

    private final AlertaService alertaService;
    private final AnalyticsService analyticsService;
    private final ConversationState conversationState = new ConversationState();

    @Inject
    public AIAssistantService(AlertaService alertaService, AnalyticsService analyticsService) {
        this.alertaService = alertaService;
        this.analyticsService = analyticsService;
    }

    public AssistantResponse processQuery(String query) {
        String lowerQuery = query.toLowerCase();

        if (conversationState.isAwaitingInfo()) {
            return handleConversation(query);
        }

        try {
            // --- Ações Diretas e Guias ---
            if (lowerQuery.matches(".*crie (um novo )?cliente chamado (.*) com (o )?contato (.*)")) {
                Pattern p = Pattern.compile(".*crie (um novo )?cliente chamado (.*) com (o )?contato (.*)");
                Matcher m = p.matcher(lowerQuery);
                if (m.find()) {
                    String nome = m.group(2).trim();
                    String contato = m.group(4).trim();
                    Map<String, Object> params = new HashMap<>();
                    params.put("nome", nome);
                    params.put("contato", contato);
                    return new AssistantResponse("Entendido! Criando o cliente '" + nome + "' agora mesmo.", Action.DIRECT_CREATE_CLIENT, params);
                }
            }

            if (lowerQuery.contains("gere") && lowerQuery.contains("relatório de vendas") && lowerQuery.contains("pdf")) {
                return new AssistantResponse("Claro. Gerando o relatório de vendas em PDF.", Action.DIRECT_GENERATE_SALES_REPORT_PDF, null);
            }

            if (lowerQuery.contains("como") && lowerQuery.contains("adicionar") && lowerQuery.contains("lote")) {
                return new AssistantResponse("Claro! Vou te mostrar como fazer isso.", Action.GUIDE_NAVIGATE_TO_ADD_LOTE, null);
            }

            // --- Análise de Dados ---
            if (lowerQuery.contains("produto mais vendido")) {
                String topProduct = analyticsService.getTopSellingProduct();
                return new AssistantResponse("Com base nos dados, o produto mais vendido é: " + topProduct);
            }

            // --- Personalização da UI ---
            if (lowerQuery.contains("mude o tema para claro")) {
                return new AssistantResponse("Mudando para o tema claro!", Action.UI_CHANGE_THEME, Map.of("theme", "light"));
            }
            if (lowerQuery.contains("mude o tema para escuro")) {
                return new AssistantResponse("Mudando para o tema escuro!", Action.UI_CHANGE_THEME, Map.of("theme", "dark"));
            }

            // --- Conversa Contextual ---
            if (lowerQuery.contains("cadastrar") && lowerQuery.contains("produto")) {
                conversationState.startConversation(Action.AWAITING_INFO);
                conversationState.getCollectedData().put("flow", "CREATE_PRODUCT");
                return new AssistantResponse("Ótimo! Vamos cadastrar um novo produto. Qual o nome dele?");
            }

            // --- Fallback ---
            return new AssistantResponse("Desculpe, não entendi. Tente 'gere o relatório de vendas' ou 'como adicionar um lote?'.");

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao aceder a base de dados: " + e.getMessage());
        } catch (Exception e) {
            return new AssistantResponse("Ocorreu um erro inesperado: " + e.getMessage());
        }
    }

    private AssistantResponse handleConversation(String userInput) {
        Map<String, Object> data = conversationState.getCollectedData();
        String flow = (String) data.get("flow");

        if ("CREATE_PRODUCT".equals(flow)) {
            if (!data.containsKey("nome")) {
                data.put("nome", userInput);
                return new AssistantResponse("Ok, nome é '" + userInput + "'. Qual o preço de venda?");
            } else if (!data.containsKey("preco")) {
                try {
                    double preco = Double.parseDouble(userInput.replace(",", "."));
                    data.put("preco", preco);
                    conversationState.reset();
                    // Em uma implementação real, chamaríamos o produtoService.salvar() aqui
                    return new AssistantResponse("Produto '" + data.get("nome") + "' cadastrado com preço " + preco + ". (Simulação, implementação futura)", null, null);
                } catch (NumberFormatException e) {
                    return new AssistantResponse("Preço inválido. Por favor, digite um número (ex: 99.90).");
                }
            }
        }
        conversationState.reset();
        return new AssistantResponse("Houve um erro na conversa. Vamos começar de novo.");
    }
}