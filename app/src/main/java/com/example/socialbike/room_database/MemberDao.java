package com.example.socialbike.room_database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MemberDao {

    @Query("SELECT * FROM Member")
    List<Member> getAllMembers();

    @Query("SELECT * FROM Member WHERE publicKey = (:userId)")
    List<Member> getUserById(int userId);

    @Query("SELECT * FROM Member WHERE name = (:name)")
    List<Member> getUserByName(String name);

    @Insert
    void insert(Member member);

    @Update
    void update(Member member);

    @Delete
    void delete(Member member);

}