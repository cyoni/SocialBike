package com.example.socialbike.chat.history;

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

    @Query("SELECT * FROM Member WHERE publicKey = (:publicKey)")
    List<Member> getMember(String publicKey);

    @Insert
    void insert(Member member);

    @Update
    void update(Member member);

    @Delete
    void delete(Member member);

}
