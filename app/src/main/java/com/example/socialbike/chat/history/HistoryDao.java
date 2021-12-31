package com.example.socialbike.chat.history;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface HistoryDao {

    // INNER JOIN Member on History.publicKey = Member.publicKey WHERE History.publicKey = :publicKey ORDER BY time ASC
    @Query("SELECT * FROM History WHERE publicKey = :publicKey ORDER BY time ASC")
    //List<History> getHistoryOfMember(String publicKey);
    LiveData<List<History>> getHistoryOfMember(String publicKey);

    @Insert
    void insert(History history);

    @Update
    void update(History history);

    @Delete
    void delete(History history);

    @Query("DELETE FROM History")
    void removeAll();
}