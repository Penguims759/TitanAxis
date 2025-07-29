// src/main/java/com/titanaxis/model/ai/ConversationContext.java
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

    
    private Action pendingProactiveAction;
    private Map<String, Object> proactiveActionParams;

    public boolean isAwaitingInfo() {
        return currentFlow != null || pendingProactiveAction != null;
    }

    public void startFlow(ConversationFlow flow) {
        this.currentFlow = flow;
        this.collectedData.clear();
        this.pendingProactiveAction = null; // Limpa qualquer ação proativa pendente
    }

    public void resetFlow() {
        this.currentFlow = null;
        this.collectedData.clear();
        this.pendingProactiveAction = null;
        this.proactiveActionParams = null;
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

    public Optional<Action> getPendingProactiveAction() {
        return Optional.ofNullable(pendingProactiveAction);
    }

    public void setPendingProactiveAction(Action action, Map<String, Object> params) {
        this.pendingProactiveAction = action;
        this.proactiveActionParams = params;
        this.currentFlow = null; // Garante que não há um fluxo normal ativo
    }
    public Map<String, Object> getProactiveActionParams() {
        return proactiveActionParams;
    }

}