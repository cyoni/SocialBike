package com.example.socialbike;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.socialbike.chat.PreviewChatMessage;
import com.example.socialbike.chat.PreviewChatMessageDao;
import com.example.socialbike.chat.history.History;
import com.example.socialbike.chat.history.HistoryDao;
import com.example.socialbike.chat.history.Member;
import com.example.socialbike.chat.history.MemberDao;

@Database(entities = {PreviewChatMessage.class, History.class, Member.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PreviewChatMessageDao chatMemberDao();
    public abstract HistoryDao historyDao();
    public abstract MemberDao memberDao();
}