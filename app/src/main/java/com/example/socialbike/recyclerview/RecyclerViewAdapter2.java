package com.example.socialbike.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbike.R;

import java.util.List;

public class RecyclerViewAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    //private ViewHolder viewHolder;
    private List mData; // reference
    private LayoutInflater mInflater;
    private ItemClickListener classReference;
    private int layout;


    // data is passed into the constructor
    public RecyclerViewAdapter2(Context context, int layout, List mData) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = mData;
        this.layout = layout;
    }

    @Override
    public int getItemViewType(int position) {
        return classReference.getItemViewType(position);
    }

    public RecyclerViewAdapter2.ViewHolder getLayoutView(View view){
        return new ViewHolder(view);
    }


    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return classReference.OnCreateViewHolder(parent, viewType);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        classReference.onBinding( holder, position);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, message, date, date_and_time,
                time, msgStyle, message_preview,
                comments, people_going, locationName, red_dot, likes, comments_count,
                replyButton, likeTextButton, description, title, memberCount, location;
        public TextView mapButton;
        public Button interested, coming, joinButton;
        public RelativeLayout layout, event_picture_layout;
        public ImageButton commentsButton, likeButton, followButton, menu_button;
        public Button commentButton, postCommentButton, who_is_coming;
        public RelativeLayout relativelayout;
        public LinearLayout commentLayout;
        public Button who_is_interested;
        public ProgressBar progressBar, picture_loader;
        public ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.description);
            layout = itemView.findViewById(R.id.layout);
            commentsButton = itemView.findViewById(R.id.commentsButton);
            description = itemView.findViewById(R.id.description);
            location = itemView.findViewById(R.id.location);
            title = itemView.findViewById(R.id.title);
            //      commentButton = itemView.findViewById(R.id.commentButton);
            comments_count = itemView.findViewById(R.id.comments);
            likeButton = itemView.findViewById(R.id.likeButton);
            likes = itemView.findViewById(R.id.likes);
            postCommentButton = itemView.findViewById(R.id.postCommentButton);
            comments = itemView.findViewById(R.id.headCommentText);
            relativelayout = itemView.findViewById(R.id.relativelayout);
            commentLayout = itemView.findViewById(R.id.commentLayout);
            date_and_time = itemView.findViewById(R.id.date_and_time);
            //     interested = itemView.findViewById(R.id.interested);
            //    coming = itemView.findViewById(R.id.coming);
            //     who_is_coming = itemView.findViewById(R.id.who_is_coming);
            //     who_is_interested = itemView.findViewById(R.id.who_is_interested);
            people_going = itemView.findViewById(R.id.going_count);
            progressBar = itemView.findViewById(R.id.progressBar);
            mapButton = itemView.findViewById(R.id.map_button);
            memberCount = itemView.findViewById(R.id.memberCount);
            //    locationName = itemView.findViewById(R.id.locationName);
            red_dot = itemView.findViewById(R.id.red_dot);
            followButton = itemView.findViewById(R.id.followButton);
            replyButton = itemView.findViewById(R.id.replyButton);
            likeTextButton = itemView.findViewById(R.id.likeTextButton);
            joinButton = itemView.findViewById(R.id.joinButton);

            time = itemView.findViewById(R.id.time);
            msgStyle = itemView.findViewById(R.id.msgStyle);
            // start_conversation = itemView.findViewById(R.id.start_conversation);
            message_preview = itemView.findViewById(R.id.message_preview);
            image = itemView.findViewById(R.id.image);
            event_picture_layout = itemView.findViewById(R.id.event_picture_layout);
            picture_loader = itemView.findViewById(R.id.picture_loader);
            menu_button = itemView.findViewById(R.id.menu_button);

        }

        @Override
        public void onClick(View view) {
            if (classReference != null)
                classReference.onItemClick(view, getAdapterPosition());
        }
    }

    class second extends RecyclerView.ViewHolder{

        public second(@NonNull View itemView) {
            super(itemView);
        }
    }

    // allows click events to be caught
    public void setClassReference(Object itemClickListener) {
        this.classReference = (ItemClickListener) itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onBinding(@NonNull RecyclerView.ViewHolder holder, int position);
        void onItemClick(@NonNull View holder, int position);
        int getItemViewType(int position);
        RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType);
    }
}