package com.titanaxis.model.ai;

import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConversationContext {

    private ConversationFlow currentFlow;
    private final Map<String, Object> collectedData = new HashMap<>();

    private Object lastEntity;
    private Intent lastIntent;

    public boolean isAwaitingInfo() {
        return currentFlow != null;
    }

    public void startFlow(ConversationFlow flow) {
        this.currentFlow = flow;
        this.collectedData.clear();
    }

    public void resetFlow() {
        this.currentFlow = null;
        this.collectedData.clear();
    }

    public void fullReset() {
        resetFlow();
        this.lastEntity = null;
        this.lastIntent = null;
    }

    public ConversationFlow getCurrentFlow() {
        return currentFlow;
    }

    public Map<String, Object> getCollectedData() {
        return collectedData;
    }

    public Optional<Object> getLastEntity() {
        return Optional.ofNullable(lastEntity);
    }

    public void setLastEntity(Object lastEntity) {
        this.lastEntity = lastEntity;
    }

    public Optional<Intent> getLastIntent() {
        return Optional.ofNullable(lastIntent);
    }

    public void setLastIntent(Intent lastIntent) {
        this.lastIntent = lastIntent;
    }
}