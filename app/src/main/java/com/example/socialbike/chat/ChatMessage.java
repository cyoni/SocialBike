package com.example.socialbike.chat;

public class ChatMessage {
    private final String messageId, senderPublicKey, sendersName, message;

    public ChatMessage(String messageId, String senderPublicKey, String sendersName, String message) {
        this.messageId = messageId;
        this.senderPublicKey = senderPublicKey;
        this.sendersName = sendersName;
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    public String getSendersName() {
        return sendersName;
    }

    public String getMessage() {
        return message;
    }

}
