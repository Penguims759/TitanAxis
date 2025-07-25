package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.Map;

public class UpdateLoteFlow implements ConversationFlow {

    private final ConversationFlow adjustStockFlow;

    @Inject
    public UpdateLoteFlow(AdjustStockFlow adjustStockFlow) {
        this.adjustStockFlow = adjustStockFlow;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.UPDATE_LOTE;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        // Esta classe simplesmente delega toda a sua lógica para o AdjustStockFlow,
        // pois a funcionalidade de "atualizar um lote" é, na prática,
        // a mesma de "ajustar o estoque de um lote".
        return adjustStockFlow.process(userInput, conversationData);
    }
}