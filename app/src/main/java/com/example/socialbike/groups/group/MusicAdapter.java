package com.example.socialbike.groups.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbike.Event;
import com.example.socialbike.R;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int DIVIDER_LAYOUT = 0;
    private static final int EVENT_LAYOUT = 1;

    private List<Event> events;
   // private List<Event> extraEvents;
    private Context context;
    private int CODE_DIVIDER;

    public MusicAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
     //   this.extraEvents = extraEvents;
        CODE_DIVIDER = events.size();
    }

    @Override
    public int getItemViewType(int position) {
        System.out.println(position + "@@@@@@, is null:" + (events.get(position)==null));
        if (events.get(position) == null)
            return DIVIDER_LAYOUT;
        else
            return EVENT_LAYOUT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if (viewType == DIVIDER_LAYOUT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_more_devider, parent, false);
            viewHolder = new UserActivityViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_events, parent, false);
            viewHolder = new MusicViewHolder(view);
        }

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == DIVIDER_LAYOUT) {
            //   UserActivityViewHolder userActivityViewHolder = (UserActivityViewHolder)holder;
            //   updateLoginSuccess(userActivityViewHolder);
        } else {

            MusicViewHolder musicViewHolder = (MusicViewHolder) holder;

          /*  final Music music = musicList.get(position-1);             ///// Check what if list is empty
            musicViewHolder.songName.setText(music.getSong());
            musicViewHolder.artist.setText(music.getArtists());

            Glide.with(context)
                    .load(music.getCover_image())
                    .centerCrop()
                    .into(musicViewHolder.cover_image);*/


        }


    }

    @Override
    public int getItemCount() {
        return events.size();
    }


    class UserActivityViewHolder extends RecyclerView.ViewHolder {

        private TextView extra_title;

        public UserActivityViewHolder(View itemView) {
            super(itemView);

            extra_title = itemView.findViewById(R.id.extra_title);


        }
    }

    class MusicViewHolder extends RecyclerView.ViewHolder {

        private TextView extra_title;

        public MusicViewHolder(View itemView) {
            super(itemView);
            extra_title = itemView.findViewById(R.id.extra_title);
        }

    }
}