package com.example.socialbike.utilities;

public class ConnectedUser {

    private static String publicKey;
    private static String nickname;
    private static Position position;

    public static void setPublicKey(String PublicKey) {
        publicKey = PublicKey;
    }
    public static void setNickname(String new_nickname) {
        nickname = new_nickname;
    }
    public static void setPosition(Position new_position) {
        new_position = position;
    }
    public static String getPublicKey() {
        return publicKey;
    }
    public static String getName() {
        return nickname;
    }
    public static Position getPosition() { return position; }

}
