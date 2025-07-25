package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.util.I18n;

import java.util.Map;

public class CreateCategoryFlow extends AbstractConversationFlow {

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_CATEGORY;
    }

    @Override
    protected void defineSteps() {
        steps.put("nome", new Step(I18n.getString("flow.createCategory.askName")));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        return new AssistantResponse(
                I18n.getString("flow.createCategory.creating", conversationData.get("nome")),
                Action.DIRECT_CREATE_CATEGORY,
                conversationData
        );
    }
}