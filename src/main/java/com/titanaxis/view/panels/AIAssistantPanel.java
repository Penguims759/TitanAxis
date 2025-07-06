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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Map;

public class AIAssistantPanel extends JPanel implements AIAssistantView {

    private AIAssistantView.AIAssistantViewListener listener;
    private final JTextField inputField;
    private final JButton sendButton;
    private final JButton voiceButton;
    private final VoiceRecognitionService voiceService;
    private final JLabel thinkingLabel;

    private final JList<ChatMessage> chatList;
    private final DefaultListModel<ChatMessage> chatModel;

    public AIAssistantPanel(AppContext appContext) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Assistente Interativo TitanAxis"));

        voiceService = new VoiceRecognitionService();

        // --- Lista de Chat com Renderizador Customizado ---
        chatModel = new DefaultListModel<>();
        chatList = new JList<>(chatModel);
        chatList.setCellRenderer(new ChatBubbleRenderer());
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setFixedCellHeight(-1);

        JScrollPane scrollPane = new JScrollPane(chatList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // --- Painel de Entrada ---
        JPanel southPanel = new JPanel(new BorderLayout(5,5));

        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        southPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendMessage());
        voiceButton = new JButton("🎤");
        voiceButton.addActionListener(e -> toggleVoiceListening());
        buttonPanel.add(sendButton);
        buttonPanel.add(voiceButton);
        southPanel.add(buttonPanel, BorderLayout.EAST);

        thinkingLabel = new JLabel("A pensar...", SwingConstants.CENTER);
        thinkingLabel.setFont(thinkingLabel.getFont().deriveFont(Font.ITALIC));
        thinkingLabel.setVisible(false);

        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.add(southPanel, BorderLayout.CENTER);
        inputContainer.add(thinkingLabel, BorderLayout.NORTH);

        add(inputContainer, BorderLayout.SOUTH);

        // Listener para redesenhar a lista quando o painel muda de tamanho
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                chatList.repaint();
            }
        });

        new AIAssistantPresenter(this, appContext.getAIAssistantService());
        appendMessage("Olá! Sou o assistente TitanAxis. Como posso ajudar?", false);
    }

    @Override
    public void appendMessage(String text, boolean isUser) {
        SwingUtilities.invokeLater(() -> {
            chatModel.addElement(new ChatMessage(text, isUser));
            int lastIndex = chatModel.getSize() - 1;
            if (lastIndex >= 0) {
                chatList.ensureIndexIsVisible(lastIndex);
            }
        });
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
    public void clearUserInput() {
        inputField.setText("");
    }

    @Override
    public void showThinkingIndicator(boolean show) {
        thinkingLabel.setVisible(show);
    }

    @Override
    public void setSendButtonEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
        inputField.setEnabled(enabled);
        voiceButton.setEnabled(enabled);
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
        // Nada a fazer aqui por enquanto
    }
}