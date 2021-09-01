package com.example.socialbike.chat;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChatMemberDao {

    @Query("SELECT * FROM ChatMember ORDER BY time DESC")
    List<ChatMember> getAllMembers();
    //LiveData<List<ChatMember>> getAllMembers();

    @Query("SELECT * FROM ChatMember WHERE publicKey = (:userId)")
    List<ChatMember> getUserById(int userId);

    @Query("SELECT * FROM ChatMember WHERE name = (:name)")
    List<ChatMember> getUserByName(String name);

    @Insert
    void insert(ChatMember chatMember);

    @Update
    void update(ChatMember chatMember);

    @Delete
    void delete(ChatMember chatMember);

}