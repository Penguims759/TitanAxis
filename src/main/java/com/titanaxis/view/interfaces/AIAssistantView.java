// penguims759/titanaxis/Penguims759-TitanAxis-e9669e5c4e163f98311d4f51683c348827675c7a/src/main/java/com/titanaxis/view/interfaces/AIAssistantView.java
package com.titanaxis.view.interfaces;

import com.titanaxis.model.ai.Action;
import java.util.Map;

public interface AIAssistantView {
    String getUserInput();
    void clearUserInput();
    void setSendButtonEnabled(boolean enabled);

    void showThinkingIndicator(boolean show);
    void appendMessage(String message, boolean isUser);
    void removeLastMessage();

    void requestAction(Action action, Map<String, Object> params);
    void requestInputFieldFocus();

    interface AIAssistantViewListener {
        void onSendMessage(String message);
        void onViewOpened(); // NOVO MÉTODO
    }

    void setListener(AIAssistantViewListener listener);
}