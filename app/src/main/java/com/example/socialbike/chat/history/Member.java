package com.example.socialbike.chat.history;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.socialbike.MainActivity;
import com.example.socialbike.chat.PreviewChatMessage;
import com.example.socialbike.chat.PreviewChatMessageDao;

import java.util.HashMap;

@Entity
public class Member {
    private static final HashMap<String, String> savedMembers = new HashMap<>();
    public static PreviewChatMessageDao memberDao = MainActivity.database.chatMemberDao();

    public static boolean contains(String id) {
        return savedMembers.containsKey("id");
    }

    public static void add(Member member) {
        if (!contains(member.publicKey))
            savedMembers.put(member.publicKey, member.name);
    }

    public static String getMemberName(String id) {
        if (!contains(id))
            return null;
        else
            return savedMembers.get(id);
    }

    @PrimaryKey
    @NonNull
    public String publicKey;

    @ColumnInfo
    public String name;

    public static void setup() {
        AsyncTask.execute(() -> {
            for (PreviewChatMessage member : memberDao.getAllMembers()) {
                savedMembers.put(member.publicKey, member.name);
            }
        });
    }
}
