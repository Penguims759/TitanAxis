// src/main/java/com/titanaxis/service/ai/flows/UpdateLoteFlow.java
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
        return adjustStockFlow.process(userInput, conversationData);
    }
}