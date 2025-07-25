package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.StringUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdjustStockPercentageFlow extends AbstractConversationFlow {

    private final ProdutoService produtoService;
    private final AuthService authService;
    private final FlowValidationService validationService;
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("([+-]?\\d*\\.?\\d+)\\s*%");

    @Inject
    public AdjustStockPercentageFlow(ProdutoService produtoService, AuthService authService, FlowValidationService validationService) {
        this.produtoService = produtoService;
        this.authService = authService;
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.ADJUST_STOCK_PERCENTAGE;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
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
                I18n.getString("flow.adjustPercentage.askProduct"),
                (input, data) -> validationService.isProdutoValido(input),
                I18n.getString("flow.manageStock.validation.productNotFound")
        ));
        steps.put("percentual", new Step(
                I18n.getString("flow.adjustPercentage.askPercentage"),
                (input, data) -> PERCENTAGE_PATTERN.matcher(input).find(),
                I18n.getString("flow.validation.invalidPercentage")
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        try {
            String productName = (String) conversationData.get("produto");
            double percentage;

            Object percObj = conversationData.get("percentual");
            if (percObj instanceof String) {
                Matcher matcher = PERCENTAGE_PATTERN.matcher((String) percObj);
                if(matcher.find()){
                    percentage = Double.parseDouble(matcher.group(1));
                } else {
                    return new AssistantResponse(I18n.getString("flow.validation.invalidPercentage"));
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
            return new AssistantResponse(I18n.getString("flow.adjustPercentage.error.adjustFailed", e.getMessage()));
        } catch (Exception e) {
            return new AssistantResponse(I18n.getString("flow.generic.error.unexpected", e.getMessage()));
        }
    }
}