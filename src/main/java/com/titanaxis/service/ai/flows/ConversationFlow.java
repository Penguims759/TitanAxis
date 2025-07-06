// src/main/java/com/titanaxis/service/ai/ConversationFlow.java
package com.titanaxis.service.ai;

import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AIAssistantService.Intent;
import java.util.Map;

/**
 * Define o contrato para um manipulador de um fluxo de conversa específico do assistente.
 * Cada implementação será responsável por uma única funcionalidade (ex: criar utilizador).
 */
public interface ConversationFlow {

    /**
     * Verifica se este manipulador é capaz de lidar com a intenção dada.
     * @param intent A intenção do utilizador.
     * @return true se o manipulador for responsável por esta intenção, false caso contrário.
     */
    boolean canHandle(Intent intent);

    /**
     * Processa a entrada do utilizador para este fluxo de conversa.
     * @param userInput A entrada atual do utilizador.
     * @param conversationData Os dados já recolhidos para esta conversa.
     * @return A resposta do assistente.
     */
    AssistantResponse process(String userInput, Map<String, Object> conversationData);
}