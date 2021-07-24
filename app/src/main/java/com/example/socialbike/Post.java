package com.example.socialbike;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Post implements Parcelable {

    private final String postId, publicKey, name, msg;
    private boolean isLiked = false;
    private final int time, commentsNumber;
    public static String POSTS_CONTAINER_CODE = "global_posts";
    public String DatabaseContainer;
    protected final ArrayList<Comment> commentsContainer = new ArrayList<>();
    private int likes = 0;

    public Post(String postId, String publicKey, String name, int time, String msg, int commentsNumber) {
        this.postId = postId;
        this.publicKey = publicKey;
        this.name = name;
        this.time = time;
        this.msg = msg;
        this.commentsNumber = commentsNumber;
    }

    protected Post(Parcel in) {
        postId = in.readString();
        publicKey = in.readString();
        name = in.readString();
        msg = in.readString();
        time = in.readInt();
        likes = in.readInt();
        commentsNumber = in.readInt();
        isLiked = in.readBoolean();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postId);
        dest.writeString(publicKey);
        dest.writeString(name);
        dest.writeString(msg);
        dest.writeInt(time);
        dest.writeInt(likes);
        dest.writeBoolean(isLiked);
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

    public int getCommentsNumber() {
        return commentsNumber;
    }

    public boolean hasComments() {
        return commentsNumber > 0;
    }

    public void addComment(Comment comment) {
        commentsContainer.add(0, comment);
    }

    public boolean getIsLiked(){
        return isLiked;
    }

    public void setIsLiked(boolean isLiked){
        this.isLiked = isLiked;
    }

    public Task<HttpsCallableResult> getComments() {
        System.out.println("Getting comments for post #" + getPostId());
        Map<String, Object> data = new HashMap<>();
        data.put("postId", getPostId());
        data.put("container", DatabaseContainer);
        return MainActivity.mFunctions
                .getHttpsCallable("getComments")
                .call(data);
    }


    public void incrementLike() {
        this.likes++;
    }
    public void decrementLike() {
        this.likes--;
    }

    public int getLikes() {
        return likes;
    }
}
