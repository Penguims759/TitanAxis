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
        view.appendUserMessage(message);
        view.clearUserInput();

        SwingWorker<AssistantResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected AssistantResponse doInBackground() throws Exception {
                return service.processQuery(message);
            }

            @Override
            protected void done() {
                try {
                    AssistantResponse response = get();
                    view.appendAssistantResponse(response.getTextResponse());
                    if (response.hasAction()) {
                        view.requestAction(response.getAction(), response.getActionParams());
                    }
                } catch (Exception e) {
                    view.appendAssistantResponse("Ocorreu um erro: " + e.getMessage());
                } finally {
                    view.setSendButtonEnabled(true);
                }
            }
        };
        worker.execute();
    }
}