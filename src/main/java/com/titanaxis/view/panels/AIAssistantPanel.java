// src/main/java/com/titanaxis/view/panels/AIAssistantPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.ChatMessage;
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
    private final JTextField inputField;
    private final JButton sendButton;
    private final JButton voiceButton;
    private final VoiceRecognitionService voiceService;

    private final JList<ChatMessage> chatList;
    private final DefaultListModel<ChatMessage> chatModel;
    private final Timer thinkingTimer;

    public AIAssistantPanel(AppContext appContext) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Assistente Interativo TitanAxis"));

        voiceService = new VoiceRecognitionService();

        // --- Lista de Chat com Renderizador Customizado ---
        chatModel = new DefaultListModel<>();
        chatList = new JList<>(chatModel);
        chatList.setCellRenderer(new ChatBubbleRenderer());
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setBackground(UIManager.getColor("Panel.background")); // Fundo consistente

        // Remove a seleção visual para uma aparência de chat mais limpa
        chatList.setSelectionBackground(new Color(0,0,0,0));
        chatList.setSelectionForeground(chatList.getForeground());


        JScrollPane scrollPane = new JScrollPane(chatList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); // Margens finas nas laterais

        add(scrollPane, BorderLayout.CENTER);

        // --- Painel de Entrada ---
        JPanel southPanel = new JPanel(new BorderLayout(5,5));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Pequena margem à volta da caixa de texto

        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        southPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0)); // Espaçamento entre botões
        sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendMessage());
        voiceButton = new JButton("🎤");
        voiceButton.addActionListener(e -> toggleVoiceListening());
        buttonPanel.add(sendButton);
        buttonPanel.add(voiceButton);
        southPanel.add(buttonPanel, BorderLayout.EAST);

        add(southPanel, BorderLayout.SOUTH);

        // Timer para animar a mensagem de "thinking"
        thinkingTimer = new Timer(500, e -> {
            chatList.repaint(); // Força o renderer a redesenhar para animar os "..."
        });

        new AIAssistantPresenter(this, appContext.getAIAssistantService());
        appendMessage("Olá! Sou o assistente TitanAxis. Como posso ajudar?", false);
    }

    @Override
    public void appendMessage(String text, boolean isUser) {
        SwingUtilities.invokeLater(() -> {
            ChatMessage.MessageType type = isUser ? ChatMessage.MessageType.USER : ChatMessage.MessageType.BOT;
            chatModel.addElement(new ChatMessage(text, type));
            scrollToBottom();
        });
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (listener != null && !text.isEmpty()) {
            listener.onSendMessage(text);
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
    public void clearUserInput() {
        inputField.setText("");
    }

    @Override
    public void showThinkingIndicator(boolean show) {
        SwingUtilities.invokeLater(() -> {
            // Remove qualquer indicador de "a pensar" anterior para evitar múltiplos
            if (!chatModel.isEmpty() && chatModel.lastElement().getType() == ChatMessage.MessageType.THINKING) {
                chatModel.removeElementAt(chatModel.getSize() - 1);
            }

            if (show) {
                chatModel.addElement(new ChatMessage("A pensar...", ChatMessage.MessageType.THINKING));
                if (!thinkingTimer.isRunning()) {
                    thinkingTimer.start();
                }
            } else {
                if (thinkingTimer.isRunning()) {
                    thinkingTimer.stop();
                }
            }
            scrollToBottom();
        });
    }

    /**
     * NOVO: Implementação do método que estava em falta.
     * Remove a última mensagem da lista do chat. Usado para remover
     * o indicador "A pensar..." antes de mostrar a resposta final.
     */
    @Override
    public void removeLastMessage() {
        SwingUtilities.invokeLater(() -> {
            if (!chatModel.isEmpty()) {
                chatModel.removeElementAt(chatModel.getSize() - 1);
            }
        });
    }

    @Override
    public void setSendButtonEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
        inputField.setEnabled(enabled);
        voiceButton.setEnabled(enabled);
    }

    @Override
    public void requestAction(Action action, Map<String, Object> params) {
        Component parent = SwingUtilities.getWindowAncestor(this);
        if (parent instanceof DashboardFrame) {
            ((DashboardFrame) parent).executeAction(action, params);
        }
    }

    private void scrollToBottom() {
        int lastIndex = chatModel.getSize() - 1;
        if (lastIndex >= 0) {
            chatList.ensureIndexIsVisible(lastIndex);
        }
    }

    @Override
    public void setListener(AIAssistantViewListener listener) {
        this.listener = listener;
    }

    public void refreshData() {
        // Nada a fazer aqui
    }
}