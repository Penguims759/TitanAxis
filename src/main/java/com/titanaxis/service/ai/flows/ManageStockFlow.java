// src/main/java/com/titanaxis/service/ai/flows/ManageStockFlow.java
package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.util.StringUtil;

import java.util.Map;

public class ManageStockFlow extends AbstractConversationFlow {

    private final ProdutoService produtoService;

    @Inject
    public ManageStockFlow(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.MANAGE_STOCK;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        if (conversationData.get("entity") != null && !conversationData.containsKey("productName")) {
            conversationData.put("productName", conversationData.get("entity"));
        }
        return super.process(userInput, conversationData);
    }

    @Override
    protected void defineSteps() {
        steps.put("productName", new Step(
                "Ok. Qual o nome do produto?",
                this::isProdutoValido,
                "Produto não encontrado. Por favor, verifique o nome ou diga 'cancelar'."
        ));
        steps.put("lotNumber", new Step(data -> "Certo. Qual o número do lote para '" + data.get("productName") + "'?"));
        steps.put("quantity", new Step(
                data -> "E qual a quantidade a adicionar ao lote '" + data.get("lotNumber") + "'?",
                StringUtil::isNumeric,
                "A quantidade deve ser um número."
        ));
        steps.put("confirmation", new Step(
                data -> String.format("Você confirma a adição de %s unidades ao lote %s do produto %s? (sim/não)",
                        data.get("quantity"), data.get("lotNumber"), data.get("productName")),
                input -> StringUtil.normalize(input).equals("sim") || StringUtil.normalize(input).equals("nao"),
                "Por favor, responda com 'sim' ou 'não'."
        ));
    }

    private boolean isProdutoValido(String nomeProduto) {
        try {
            return produtoService.produtoExiste(nomeProduto);
        } catch (PersistenciaException e) {
            return false;
        }
    }


    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        if ("sim".equalsIgnoreCase((String) conversationData.get("confirmation"))) {
            return new AssistantResponse("Ok, a atualizar o stock...", Action.DIRECT_ADD_STOCK, conversationData);
        } else {
            return new AssistantResponse("Ok, ação cancelada.");
        }
    }
}