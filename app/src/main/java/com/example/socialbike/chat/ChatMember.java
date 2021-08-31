package com.example.socialbike.chat;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatMember {

    @PrimaryKey
    @NonNull
    public String publicKey;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "previewMsg")
    public String previewMsg;

    @ColumnInfo(name = "time")
    public long time;

    @ColumnInfo(name = "isRead")
    public boolean isRead;

    public ChatMember(@NonNull String publicKey, String name){
        this.publicKey = publicKey;
        this.name = name;
        this.previewMsg = "";
    }

}





