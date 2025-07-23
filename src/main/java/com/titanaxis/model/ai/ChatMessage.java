package com.titanaxis.model.ai;

public class ChatMessage {
    public enum MessageType {
        USER,
        BOT,
        THINKING
    }

    private final String text;
    private final MessageType type;

    public ChatMessage(String text, MessageType type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public MessageType getType() {
        return type;
    }

    public boolean isUser() {
        return type == MessageType.USER;
    }
}