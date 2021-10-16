package com.example.socialbike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialbike.groups.group.MusicAdapter;
import com.example.socialbike.room_database.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class EventsManager implements RecyclerViewAdapter.ItemClickListener {

    public final int NEW_EVENT_CODE = 100;
    public final EventsCommentsExtension eventsCommentsExtension;

    String MOST_RECENT_CODE = "MOST_RECENT";
    String TRADING_CODE = "TRADING";
    Updater.IUpdate update;
    String dataType = MOST_RECENT_CODE;
    public ArrayList<Event> container = new ArrayList<>();
    // public ArrayList<Event> extraEvents = new ArrayList<>();
    RecyclerView recyclerView;
    public RecyclerViewAdapter recyclerViewAdapter;
    public ProgressBar progressBar;
    int range = 10;
    Context context;
    Activity activity;
    TextView rangeText;
    public SwipeRefreshLayout swipe_refresh;


    public EventsManager(Activity activity, Context context, Updater.IUpdate update) {
        this.update = update;
        this.activity = activity;
        this.context = context;
        eventsCommentsExtension = new EventsCommentsExtension(this);
    }

    public Context getContext() {
        return context;
    }

    protected void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_events, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    public void getEvents(Map<String, Object> data) {
        System.out.println("getting Events...");
        container.clear();
        //  extraEvents.clear();
        data.put("dataType", dataType);
        MainActivity.mFunctions
                .getHttpsCallable("getEvents")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    if (!response.isEmpty()) {
                        parseMessages(response);
                    }
                    return "";
                });
    }

    public void parseMessages(String data) {

        if (data == null || data.length() == 0) {
            // no_events_text.setVisibility(View.VISIBLE);
            update.onFinishedUpdating();
            return;
        }

        if (container.size() > 0) {
            container.clear();
//         //   extraEvents.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            EventDTO eventDTO = objectMapper.readValue(data, EventDTO.class);
            container.addAll(eventDTO.getEvents());
            if (!eventDTO.getExtraEvents().isEmpty()) {
                container.add(null);
                container.addAll(eventDTO.getExtraEvents());
            }

            update.onFinishedUpdating();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void changeTypeOfSearch(String type) {
        dataType = type;
    }


    public void showProgressbar() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressbar() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        swipe_refresh.setRefreshing(false);
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Event event = container.get(position);
        if (event != null) {
            holder.title.setText(event.getTitle());
            Member.fetchAndSetName(holder.name, holder.name.getText().toString(), event.getPublicKey());
            String start = DateUtils.convertMiliToDateTime(event.getStart(), Consts.FULL_DATE_TIME);
            holder.date_and_time.setText(start);
            if (event.getNumParticipants() > 1) {
                holder.people_going.setVisibility(View.VISIBLE);
                holder.people_going.setText(event.getNumParticipants() + " people going");
            } else
                holder. people_going.setVisibility(View.GONE);
            if (event.getAddress() == null)
                holder.location.setVisibility(View.GONE);
            else
                holder.location.setText(event.getAddress());

            if (event.hasHeaderImage()){
                downloadImage();



            }

            holder.relativelayout.setOnClickListener(view -> {
                Intent intent = new Intent(getContext(), EventActivity.class);
                intent.putExtra("event", container.get(position));
                getContext().startActivity(intent);
            });
        }
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    private void openMap(LatLng latLng) {
        Intent intent = new Intent(getContext(), Maps.class);
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("lng", latLng.longitude);
        activity.startActivity(intent);
    }

    /*@Override
    public void onItemClick(@NonNull View holder, int position) {

    }*/

    protected void updateSearchText() {
        if (rangeText != null) {
            String str;
            if (range == 100) {
                str = "Shows " + container.size() + " events in";
            } else
                str = "Shows " + container.size() + " events within " + range + " km of";
            rangeText.setText(str);
        }
    }


    public void init(View root) {
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();
        progressBar = root.findViewById(R.id.progressBar);
        swipe_refresh = root.findViewById(R.id.swipe_refresh);
        progressBar.setVisibility(View.INVISIBLE);
    }
}
