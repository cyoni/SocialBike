package com.example.socialbike;

public class Post {

    private final String publicKey, name, msg;
    private final int time;

    public Post(String publicKey, String name, int time, String msg) {
        this.publicKey = publicKey;
        this.name = name;
        this.time = time;
        this.msg = msg;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }

    public String getMsg() {
        return msg;
    }

    public int getTime() {
        return time;
    }

}
