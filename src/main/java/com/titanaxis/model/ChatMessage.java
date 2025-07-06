// src/main/java/com/titanaxis/model/ChatMessage.java
package com.titanaxis.model;

public class ChatMessage {
    public enum MessageType {
        USER,
        BOT,
        THINKING // NOVO TIPO
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

    // MÉTODOS DE CONVENIÊNCIA
    public boolean isUser() {
        return type == MessageType.USER;
    }
}