package com.titanaxis.view.panels.components;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.ai.ChatMessage;
import com.titanaxis.model.ai.Action;
import com.titanaxis.presenter.AIAssistantPresenter;
import com.titanaxis.service.VoiceRecognitionService;
import com.titanaxis.util.I18n;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.interfaces.AIAssistantView;
import com.titanaxis.view.renderer.ChatBubbleRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Map;

public class AIAssistantPanel extends JPanel implements AIAssistantView {

    private AIAssistantViewListener listener;
    private JTextField inputField;
    private JButton sendButton;
    private JButton voiceButton;
    private JButton copyButton;
    private VoiceRecognitionService voiceService;

    private JList<ChatMessage> chatList;
    private DefaultListModel<ChatMessage> chatModel;
    private Timer thinkingTimer;

    public AIAssistantPanel(AppContext appContext) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(createChatPanel(appContext), BorderLayout.CENTER);
    }

    
    public void stopServices() {
        if (voiceService != null && voiceService.isListening()) {
            voiceService.stopListening();
            voiceButton.setForeground(null);
        }
    }

    private JComponent createChatPanel(AppContext appContext) {
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));
        chatPanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("assistant.chat.title")));

        voiceService = new VoiceRecognitionService();

        chatModel = new DefaultListModel<>();
        chatList = new JList<>(chatModel);
        chatList.setCellRenderer(new ChatBubbleRenderer());
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setBackground(UIManager.getColor("Panel.background"));
        chatList.setSelectionBackground(new Color(0, 0, 0, 0));
        chatList.setSelectionForeground(chatList.getForeground());

        JScrollPane scrollPane = new JScrollPane(chatList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        southPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        sendButton = new JButton(I18n.getString("assistant.button.send"));
        sendButton.addActionListener(e -> sendMessage());
        voiceButton = new JButton(I18n.getString("assistant.button.voice"));
        voiceButton.addActionListener(e -> toggleVoiceListening());

        copyButton = new JButton(I18n.getString("assistant.button.copy"));
        copyButton.addActionListener(this::copyConversationToClipboard);

        if (!voiceService.isAvailable()) {
            voiceButton.setEnabled(false);
            voiceButton.setToolTipText(I18n.getString("assistant.tooltip.voiceUnavailable"));
        }

        buttonPanel.add(sendButton);
        buttonPanel.add(voiceButton);
        buttonPanel.add(copyButton);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        chatPanel.add(southPanel, BorderLayout.SOUTH);

        thinkingTimer = new Timer(500, e -> chatList.repaint());

        new AIAssistantPresenter(this, appContext.getAIAssistantService());

        return chatPanel;
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

    private void copyConversationToClipboard(ActionEvent e) {
        StringBuilder conversationText = new StringBuilder();
        for (int i = 0; i < chatModel.getSize(); i++) {
            ChatMessage message = chatModel.getElementAt(i);
            if (message.getType() != ChatMessage.MessageType.THINKING) {
                String prefix = message.isUser() ? I18n.getString("assistant.copy.userPrefix") : I18n.getString("assistant.copy.assistantPrefix");
                conversationText.append(prefix)
                        .append(message.getText().replace("<br>", "\n"))
                        .append("\n\n");
            }
        }

        StringSelection stringSelection = new StringSelection(conversationText.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        JOptionPane.showMessageDialog(this, I18n.getString("assistant.dialog.copy.message"), I18n.getString("assistant.dialog.copy.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void toggleVoiceListening() {
        if (!voiceService.isListening()) {
            voiceButton.setForeground(Color.RED);
            voiceService.startListening(transcribedText -> {
                if (voiceService.isListening()) {
                    voiceService.stopListening();
                    voiceButton.setForeground(null);
                }
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
            if (!chatModel.isEmpty() && chatModel.lastElement().getType() == ChatMessage.MessageType.THINKING) {
                chatModel.removeElementAt(chatModel.getSize() - 1);
            }
            if (show) {
                chatModel.addElement(new ChatMessage(I18n.getString("assistant.chat.thinking"), ChatMessage.MessageType.THINKING));
                if (!thinkingTimer.isRunning()) thinkingTimer.start();
            } else {
                if (thinkingTimer.isRunning()) thinkingTimer.stop();
            }
            scrollToBottom();
        });
    }

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
        copyButton.setEnabled(enabled);

        if (voiceService.isAvailable()) {
            voiceButton.setEnabled(enabled);
        }
    }

    @Override
    public void requestAction(Action action, Map<String, Object> params) {
        Component parent = SwingUtilities.getWindowAncestor(this);
        if (parent instanceof DashboardFrame) {
            ((DashboardFrame) parent).executeAction(action, params);
        }
    }

    @Override
    public void requestInputFieldFocus() {
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
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