package com.example.socialbike.chat;

public class ChatUser {

    private final String name, publicKey, messagePreview;

    public ChatUser(String publicKey, String name, String messagePreview){
        this.publicKey = publicKey;
        this.name = name;
        this.messagePreview = messagePreview;
    }

    public String getMessagePreview() {
        return messagePreview;
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }

}
