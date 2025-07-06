// src/main/java/com/titanaxis/model/ai/ConversationState.java
package com.titanaxis.model.ai;

import java.util.HashMap;
import java.util.Map;

public class ConversationState {

    private Action currentAction;
    private final Map<String, Object> collectedData = new HashMap<>();

    public void startConversation(Action action) {
        this.currentAction = action;
        this.collectedData.clear();
    }

    public void collectData(String key, Object value) {
        collectedData.put(key, value);
    }

    public Map<String, Object> getCollectedData() {
        return collectedData;
    }

    public Action getCurrentAction() {
        return currentAction;
    }

    public void reset() {
        this.currentAction = null;
        this.collectedData.clear();
    }

    public boolean isAwaitingInfo() {
        return currentAction != null;
    }
}