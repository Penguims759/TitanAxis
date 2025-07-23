// penguims759/titanaxis/Penguims759-TitanAxis-e9669e5c4e163f98311d4f51683c348827675c7a/src/main/java/com/titanaxis/presenter/AIAssistantPresenter.java
package com.titanaxis.presenter;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AIAssistantService;
import com.titanaxis.util.I18n;
import com.titanaxis.view.interfaces.AIAssistantView;

import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.util.Map;

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
                Thread.sleep(500);
                return service.processQuery(message);
            }

            @Override
            protected void done() {
                try {
                    AssistantResponse response = get();
                    view.showThinkingIndicator(false);
                    view.appendMessage(response.getTextResponse(), false);

                    // *** LÓGICA DE AÇÃO AJUSTADA ***
                    if (response.hasAction() && response.getAction() != Action.AWAITING_INFO) {
                        if (response.getAction().name().startsWith("PROACTIVE_")) {
                            service.getContext().setPendingProactiveAction(response.getAction(), response.getActionParams());
                        } else {
                            view.requestAction(response.getAction(), response.getActionParams());
                            handleProactiveSuggestion(response);
                        }
                    }
                } catch (Exception e) {
                    view.showThinkingIndicator(false);
                    view.appendMessage(I18n.getString("presenter.assistant.error.generic", e.getMessage()), false);
                } finally {
                    view.setSendButtonEnabled(true);
                    view.requestInputFieldFocus();
                }
            }
        };
        worker.execute();
    }

    @Override
    public void onViewOpened() {
        view.setSendButtonEnabled(false);
        view.showThinkingIndicator(true);

        SwingWorker<AssistantResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected AssistantResponse doInBackground() throws Exception {
                return service.getInitialGreetingWithInsights();
            }

            @Override
            protected void done() {
                try {
                    AssistantResponse response = get();
                    view.showThinkingIndicator(false);
                    view.appendMessage(response.getTextResponse(), false);
                } catch (Exception e) {
                    view.showThinkingIndicator(false);
                    view.appendMessage(I18n.getString("presenter.assistant.error.generic", e.getMessage()), false);
                } finally {
                    view.setSendButtonEnabled(true);
                    view.requestInputFieldFocus();
                }
            }
        };
        worker.execute();
    }


    private void handleProactiveSuggestion(AssistantResponse response) {
        Action completedAction = response.getAction();
        Map<String, Object> params = response.getActionParams();

        if (completedAction == Action.DIRECT_CREATE_PRODUCT && params != null) {
            String nomeProduto = (String) params.get("nome");
            if (nomeProduto != null) {
                Timer timer = new Timer(1500, e -> {
                    service.getContext().setPendingProactiveAction(Action.PROACTIVE_SUGGEST_ADD_LOTE, params);
                    view.appendMessage(I18n.getString("presenter.assistant.proactive.addProduct", nomeProduto), false);
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
        else if (completedAction == Action.DIRECT_CREATE_CLIENT && params != null) {
            String nomeCliente = (String) params.get("nome");
            if (nomeCliente != null) {
                Timer timer = new Timer(1500, e -> {
                    service.getContext().setPendingProactiveAction(Action.PROACTIVE_SUGGEST_START_SALE, params);
                    view.appendMessage(I18n.getString("presenter.assistant.proactive.addClient", nomeCliente), false);
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
        else if (completedAction == Action.DIRECT_CREATE_FORNECEDOR && params != null) {
            String nomeFornecedor = (String) params.get("nome");
            if (nomeFornecedor != null) {
                Timer timer = new Timer(1500, e -> {
                    service.getContext().setPendingProactiveAction(Action.PROACTIVE_SUGGEST_CREATE_PURCHASE_ORDER, params);
                    view.appendMessage(I18n.getString("presenter.assistant.proactive.addSupplier", nomeFornecedor), false);
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }
}