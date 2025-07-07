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
        view.appendMessage(message, true);
        view.clearUserInput();
        view.showThinkingIndicator(true);

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
                    // Primeiro, remove o balão "A pensar..." ou qualquer outro indicador
                    view.showThinkingIndicator(false);
                    // Depois, adiciona a resposta final do bot
                    view.appendMessage(response.getTextResponse(), false);

                    if (response.hasAction()) {
                        view.requestAction(response.getAction(), response.getActionParams());
                    }
                } catch (Exception e) {
                    view.showThinkingIndicator(false); // Remove o indicador mesmo se der erro
                    view.appendMessage("Ocorreu um erro: " + e.getMessage(), false);
                } finally {
                    view.setSendButtonEnabled(true);
                }
            }
        };
        worker.execute();
    }
}