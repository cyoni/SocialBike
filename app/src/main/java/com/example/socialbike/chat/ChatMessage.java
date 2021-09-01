package com.example.socialbike.chat;

public class ChatMessage {

    protected final String messageId, senderPublicKey, sendersName;
    protected String message;
    private final boolean isIncomingMessage;
    private final long time;

    public ChatMessage(String messageId, String senderPublicKey, String sendersName, String message, boolean isIncomingMessage, long time) {
        this.messageId = messageId;
        this.senderPublicKey = senderPublicKey;
        this.sendersName = sendersName;
        this.message = message;
        this.isIncomingMessage = isIncomingMessage;
        this.time = time;
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

    public boolean isIncomingMessage() {
        return isIncomingMessage;
    }

    public boolean getIsRead() {
        return false;
    }

    public long getTime() {
        return time;
    }
}
