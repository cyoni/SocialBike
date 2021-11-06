package com.example.socialbike.events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialbike.utilities.DateUtils;
import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.utilities.Maps;
import com.example.socialbike.R;
import com.example.socialbike.recyclerview.RecyclerViewAdapter;
import com.example.socialbike.utilities.Updater;
import com.example.socialbike.activities.EventActivity;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.model.EventDTO;
import com.example.socialbike.room_database.Member;
import com.example.socialbike.utilities.Consts;
import com.example.socialbike.utilities.ImageManager;
import com.example.socialbike.utilities.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class EventsManager implements RecyclerViewAdapter.ItemClickListener {

    public final int NEW_EVENT_CODE = 100;
    public final EventsCommentsExtension eventsCommentsExtension;

    public String MOST_RECENT_CODE = "MOST_RECENT";
    public String TRADING_CODE = "TRADING";
    Updater.IUpdate update;
    String dataType = MOST_RECENT_CODE;
    public ArrayList<Event> container = new ArrayList<>();
    // public ArrayList<Event> extraEvents = new ArrayList<>();
    RecyclerView recyclerView;
    public RecyclerViewAdapter recyclerViewAdapter;
    public ProgressBar progressBar;
    Context context;
    Activity activity;
    public TextView rangeText;
    public SwipeRefreshLayout swipe_refresh;
    ImageManager imageManager;


    public EventsManager(Activity activity, Context context, Updater.IUpdate update) {
        this.update = update;
        this.activity = activity;
        this.context = context;
        eventsCommentsExtension = new EventsCommentsExtension(this);
        imageManager = new ImageManager(activity);
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
        Utils.PostData(EMethods.getEvents, data)
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

    public void changeTypeOfSearch(String type) {
        dataType = type;
    }


    public void showProgressbar() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressbar() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        if (swipe_refresh != null)
            swipe_refresh.setRefreshing(false);
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Event event = container.get(position);
        if (event != null) {
            holder.event_picture_layout.setVisibility(View.VISIBLE);
            holder.image.setImageBitmap(null);

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

            if (event.getHasHeaderPicture()){

                if (imageManager.doesPictureExistLocally("event_picture_headers", event.getEventId())){
                    imageManager.setImage(imageManager.loadPictureLocally("event_picture_headers", event.getEventId()), holder.image);
                    holder.picture_loader.setVisibility(View.GONE);
                }
                else {
                    StorageReference ref = getPath(event.getGroupId(), event.getEventId());
                    imageManager.downloadPicture(ref).addOnSuccessListener(bytes -> {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageManager.setImage(bmp, holder.image);
                        imageManager.locallySavePicture(bmp, "event_picture_headers", event.getEventId());
                        holder.event_picture_layout.setVisibility(View.VISIBLE);
                        holder.picture_loader.setVisibility(View.GONE);
                    });
                }
            }
            else
                holder.event_picture_layout.setVisibility(View.GONE);

            holder.relativelayout.setOnClickListener(view -> {
                Intent intent = new Intent(getContext(), EventActivity.class);
                intent.putExtra("event", container.get(position));
                getContext().startActivity(intent);
            });
        }
    }



    private StorageReference getPath(String groupId, String eventId) {
        StorageReference ref = MainActivity.storageRef;
        if (groupId != null && eventId != null)
            ref = ref.child("groups").child(groupId).child("events").child(eventId);
        else if (groupId == null && eventId != null)
            ref = ref.child("events").child(eventId);
        ref = ref.child("header");
        return ref;
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

    public void updateSearchText() {
        if (rangeText != null) {
            String str = "Shows " + container.size() + " events near";
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

    public void init() {
        recyclerView = activity.findViewById(R.id.recyclerview);
        initAdapter();
        progressBar = activity.findViewById(R.id.progressBar);
        swipe_refresh = activity.findViewById(R.id.swipe_refresh);
        progressBar.setVisibility(View.INVISIBLE);
    }
}
