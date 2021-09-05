package com.example.socialbike.chat;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PreviewChatMessageDao {

    @Query("SELECT * FROM PreviewChatMessage ORDER BY time DESC")
    List<PreviewChatMessage> getAllMembers();
    //LiveData<List<ChatMember>> getAllMembers();

    @Query("SELECT * FROM PreviewChatMessage WHERE publicKey = (:userId)")
    List<PreviewChatMessage> getUserById(int userId);

    @Query("SELECT * FROM PreviewChatMessage WHERE name = (:name)")
    List<PreviewChatMessage> getUserByName(String name);

    @Query("UPDATE PreviewChatMessage SET unreadMessages = unreadMessages + 1 WHERE publicKey = :publicKey")
    void incrementUnreadMessages(String publicKey);

    @Query("UPDATE PreviewChatMessage SET unreadMessages = 0 WHERE publicKey = :publicKey")
    void resetUnreadMessages(String publicKey);

    @Insert
    void insert(PreviewChatMessage chatMember);

    @Update
    void update(PreviewChatMessage chatMember);

    @Delete
    void delete(PreviewChatMessage chatMember);

}