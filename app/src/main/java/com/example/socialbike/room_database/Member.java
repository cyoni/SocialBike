package com.example.socialbike.room_database;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.socialbike.MainActivity;
import com.example.socialbike.RecyclerViewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

@Entity
public class Member {

    @PrimaryKey
    @NonNull
    public String publicKey;

    @ColumnInfo
    public String name;

    public Member(@NonNull String publicKey, String name) {
        this.publicKey = publicKey;
        this.name = name;
    }

    public static String getNameFromLocal(String publicKey) {
        return MainActivity.membersMap.getOrDefault(publicKey, "...");
    }

    public static void fetchName(TextView reference, String publicKey) {
        System.out.println("Getting name of " + publicKey + " from database...");
        MainActivity.mDatabase.child("public").child(publicKey).child("profile").child("nickname")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                                Member member = new Member(publicKey, String.valueOf(snapshot.getValue()));
                                MainActivity.membersMap.put(publicKey, member.name);
                                reference.setText(member.name);
                                try {
                                    MainActivity.memberDao.insert(member);
                                }
                                catch(Exception ignored){}
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void fetchAndSetName(RecyclerViewAdapter.ViewHolder holder, String name, String publicKey) {
        fetchAndSetName(holder.name, name, publicKey);
    }

    public static void fetchAndSetName(TextView reference, String name, String publicKey) {
        if (reference.getText().toString().equals("...")){
            String ans = getNameFromLocal(publicKey);
            if (ans.isEmpty() || ans.equals("..."))
                Member.fetchName(reference, publicKey);
            else
                reference.setText(ans);
        }
        else
            reference.setText(name);
    }
}





