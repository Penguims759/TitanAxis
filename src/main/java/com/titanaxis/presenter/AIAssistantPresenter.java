// src/main/java/com/titanaxis/presenter/AIAssistantPresenter.java
package com.titanaxis.presenter;

import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AIAssistantService;
import com.titanaxis.view.interfaces.AIAssistantView;

import javax.swing.SwingWorker;

public class AIAssistantPresenter implements AIAssistantView.AIAssistantViewListener {

    private final AIAssistantView view;
    private final AIAssistantService service;

    public AIAssistantPresenter(AIAssistantView view, AIAssistantService service) {
        this.view = view;
        this.service = service;
        this.view.setListener(this);
    }

    @Override
    public void onSendMessage(String message) {
        view.setSendButtonEnabled(false);
        view.appendMessage(message, true); // Adiciona a mensagem do usuário à UI
        view.clearUserInput();
        view.showThinkingIndicator(true); // Mostra o indicador "A pensar..."

        SwingWorker<AssistantResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected AssistantResponse doInBackground() throws Exception {
                // Simula um pequeno atraso para o indicador ser visível
                Thread.sleep(500);
                return service.processQuery(message);
            }

            @Override
            protected void done() {
                try {
                    AssistantResponse response = get();
                    view.appendMessage(response.getTextResponse(), false); // Adiciona a resposta do assistente
                    if (response.hasAction()) {
                        view.requestAction(response.getAction(), response.getActionParams());
                    }
                } catch (Exception e) {
                    view.appendMessage("Ocorreu um erro: " + e.getMessage(), false);
                } finally {
                    view.showThinkingIndicator(false); // Esconde o indicador
                    view.setSendButtonEnabled(true);
                }
            }
        };
        worker.execute();
    }
}