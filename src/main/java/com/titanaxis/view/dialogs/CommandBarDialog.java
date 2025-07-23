// penguims759/titanaxis/Penguims759-TitanAxis-e9669e5c4e163f98311d4f51683c348827675c7a/src/main/java/com/titanaxis/view/dialogs/CommandBarDialog.java
package com.titanaxis.view.dialogs;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.ChatMessage;
import com.titanaxis.model.ai.Action;
import com.titanaxis.presenter.AIAssistantPresenter;
import com.titanaxis.service.VoiceRecognitionService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.interfaces.AIAssistantView;
import com.titanaxis.view.panels.ChatBubbleRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.Map;

public class CommandBarDialog extends JDialog implements AIAssistantView {

    private final AppContext appContext;
    private final DashboardFrame ownerFrame;
    private JTextField commandField;
    private JList<ChatMessage> resultsList;
    private DefaultListModel<ChatMessage> listModel;
    private AIAssistantViewListener listener;
    private Timer thinkingTimer;

    private JButton voiceButton;
    private JButton copyButton;
    private VoiceRecognitionService voiceService;


    public CommandBarDialog(Frame owner, AppContext appContext) {
        super(owner, true);
        this.appContext = appContext;
        this.ownerFrame = (DashboardFrame) owner;
        initComponents();
        new AIAssistantPresenter(this, appContext.getAIAssistantService());
        SwingUtilities.invokeLater(() -> listener.onViewOpened());
    }

    private void initComponents() {
        setUndecorated(true);
        setSize(600, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setBackground(new Color(0, 0, 0, 0));
        getRootPane().setOpaque(false);

        JPanel contentPanel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIManager.getColor("Panel.background"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UIManager.getColor("Separator.foreground"));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        commandField = new JTextField();
        commandField.setFont(new Font("Arial", Font.PLAIN, 18));
        commandField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        commandField.addActionListener(e -> sendMessage());

        listModel = new DefaultListModel<>();
        resultsList = new JList<>(listModel);
        resultsList.setCellRenderer(new ChatBubbleRenderer());
        resultsList.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane scrollPane = new JScrollPane(resultsList);
        scrollPane.setBorder(null);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(createSouthPanel(), BorderLayout.SOUTH);

        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        javax.swing.Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                commandField.requestFocusInWindow();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // ALTERAÇÃO: Garante que o overlay é removido quando o diálogo fecha
                if (ownerFrame != null) {
                    ownerFrame.setOverlayVisible(false);
                }
                appContext.getAIAssistantService().getContext().fullReset();
            }
        });

        thinkingTimer = new Timer(500, e -> resultsList.repaint());

        add(contentPanel);
    }

    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setOpaque(false);
        southPanel.add(commandField, BorderLayout.CENTER);

        voiceService = new VoiceRecognitionService();
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setOpaque(false);

        voiceButton = new JButton(I18n.getString("assistant.button.voice"));
        voiceButton.addActionListener(e -> toggleVoiceListening());

        copyButton = new JButton(I18n.getString("assistant.button.copy"));
        copyButton.addActionListener(this::copyConversationToClipboard);

        if (!voiceService.isAvailable()) {
            voiceButton.setEnabled(false);
            voiceButton.setToolTipText(I18n.getString("assistant.tooltip.voiceUnavailable"));
        }

        buttonPanel.add(voiceButton);
        buttonPanel.add(copyButton);
        southPanel.add(buttonPanel, BorderLayout.EAST);

        return southPanel;
    }

    private void sendMessage() {
        String text = getUserInput();
        if (listener != null && !text.isEmpty()) {
            listener.onSendMessage(text);
        }
    }

    private void toggleVoiceListening() {
        if (!voiceService.isListening()) {
            voiceButton.setForeground(Color.RED);
            voiceService.startListening(transcribedText -> {
                commandField.setText(transcribedText);
                sendMessage();
            });
        } else {
            voiceButton.setForeground(null);
            voiceService.stopListening();
        }
    }

    private void copyConversationToClipboard(ActionEvent e) {
        StringBuilder conversationText = new StringBuilder();
        for (int i = 0; i < listModel.getSize(); i++) {
            ChatMessage message = listModel.getElementAt(i);
            if (message.getType() != ChatMessage.MessageType.THINKING) {
                String prefix = message.isUser() ? I18n.getString("assistant.copy.userPrefix") : I18n.getString("assistant.copy.assistantPrefix");
                conversationText.append(prefix)
                        .append(message.getText().replace("<br>", "\n"))
                        .append("\n\n");
            }
        }

        StringSelection stringSelection = new StringSelection(conversationText.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        UIMessageUtil.showInfoMessage(this, I18n.getString("assistant.dialog.copy.message"), I18n.getString("assistant.dialog.copy.title"));
    }


    private void scrollToBottom() {
        int lastIndex = listModel.getSize() - 1;
        if (lastIndex >= 0) {
            resultsList.ensureIndexIsVisible(lastIndex);
        }
    }

    @Override
    public String getUserInput() {
        return commandField.getText();
    }

    @Override
    public void clearUserInput() {
        commandField.setText("");
    }

    @Override
    public void setSendButtonEnabled(boolean enabled) {
        commandField.setEnabled(enabled);
        copyButton.setEnabled(enabled);
        if (voiceService.isAvailable()) {
            voiceButton.setEnabled(enabled);
        }
    }

    @Override
    public void showThinkingIndicator(boolean show) {
        SwingUtilities.invokeLater(() -> {
            if (!listModel.isEmpty() && listModel.lastElement().getType() == ChatMessage.MessageType.THINKING) {
                listModel.removeElementAt(listModel.getSize() - 1);
            }
            if (show) {
                listModel.addElement(new ChatMessage(I18n.getString("assistant.chat.thinking"), ChatMessage.MessageType.THINKING));
                if (!thinkingTimer.isRunning()) thinkingTimer.start();
            } else {
                if (thinkingTimer.isRunning()) thinkingTimer.stop();
            }
            scrollToBottom();
        });
    }

    @Override
    public void appendMessage(String message, boolean isUser) {
        SwingUtilities.invokeLater(() -> {
            ChatMessage.MessageType type = isUser ? ChatMessage.MessageType.USER : ChatMessage.MessageType.BOT;
            listModel.addElement(new ChatMessage(message, type));
            scrollToBottom();
        });
    }

    @Override
    public void removeLastMessage() {
        SwingUtilities.invokeLater(() -> {
            if (!listModel.isEmpty()) {
                listModel.removeElementAt(listModel.getSize() - 1);
            }
        });
    }

    @Override
    public void requestAction(Action action, Map<String, Object> params) {
        if (action != Action.AWAITING_INFO) {
            dispose();
            if (ownerFrame != null) {
                ownerFrame.executeAction(action, params);
            }
        }
    }

    @Override
    public void requestInputFieldFocus() {
        commandField.requestFocusInWindow();
    }

    @Override
    public void setListener(AIAssistantViewListener listener) {
        this.listener = listener;
    }
}