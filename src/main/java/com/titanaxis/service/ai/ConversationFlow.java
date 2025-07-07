package com.titanaxis.service.ai;

import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import java.util.Map;

public interface ConversationFlow {
    boolean canHandle(Intent intent);
    AssistantResponse process(String userInput, Map<String, Object> conversationData);
}