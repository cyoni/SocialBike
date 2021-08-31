package com.example.socialbike;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.socialbike.chat.ChatMember;
import com.example.socialbike.chat.ChatMemberDao;
import com.google.firebase.firestore.auth.User;

@Database(entities = {ChatMember.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatMemberDao chatMemberDao();
}