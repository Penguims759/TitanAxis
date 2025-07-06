// src/main/java/com/titanaxis/service/AIAssistantService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.model.ai.ConversationState;
import com.titanaxis.util.StringUtil;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AIAssistantService {

    private final AlertaService alertaService;
    private final AnalyticsService analyticsService;
    private final ConversationState conversationState = new ConversationState();

    private enum Intent {
        CREATE_CLIENT("criar", "adicione", "novo", "cliente"),
        GENERATE_REPORT("gerar", "relatorio", "vendas", "venda", "inventario", "estoque"),
        GUIDE_ADD_LOTE("como", "adicionar", "incluir", "lote"),
        CREATE_PRODUCT("cadastrar", "criar", "produto"),
        CHANGE_THEME("mudar", "alterar", "tema", "claro", "escuro"),
        QUERY_LOW_STOCK("quais", "liste", "mostrar", "produto", "baixo", "pouco", "estoque"),
        QUERY_EXPIRING_LOTES("quais", "liste", "lotes", "vencer", "vencimento"),
        QUERY_TOP_SELLING("produto", "mais", "vendido", "top", "vendas"),
        GREETING("ola", "oi", "bom", "dia", "tarde", "noite"),
        UNKNOWN;

        final String[] keywords;
        Intent(String... keywords) {
            this.keywords = keywords;
        }
    }

    @Inject
    public AIAssistantService(AlertaService alertaService, AnalyticsService analyticsService) {
        this.alertaService = alertaService;
        this.analyticsService = analyticsService;
    }

    public AssistantResponse processQuery(String query) {
        if (conversationState.isAwaitingInfo()) {
            return handleConversation(query);
        }

        Intent intent = getIntent(query);

        try {
            switch (intent) {
                case CREATE_CLIENT: return startCreateClientConversation(query);
                case GENERATE_REPORT: return startGenerateReportConversation(query);
                case GUIDE_ADD_LOTE: return new AssistantResponse("Claro! Vou te mostrar como fazer isso.", Action.GUIDE_NAVIGATE_TO_ADD_LOTE, null);
                case CREATE_PRODUCT: return startCreateProductConversation();
                case CHANGE_THEME: return handleChangeTheme(query);
                case QUERY_LOW_STOCK: return handleLowStockQuery(query);
                case QUERY_EXPIRING_LOTES: return handleExpiringLotesQuery();
                case QUERY_TOP_SELLING:
                    String topProduct = analyticsService.getTopSellingProduct();
                    return new AssistantResponse("Com base nos dados, o produto mais vendido é: " + topProduct);
                case GREETING: return getProactiveGreeting();
                default: // UNKNOWN
                    return new AssistantResponse("Desculpe, não entendi o que você quis dizer. Consulte o guia ao lado para ver algumas ideias.");
            }
        } catch (Exception e) {
            return new AssistantResponse("Ocorreu um erro inesperado ao processar sua solicitação: " + e.getMessage());
        }
    }

    /**
     * Determina a intenção do utilizador com base na contagem de correspondências de palavras-chave.
     * ALTERADO: Usa uma lógica de "melhor correspondência" em vez de um limiar rígido.
     */
    private Intent getIntent(String query) {
        String[] queryWords = StringUtil.normalize(query).split("\\s+");
        if (queryWords.length == 0 || (queryWords.length == 1 && queryWords[0].isEmpty())) {
            return Intent.UNKNOWN;
        }

        Intent bestIntent = Intent.UNKNOWN;
        int maxMatches = 0;

        for (Intent intent : Intent.values()) {
            if (intent == Intent.UNKNOWN) continue;

            int currentMatches = 0;
            for (String keyword : intent.keywords) {
                // Verifica se alguma palavra na query corresponde à palavra-chave com uma pequena margem de erro.
                if (Arrays.stream(queryWords).anyMatch(word -> StringUtil.levenshteinDistance(keyword, word) <= 1)) {
                    currentMatches++;
                }
            }

            // A melhor intenção é aquela com o maior número de palavras correspondentes.
            if (currentMatches > maxMatches) {
                maxMatches = currentMatches;
                bestIntent = intent;
            }
        }

        // Se a melhor intenção tem pelo menos uma correspondência, retorna-a.
        return maxMatches > 0 ? bestIntent : Intent.UNKNOWN;
    }

    // ... O restante do código, que já estava robusto, permanece o mesmo.

    private AssistantResponse handleConversation(String userInput) {
        if (fuzzyContains(userInput, "cancelar", "cancela", "sair")) {
            conversationState.reset();
            return new AssistantResponse("Ok, ação cancelada. Como posso ajudar agora?");
        }

        Map<String, Object> data = conversationState.getCollectedData();
        String flow = (String) data.get("flow");

        if ("CREATE_PRODUCT".equals(flow)) return handleCreateProductFlow(userInput);
        if ("GENERATE_REPORT".equals(flow)) return handleGenerateReportFlow(userInput);
        if ("CREATE_CLIENT".equals(flow)) return handleCreateClientFlow(userInput);

        conversationState.reset();
        return new AssistantResponse("Houve um erro na conversa. Vamos começar de novo.");
    }

    private AssistantResponse startGenerateReportConversation(String query) {
        conversationState.startConversation(Action.AWAITING_INFO);
        conversationState.getCollectedData().put("flow", "GENERATE_REPORT");

        if (fuzzyContains(query, "venda", "vendas")) {
            conversationState.collectData("report_type", "vendas");
        }
        if (fuzzyContains(query, "inventario", "estoque")) {
            conversationState.collectData("report_type", "inventario");
        }
        if (fuzzyContains(query, "pdf")) {
            conversationState.collectData("report_format", "pdf");
        }
        if (fuzzyContains(query, "csv")) {
            conversationState.collectData("report_format", "csv");
        }

        return handleConversation("");
    }

    private boolean fuzzyContains(String text, String... keywords) {
        String[] textWords = StringUtil.normalize(text).split("\\s+");
        for (String keyword : keywords) {
            for (String word : textWords) {
                if (StringUtil.levenshteinDistance(keyword, word) <= 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private AssistantResponse getProactiveGreeting() throws PersistenciaException {
        StringBuilder greeting = new StringBuilder("Olá! Como posso ajudar hoje?");
        List<Produto> lowStockProducts = alertaService.getProdutosComEstoqueBaixo();
        List<Lote> expiringLotes = alertaService.getLotesProximosDoVencimento();

        if (!lowStockProducts.isEmpty() || !expiringLotes.isEmpty()) {
            greeting.append("\n\nNotei algumas coisas importantes para você:");
            if (!lowStockProducts.isEmpty()) {
                greeting.append("\n- ").append(lowStockProducts.size()).append(" produto(s) estão com baixo estoque.");
            }
            if (!expiringLotes.isEmpty()) {
                greeting.append("\n- ").append(expiringLotes.size()).append(" lote(s) estão próximos do vencimento.");
            }
            greeting.append("\n\nPergunte-me sobre \"baixo estoque\" ou \"lotes a vencer\" para ver os detalhes.");
        }
        return new AssistantResponse(greeting.toString());
    }

    private AssistantResponse handleLowStockQuery(String query) throws PersistenciaException {
        List<Produto> lowStockProducts = alertaService.getProdutosComEstoqueBaixo();

        if (fuzzyContains(query, "categoria")) {
            String[] parts = StringUtil.normalize(query).split("categoria");
            if (parts.length > 1) {
                String category = parts[1].replace("'", "").replace("\"", "").trim();
                lowStockProducts = lowStockProducts.stream()
                        .filter(p -> p.getNomeCategoria() != null && StringUtil.normalize(p.getNomeCategoria()).equalsIgnoreCase(category))
                        .collect(Collectors.toList());
            }
        }

        if (lowStockProducts.isEmpty()) {
            return new AssistantResponse("Nenhum produto com baixo estoque foi encontrado com os seus critérios.");
        }

        StringBuilder response = new StringBuilder("Encontrei estes produtos com baixo estoque:\n");
        lowStockProducts.forEach(p -> response.append(String.format("- %s (Restam: %d)\n", p.getNome(), p.getQuantidadeTotal())));
        return new AssistantResponse(response.toString());
    }

    private AssistantResponse handleExpiringLotesQuery() throws PersistenciaException {
        List<Lote> expiringLotes = alertaService.getLotesProximosDoVencimento();

        if (expiringLotes.isEmpty()) {
            return new AssistantResponse("Ótima notícia! Nenhum lote está para vencer nos próximos 30 dias.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder response = new StringBuilder("Encontrei estes lotes com vencimento próximo:\n");
        expiringLotes.forEach(lote -> response.append(String.format("- %s (Lote: %s) vence em %s\n",
                lote.getProduto().getNome(),
                lote.getNumeroLote(),
                lote.getDataValidade().format(formatter)
        )));

        return new AssistantResponse(response.toString());
    }

    private AssistantResponse handleChangeTheme(String query) {
        if (fuzzyContains(query, "claro", "light")) {
            return new AssistantResponse("Mudando para o tema claro!", Action.UI_CHANGE_THEME, Map.of("theme", "light"));
        }
        if (fuzzyContains(query, "escuro", "dark")) {
            return new AssistantResponse("Mudando para o tema escuro!", Action.UI_CHANGE_THEME, Map.of("theme", "dark"));
        }
        return new AssistantResponse("Não entendi qual tema você quer. Tente 'tema claro' ou 'tema escuro'.");
    }

    private AssistantResponse startCreateProductConversation() {
        conversationState.startConversation(Action.AWAITING_INFO);
        conversationState.getCollectedData().put("flow", "CREATE_PRODUCT");
        return new AssistantResponse("Ótimo! Vamos cadastrar um novo produto. Qual o nome dele?");
    }

    private AssistantResponse startCreateClientConversation(String query) {
        String nome = extractValueAfter(query, "chamado");
        String contato = extractValueAfter(query, "contato");

        if (nome != null && !nome.isEmpty() && contato != null && !contato.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("nome", nome);
            params.put("contato", contato);
            return new AssistantResponse("Entendido! Criando o cliente '" + nome + "' com o contato '" + contato + "'.", Action.DIRECT_CREATE_CLIENT, params);
        }

        conversationState.startConversation(Action.AWAITING_INFO);
        conversationState.getCollectedData().put("flow", "CREATE_CLIENT");
        if (nome != null && !nome.isEmpty()) {
            conversationState.collectData("nome", nome);
            return new AssistantResponse("Ok, o nome do cliente é '" + nome + "'. Qual o contato (email ou telefone)?");
        }
        return new AssistantResponse("Vamos criar um novo cliente. Qual é o nome dele?");
    }

    private String extractValueAfter(String text, String keyword) {
        if (text.contains(keyword)) {
            String[] parts = StringUtil.normalize(text).split(keyword);
            if (parts.length > 1) {
                String potentialValue = parts[1].split(" com ")[0].trim();
                return potentialValue;
            }
        }
        return null;
    }

    private AssistantResponse handleCreateClientFlow(String userInput) {
        Map<String, Object> data = conversationState.getCollectedData();
        if (!data.containsKey("nome")) {
            data.put("nome", userInput);
            return new AssistantResponse("Entendido, nome é '" + userInput + "'. Qual o contato (email/telefone)?");
        } else if (!data.containsKey("contato")) {
            data.put("contato", userInput);
            conversationState.reset();
            return new AssistantResponse("Cliente '" + data.get("nome") + "' criado com o contato '" + userInput + "'. (Simulação)", Action.DIRECT_CREATE_CLIENT, data);
        }
        return new AssistantResponse("Fluxo de criação de cliente concluído.");
    }

    private AssistantResponse handleGenerateReportFlow(String userInput) {
        Map<String, Object> data = conversationState.getCollectedData();

        if (!userInput.isEmpty()) {
            if (fuzzyContains(userInput, "venda", "vendas")) data.put("report_type", "vendas");
            if (fuzzyContains(userInput, "inventario", "estoque")) data.put("report_type", "inventario");
            if (fuzzyContains(userInput, "pdf")) data.put("report_format", "pdf");
            if (fuzzyContains(userInput, "csv")) data.put("report_format", "csv");
        }

        if (!data.containsKey("report_type")) {
            return new AssistantResponse("Que tipo de relatório você quer? (Vendas ou Inventário)");
        }
        if (!data.containsKey("report_format")) {
            return new AssistantResponse("E em qual formato? (PDF ou CSV)");
        }

        String type = (String) data.get("report_type");
        String format = (String) data.get("report_format");
        conversationState.reset();

        Action action = (type.equals("vendas") && format.equals("pdf")) ? Action.DIRECT_GENERATE_SALES_REPORT_PDF : null;

        return new AssistantResponse("Ok, gerando o relatório de " + type + " em " + format + ".", action, null);
    }

    private AssistantResponse handleCreateProductFlow(String userInput) {
        Map<String, Object> data = conversationState.getCollectedData();
        if (!data.containsKey("nome")) {
            data.put("nome", userInput);
            return new AssistantResponse("Ok, o nome é '" + userInput + "'. Qual o preço de venda?");
        } else if (!data.containsKey("preco")) {
            try {
                double preco = Double.parseDouble(userInput.replace(",", "."));
                data.put("preco", preco);
                conversationState.reset();
                return new AssistantResponse("Produto '" + data.get("nome") + "' cadastrado com preço " + preco + ". (Simulação)", null, null);
            } catch (NumberFormatException e) {
                return new AssistantResponse("Preço inválido. Por favor, digite um número (ex: 99.90).");
            }
        }
        return new AssistantResponse("Fluxo de criação de produto concluído.");
    }
}