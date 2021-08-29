package com.example.socialbike;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ViewHolder viewHolder;
    private List mData; // reference
    private LayoutInflater mInflater;
    private ItemClickListener msgItemListener;
    private int layout;

    // data is passed into the constructor
    public RecyclerViewAdapter(Context context, int layout, List mData) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = mData;
        this.layout = layout;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        msgItemListener.onBinding(holder, position);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, message, date,
                time, msgStyle, message_preview,
                comments, people_going, locationName, red_dot, likes, comments_count,
                replyButton, likeTextButton;
        public Button mapButton, interested, coming;
        public RelativeLayout layout;
        public ImageButton commentsButton, likeButton, followButton;
        public Button commentButton, postCommentButton, who_is_coming;
        public RelativeLayout newCommentSection;
        public LinearLayout commentLayout;
        public Button who_is_interested;
        public ProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            layout = itemView.findViewById(R.id.layout);
            commentsButton = itemView.findViewById(R.id.commentsButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            comments_count = itemView.findViewById(R.id.comments);
            likeButton = itemView.findViewById(R.id.likeButton);
            likes = itemView.findViewById(R.id.likes);
            postCommentButton = itemView.findViewById(R.id.postCommentButton);
            comments = itemView.findViewById(R.id.headCommentText);
            newCommentSection = itemView.findViewById(R.id.newCommentSection);
            commentLayout = itemView.findViewById(R.id.commentLayout);
            interested = itemView.findViewById(R.id.interested);
            coming = itemView.findViewById(R.id.coming);
            who_is_coming = itemView.findViewById(R.id.who_is_coming);
            who_is_interested = itemView.findViewById(R.id.who_is_interested);
            people_going = itemView.findViewById(R.id.people_going);
            progressBar = itemView.findViewById(R.id.progressBar);
            mapButton = itemView.findViewById(R.id.mapButton);
            locationName = itemView.findViewById(R.id.locationName);
            red_dot = itemView.findViewById(R.id.red_dot);
            followButton = itemView.findViewById(R.id.followButton);
            replyButton = itemView.findViewById(R.id.replyButton);
            likeTextButton = itemView.findViewById(R.id.likeTextButton);

            time = itemView.findViewById(R.id.time);
            msgStyle = itemView.findViewById(R.id.msgStyle);
            // start_conversation = itemView.findViewById(R.id.start_conversation);
            message_preview = itemView.findViewById(R.id.message_preview);
        }

        @Override
        public void onClick(View view) {
            if (msgItemListener != null)
                msgItemListener.onItemClick(view, getAdapterPosition());
        }

        public void fresh() {
            comments = itemView.findViewById(R.id.headCommentText);
        }
    }

    // convenience method for getting data at click position
    //Item getItem(int id) {
    //   return mData.get(id);
    //  }

    // allows click events to be caught
    public void setClassReference(Object itemClickListener) {
        this.msgItemListener = (ItemClickListener) itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onBinding(@NonNull ViewHolder holder, int position);

        void onItemClick(@NonNull View holder, int position);
    }
}