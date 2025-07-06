// src/main/java/com/titanaxis/model/ai/ConversationState.java
package com.titanaxis.model.ai;

import com.titanaxis.service.ai.ConversationFlow; // NOVO
import java.util.HashMap;
import java.util.Map;

public class ConversationState {

    private ConversationFlow currentFlowHandler; // ALTERADO
    private final Map<String, Object> collectedData = new HashMap<>();

    /**
     * Inicia uma nova conversa, definindo qual manipulador de fluxo está ativo.
     * @param handler O manipulador de fluxo para esta conversa.
     */
    public void startConversation(ConversationFlow handler) { // ALTERADO
        this.currentFlowHandler = handler;
        this.collectedData.clear();
    }

    public void collectData(String key, Object value) {
        collectedData.put(key, value);
    }

    public Map<String, Object> getCollectedData() {
        return collectedData;
    }

    /**
     * Obtém o manipulador de fluxo atualmente ativo.
     * @return O ConversationFlow ativo.
     */
    public ConversationFlow getCurrentFlowHandler() { // ALTERADO
        return currentFlowHandler;
    }

    /**
     * Limpa o estado da conversa, terminando o fluxo atual.
     */
    public void reset() {
        this.currentFlowHandler = null;
        this.collectedData.clear();
    }

    /**
     * Verifica se há uma conversa em andamento.
     * @return true se um fluxo de conversa estiver ativo, false caso contrário.
     */
    public boolean isAwaitingInfo() {
        return currentFlowHandler != null;
    }
}