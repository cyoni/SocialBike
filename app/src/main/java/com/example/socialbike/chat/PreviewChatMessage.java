package com.example.socialbike.chat;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PreviewChatMessage {

    @PrimaryKey
    @NonNull
    public String publicKey;

    @ColumnInfo
    public String name;

    @ColumnInfo
    public String previewMsg;

    @ColumnInfo
    public long time;

    @ColumnInfo
    public int unreadMessages;

    public PreviewChatMessage(@NonNull String publicKey,
                              String name,
                              String previewMsg,
                              long time, int unreadMessages) {
        this.publicKey = publicKey;
        this.name = name;
        this.previewMsg = previewMsg;
        this.time = time;
        this.unreadMessages = unreadMessages;
    }
}





