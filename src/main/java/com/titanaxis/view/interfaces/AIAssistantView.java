// src/main/java/com/titanaxis/view/interfaces/AIAssistantView.java
package com.titanaxis.view.interfaces;

import com.titanaxis.model.ai.Action;
import java.util.Map;

public interface AIAssistantView {
    String getUserInput();
    void clearUserInput();
    void setSendButtonEnabled(boolean enabled);

    // Métodos para a nova UI
    void showThinkingIndicator(boolean show);
    void appendMessage(String message, boolean isUser);

    void requestAction(Action action, Map<String, Object> params);

    interface AIAssistantViewListener {
        void onSendMessage(String message);
    }

    void setListener(AIAssistantViewListener listener);
}