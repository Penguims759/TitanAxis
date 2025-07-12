// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/panels/AIAssistantPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.ChatMessage;
import com.titanaxis.model.ai.Action;
import com.titanaxis.presenter.AIAssistantPresenter;
import com.titanaxis.service.VoiceRecognitionService;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.interfaces.AIAssistantView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Map;

public class AIAssistantPanel extends JPanel implements AIAssistantView {

    private AIAssistantView.AIAssistantViewListener listener;
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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerLocation(400);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(createHelpPanel());
        splitPane.setRightComponent(createChatPanel(appContext));

        add(splitPane, BorderLayout.CENTER);
    }

    private JComponent createHelpPanel() {
        JPanel helpPanel = new JPanel(new BorderLayout());
        helpPanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("assistant.help.title"))); // ALTERADO

        JEditorPane helpTextPane = new JEditorPane();
        helpTextPane.setContentType("text/html;charset=UTF-8");
        helpTextPane.setEditable(false);
        helpTextPane.setOpaque(false);
        helpTextPane.setBorder(new EmptyBorder(10, 15, 10, 15));

        // ALTERADO: Todas as strings foram substituídas por chaves I18n
        String helpHtml = "<html><body style='font-family: Arial; font-size: 11pt;'>"
                + "<h3>" + I18n.getString("assistant.help.subtitle") + "</h3>"

                + "<b><u>" + I18n.getString("assistant.help.section.sales") + "</u></b>"
                + "<ul>"
                + "<li><b>" + I18n.getString("assistant.help.sales.command1") + "</b></li>"
                + "<li><b>" + I18n.getString("assistant.help.sales.command2") + "</b></li>"
                + "<li><b>" + I18n.getString("assistant.help.sales.command3") + "</b></li>"
                + "<li><b>" + I18n.getString("assistant.help.sales.command4") + "</b></li>"
                + "</ul>"

                + "<b><u>" + I18n.getString("assistant.help.section.stock") + "</u></b>"
                + "<ul>"
                + "<li><b>" + I18n.getString("assistant.help.stock.command1") + "</b></li>"
                + "<li><b>" + I18n.getString("assistant.help.stock.command2") + "</b></li>"
                + "<li><b>" + I18n.getString("assistant.help.stock.command3") + "</b></li>"
                + "<li><b>" + I18n.getString("assistant.help.stock.command4") + "</b></li>"
                + "<li><b>" + I18n.getString("assistant.help.stock.command5") + "</b></li>"
                + "</ul>"

                + "<b><u>" + I18n.getString("assistant.help.section.reports") + "</u></b>"
                + "<ul>"
                + "<li><b>" + I18n.getString("assistant.help.reports.command1") + "</b></li>"
                + "<li><b>" + I18n.getString("assistant.help.reports.command2") + "</b></li>"
                + "<li><b>" + I18n.getString("assistant.help.reports.command3") + "</b></li>"
                + "</ul>"

                + "<hr>"
                + "<p><b>" + I18n.getString("assistant.help.tip.proactive") + "</b></p>"
                + "<p><b>" + I18n.getString("assistant.help.tip.cancel") + "</b></p>"

                + "</body></html>";

        helpTextPane.setText(helpHtml);

        JScrollPane scrollPane = new JScrollPane(helpTextPane);
        scrollPane.setBorder(null);
        helpPanel.add(scrollPane, BorderLayout.CENTER);

        return helpPanel;
    }


    private JComponent createChatPanel(AppContext appContext) {
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));
        chatPanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("assistant.chat.title"))); // ALTERADO

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
        sendButton = new JButton(I18n.getString("assistant.button.send")); // ALTERADO
        sendButton.addActionListener(e -> sendMessage());
        voiceButton = new JButton(I18n.getString("assistant.button.voice")); // ALTERADO
        voiceButton.addActionListener(e -> toggleVoiceListening());

        copyButton = new JButton(I18n.getString("assistant.button.copy")); // ALTERADO
        copyButton.addActionListener(this::copyConversationToClipboard);

        if (!voiceService.isAvailable()) {
            voiceButton.setEnabled(false);
            voiceButton.setToolTipText(I18n.getString("assistant.tooltip.voiceUnavailable")); // ALTERADO
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
                String prefix = message.isUser() ? I18n.getString("assistant.copy.userPrefix") : I18n.getString("assistant.copy.assistantPrefix"); // ALTERADO
                conversationText.append(prefix)
                        .append(message.getText().replace("<br>", "\n"))
                        .append("\n\n");
            }
        }

        StringSelection stringSelection = new StringSelection(conversationText.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        JOptionPane.showMessageDialog(this, I18n.getString("assistant.dialog.copy.message"), I18n.getString("assistant.dialog.copy.title"), JOptionPane.INFORMATION_MESSAGE); // ALTERADO
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
            if (!chatModel.isEmpty() && chatModel.lastElement().getType() == ChatMessage.MessageType.THINKING) {
                chatModel.removeElementAt(chatModel.getSize() - 1);
            }
            if (show) {
                chatModel.addElement(new ChatMessage(I18n.getString("assistant.chat.thinking"), ChatMessage.MessageType.THINKING)); // ALTERADO
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