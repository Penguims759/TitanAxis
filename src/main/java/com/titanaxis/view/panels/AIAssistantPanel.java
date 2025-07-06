// src/main/java/com/titanaxis/view/panels/AIAssistantPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.ai.Action;
import com.titanaxis.presenter.AIAssistantPresenter;
import com.titanaxis.service.VoiceRecognitionService;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.interfaces.AIAssistantView;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AIAssistantPanel extends JPanel implements AIAssistantView {

    private AIAssistantView.AIAssistantViewListener listener;
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton;
    private final JButton voiceButton;
    private final VoiceRecognitionService voiceService;


    public AIAssistantPanel(AppContext appContext) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Assistente Interativo TitanAxis"));

        voiceService = new VoiceRecognitionService();

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField();
        sendButton = new JButton("Enviar");
        voiceButton = new JButton("🎤");

        inputPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonGroup = new JPanel(new GridLayout(1, 2));
        buttonGroup.add(sendButton);
        buttonGroup.add(voiceButton);
        inputPanel.add(buttonGroup, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        inputField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());
        voiceButton.addActionListener(e -> toggleVoiceListening());


        new AIAssistantPresenter(this, appContext.getAIAssistantService());
        appendAssistantResponse("Olá! Sou o assistente TitanAxis. Como posso ajudar?");
    }

    private void sendMessage() {
        if (listener != null && !inputField.getText().trim().isEmpty()) {
            listener.onSendMessage(inputField.getText());
        }
    }

    private void toggleVoiceListening() {
        if (!voiceService.isListening()) {
            voiceButton.setForeground(Color.RED);
            voiceService.startListening(transcribedText -> {
                inputField.setText(transcribedText);
                sendMessage();
            });
        } else {
            voiceButton.setForeground(null);
            voiceService.stopListening();
        }
    }

    @Override
    public String getUserInput() {
        return inputField.getText();
    }

    @Override
    public void appendUserMessage(String message) {
        chatArea.append("Você: " + message + "\n");
    }

    @Override
    public void appendAssistantResponse(String response) {
        chatArea.append("Assistente: " + response + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    @Override
    public void clearUserInput() {
        inputField.setText("");
    }

    @Override
    public void setSendButtonEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
        inputField.setEnabled(enabled);
    }

    @Override
    public void requestAction(Action action, Map<String, Object> params) {
        Component parent = this.getParent();
        while (parent != null && !(parent instanceof DashboardFrame)) {
            parent = parent.getParent();
        }

        if (parent instanceof DashboardFrame) {
            ((DashboardFrame) parent).executeAction(action, params);
        }
    }

    @Override
    public void setListener(AIAssistantView.AIAssistantViewListener listener) {
        this.listener = listener;
    }

    public void refreshData() {
        // Nada a fazer aqui por enquanto, mas o método existe para consistência.
    }
}