package com.titanaxis.model.ai;

import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Representa o estado completo de uma conversa com o assistente.
 * Mantém o fluxo de conversa atual, os dados recolhidos e, mais importante,
 * a última entidade e intenção de contexto para conversas multi-turnos.
 */
public class ConversationContext {

    private ConversationFlow currentFlow;
    private final Map<String, Object> collectedData = new HashMap<>();

    // A nova "memória" do assistente
    private Object lastEntity;
    private Intent lastIntent;

    public boolean isAwaitingInfo() {
        return currentFlow != null;
    }

    public void startFlow(ConversationFlow flow) {
        this.currentFlow = flow;
        this.collectedData.clear();
    }

    /**
     * Reinicia o fluxo da conversa, mas mantém o contexto (última entidade e intenção)
     * para ser usado no próximo comando.
     */
    public void resetFlow() {
        this.currentFlow = null;
        this.collectedData.clear();
    }

    /**
     * Limpa completamente o estado do assistente, incluindo o contexto.
     * Usado para comandos como "cancelar" ou saudações.
     */
    public void fullReset() {
        resetFlow();
        this.lastEntity = null;
        this.lastIntent = null;
    }

    // --- Getters e Setters ---

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