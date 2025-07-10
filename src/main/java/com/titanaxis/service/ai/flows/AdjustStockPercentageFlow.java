package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.util.StringUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdjustStockPercentageFlow extends AbstractConversationFlow {

    private final ProdutoService produtoService;
    private final AuthService authService;
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("([+-]?\\d*\\.?\\d+)\\s*%");

    @Inject
    public AdjustStockPercentageFlow(ProdutoService produtoService, AuthService authService) {
        this.produtoService = produtoService;
        this.authService = authService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.ADJUST_STOCK_PERCENTAGE;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        // Extrair entidades da pergunta inicial
        if (conversationData.get("entity") == null) {
            String productName = StringUtil.extractValueAfter(userInput, new String[]{"de", "do", "da"});
            Matcher matcher = PERCENTAGE_PATTERN.matcher(userInput);
            if (productName != null && matcher.find()) {
                conversationData.put("produto", productName);
                String percentageStr = matcher.group(1);
                if (userInput.contains("reduzir") || userInput.contains("diminuir")) {
                    percentageStr = "-" + percentageStr;
                }
                conversationData.put("percentual", Double.parseDouble(percentageStr));
            }
        }
        return super.process(userInput, conversationData);
    }

    @Override
    protected void defineSteps() {
        steps.put("produto", new Step(
                "Qual produto você deseja ajustar?",
                this::isProdutoValido,
                "Produto não encontrado. Por favor, verifique o nome."
        ));
        steps.put("percentual", new Step(
                "Qual o percentual de ajuste? (ex: 20% para aumentar, -15% para reduzir)",
                (input, data) -> PERCENTAGE_PATTERN.matcher(input).find(),
                "Formato de percentual inválido. Use um número seguido de '%' (ex: '20%')."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        try {
            String productName = (String) conversationData.get("produto");
            double percentage;

            // Re-processar o percentual caso tenha sido inserido no segundo passo
            Object percObj = conversationData.get("percentual");
            if (percObj instanceof String) {
                Matcher matcher = PERCENTAGE_PATTERN.matcher((String) percObj);
                if(matcher.find()){
                    percentage = Double.parseDouble(matcher.group(1));
                } else {
                    return new AssistantResponse("Formato de percentual inválido.");
                }
            } else {
                percentage = (Double) percObj;
            }

            String resultMessage = produtoService.ajustarEstoquePercentual(
                    productName,
                    percentage,
                    authService.getUsuarioLogado().orElse(null)
            );
            return new AssistantResponse(resultMessage);
        } catch (PersistenciaException | IllegalArgumentException e) {
            return new AssistantResponse("Não foi possível ajustar o estoque: " + e.getMessage());
        } catch (Exception e) {
            return new AssistantResponse("Ocorreu um erro inesperado: " + e.getMessage());
        }
    }

    private boolean isProdutoValido(String nomeProduto) {
        try {
            return produtoService.produtoExiste(nomeProduto);
        } catch (PersistenciaException e) {
            return false;
        }
    }
}