package com.example.socialbike.chat;

public class ChatPreviewUser extends ChatMessage {

    public ChatPreviewUser(String messageId, String publicKey, String name, String messagePreview){
        super(messageId, publicKey, name, messagePreview,true);
    }

    public String getMessagePreview() {
        return message;
    }

    public String getName() {
        return sendersName;
    }

    public String getPublicKey() {
        return senderPublicKey;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
