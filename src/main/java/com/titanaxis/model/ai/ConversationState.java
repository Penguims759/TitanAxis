package com.titanaxis.model.ai;

import com.titanaxis.service.ai.ConversationFlow;
import java.util.HashMap;
import java.util.Map;

public class ConversationState {

    private ConversationFlow currentFlowHandler;
    private final Map<String, Object> collectedData = new HashMap<>();

    public void startConversation(ConversationFlow handler) {
        this.currentFlowHandler = handler;
        this.collectedData.clear();
    }

    public void collectData(String key, Object value) {
        collectedData.put(key, value);
    }

    public Map<String, Object> getCollectedData() {
        return collectedData;
    }

    public ConversationFlow getCurrentFlowHandler() {
        return currentFlowHandler;
    }

    public void reset() {
        this.currentFlowHandler = null;
        this.collectedData.clear();
    }

    public boolean isAwaitingInfo() {
        return currentFlowHandler != null;
    }
}