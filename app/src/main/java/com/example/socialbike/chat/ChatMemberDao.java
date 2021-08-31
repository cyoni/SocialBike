package com.example.socialbike.chat;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMemberDao {

    @Query("SELECT * FROM ChatMember")
    List<ChatMember> getAllMembers();
   // LiveData<List<ChatMember>> getAllMembers();

    @Query("SELECT * FROM ChatMember WHERE publicKey = (:userId)")
    List<ChatMember> getUserById(int userId);

    @Query("SELECT * FROM ChatMember WHERE name = (:name)")
    List<ChatMember> getUserByName(String name);

    @Insert
    void insert(ChatMember chatMember);

    @Delete
    void delete(ChatMember chatMember);
}