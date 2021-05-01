package com.example.socialbike;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

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
        public TextView name, message, time, msgStyle, message_preview;
        public ImageView profilePicture;
        public Button start_conversation;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            //profilePicture = itemView.findViewById(R.id.status);
            time = itemView.findViewById(R.id.time);
            msgStyle = itemView.findViewById(R.id.msgStyle);
            start_conversation = itemView.findViewById(R.id.start_conversation);
            message_preview = itemView.findViewById(R.id.message_preview);
        }

        @Override
        public void onClick(View view) {
            if (msgItemListener != null)
                msgItemListener.onItemClick(view, getAdapterPosition());
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