package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.ChatMessage;
import com.titanaxis.model.ai.Action;
import com.titanaxis.presenter.AIAssistantPresenter;
import com.titanaxis.service.VoiceRecognitionService;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.interfaces.AIAssistantView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class AIAssistantPanel extends JPanel implements AIAssistantView {

    private AIAssistantView.AIAssistantViewListener listener;
    private JTextField inputField;
    private JButton sendButton;
    private JButton voiceButton;
    private VoiceRecognitionService voiceService;

    private JList<ChatMessage> chatList;
    private DefaultListModel<ChatMessage> chatModel;
    private Timer thinkingTimer;

    public AIAssistantPanel(AppContext appContext) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.3);
        splitPane.setDividerLocation(350);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(createHelpPanel());
        splitPane.setRightComponent(createChatPanel(appContext));

        add(splitPane, BorderLayout.CENTER);
    }

    private JComponent createHelpPanel() {
        JPanel helpPanel = new JPanel(new BorderLayout());
        helpPanel.setBorder(BorderFactory.createTitledBorder("Habilidades do Assistente"));

        JEditorPane helpTextPane = new JEditorPane();
        helpTextPane.setContentType("text/html;charset=UTF-8");
        helpTextPane.setEditable(false);
        helpTextPane.setOpaque(false);
        helpTextPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        String helpHtml = "<html><body style='font-family: Arial; font-size: 11pt;'>"
                + "<b>Experimente estes comandos:</b>"

                + "<p><b>\"inicie uma venda para o cliente</b> <i>Nome do Cliente</i><b>\"</b><br>"
                + "Abre o painel de vendas e seleciona o cliente."
                + "</p>"

                + "<p><b>\"qual o histórico de compras do cliente</b> <i>Nome do Cliente</i><b>\"</b><br>"
                + "Exibe um resumo das compras do cliente."
                + "</p>"

                + "<p><b>\"quais os melhores clientes\"</b><br>"
                + "Mostra um ranking dos clientes que mais compraram."
                + "</p>"

                + "<p><b>\"crie um novo cliente chamado</b> <i>Nome</i> <b>com o contato</b> <i>email@exemplo.com</i><b>\"</b><br>"
                + "Cria um novo cliente diretamente.</p>"

                + "<p><b>\"gere o relatório de vendas em pdf\"</b><br>"
                + "Inicia a geração de um relatório de vendas.</p>"

                + "<p><b>\"como adicionar um lote\"</b><br>"
                + "Mostra um guia visual de como adicionar um lote a um produto.</p>"

                + "<p><b>\"qual o produto mais vendido\"</b><br>"
                + "Analisa os dados e informa o produto com mais vendas.</p>"

                + "<p><b>\"mude o tema para claro\"</b> ou <b>\"...para escuro\"</b><br>"
                + "Altera o tema visual da aplicação.</p>"

                + "<p><b>\"quais produtos têm baixo estoque\"</b><br>"
                + "Lista os produtos com 10 ou menos unidades.</p>"

                + "<p><b>\"quais lotes estão para vencer\"</b><br>"
                + "Mostra os lotes que vencem nos próximos 30 dias.</p>"

                + "<br><b>Conversa Guiada:</b>"
                + "<p>Comece com <b>\"cadastrar um novo produto\"</b> e o assistente irá pedir as informações passo a passo.</p>"

                + "<hr><p><b>Dica:</b> A qualquer momento durante uma conversa, digite <b>cancelar</b> ou <b>sair</b> para interromper a ação atual.</p>"
                + "</body></html>";

        helpTextPane.setText(helpHtml);

        JScrollPane scrollPane = new JScrollPane(helpTextPane);
        scrollPane.setBorder(null);
        helpPanel.add(scrollPane, BorderLayout.CENTER);

        return helpPanel;
    }


    private JComponent createChatPanel(AppContext appContext) {
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));
        chatPanel.setBorder(BorderFactory.createTitledBorder("Assistente Interativo"));

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

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendMessage());
        voiceButton = new JButton("Voz");
        voiceButton.addActionListener(e -> toggleVoiceListening());

        // ALTERADO: Desativa o botão de voz se o serviço não estiver disponível
        if (!voiceService.isAvailable()) {
            voiceButton.setEnabled(false);
            voiceButton.setToolTipText("Serviço de voz indisponível no seu sistema.");
        }

        buttonPanel.add(sendButton);
        buttonPanel.add(voiceButton);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        chatPanel.add(southPanel, BorderLayout.SOUTH);

        thinkingTimer = new Timer(500, e -> chatList.repaint());

        new AIAssistantPresenter(this, appContext.getAIAssistantService());
        appendMessage("Olá! Sou o Assistente. Como posso ajudar?", false);

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
                chatModel.addElement(new ChatMessage("A pensar...", ChatMessage.MessageType.THINKING));
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

        // ALTERADO: Só ativa o botão de voz se o serviço estiver disponível
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