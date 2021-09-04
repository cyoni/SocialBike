package com.example.socialbike.chat.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class History {

    @PrimaryKey(autoGenerate = true)
    public int messageId;

    public String publicKey;

    @ColumnInfo
    public String message;

    @ColumnInfo
    public long time;

    @ColumnInfo
    public boolean isIncoming;

    public String name;

    public History(String publicKey,
                   String message,
                   long time,
                   boolean isIncoming) {
        this.publicKey = publicKey;
        this.message = message;
        this.time = time;
        this.isIncoming = isIncoming;
    }

}
