package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import java.util.Map;

public class CreateCategoryFlow extends AbstractConversationFlow {

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_CATEGORY;
    }

    @Override
    protected void defineSteps() {
        steps.put("nome", new Step("Claro, vamos criar uma nova categoria. Qual é o nome?"));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        // Embora não se guarde a categoria no contexto (por ser uma ação simples),
        // a estrutura do fluxo permanece consistente.
        return new AssistantResponse(
                "Ok, a criar a categoria '" + conversationData.get("nome") + "'...",
                Action.DIRECT_CREATE_CATEGORY,
                conversationData
        );
    }
}