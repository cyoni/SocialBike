package com.example.socialbike;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Post implements Parcelable {

    private final String postId, publicKey, name, msg;
    private final int time;
    private final ArrayList<Comment> commentsContainer = new ArrayList<>();

    public Post(String postId, String publicKey, String name, int time, String msg) {
        this.postId = postId;
        this.publicKey = publicKey;
        this.name = name;
        this.time = time;
        this.msg = msg;
    }

    protected Post(Parcel in) {
        postId = in.readString();
        publicKey = in.readString();
        name = in.readString();
        msg = in.readString();
        time = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postId);
        dest.writeString(publicKey);
        dest.writeString(name);
        dest.writeString(msg);
        dest.writeInt(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getPostId() {
        return postId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }

    public String getMsg() {
        return msg;
    }

    public int getTime() {
        return time;
    }

    public boolean hasComments() {
        return commentsContainer.size() > 0;
    }

    public void addComment(Comment comment) {
        commentsContainer.add(0, comment);
    }
}
