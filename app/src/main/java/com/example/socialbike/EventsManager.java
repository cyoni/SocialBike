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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventsManager implements RecyclerViewAdapter.ItemClickListener {

    public final int NEW_EVENT_CODE = 100;
    public final EventsCommentsExtension eventsCommentsExtension;

    String MOST_RECENT_CODE = "MOST_RECENT";
    String TRADING_CODE = "TRADING";
    Updater.IUpdate update;
    String dataType = MOST_RECENT_CODE;
    public ArrayList<Event> container = new ArrayList<>();
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

    public Context getContext(){
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

    public void parseMessages(String rawData) {
        JSONArray data = null;
        JSONObject obj;
        try {
            obj = new JSONObject(rawData);
            data = obj.getJSONArray("events");
        } catch (Exception e) {
            System.out.println("An error was caught in message fetcher: " + e.getMessage());
        }
        if (data == null || data.length() == 0) {
            // no_events_text.setVisibility(View.VISIBLE);
            update.onFinishedUpdating();
            return;
        }

        if (container.size() > 0 && data.length() > 0) {
            container.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            EventDTO eventDTO = objectMapper.readValue(rawData, EventDTO.class);
            container.addAll(eventDTO.getEvents());
            update.onFinishedUpdating();
        } catch (IOException e) {
            e.printStackTrace();
        }

/*
        for (int i = 0; i < data.length(); i++) {

            String user_public_key = null;
            try {
                user_public_key = data.getJSONObject(i).getString("user_public_key");
                String eventDetails = data.getJSONObject(i).getString("details");
                String name = data.getJSONObject(i).getString("name");
                String dateOfEvent = data.getJSONObject(i).getString("date");
                String timeOfEvent = data.getJSONObject(i).getString("time");
                String created_event_time = data.getJSONObject(i).getString("created_event_time");
                String event_id = data.getJSONObject(i).getString("event_id");
                String num_interested_members = data.getJSONObject(i).getString("num_interested_members");
                String num_participants = data.getJSONObject(i).getString("num_participants");
                String title = data.getJSONObject(i).getString("title");
                String address = data.getJSONObject(i).getString("address");
                double lat = data.getJSONObject(i).getDouble("lat");
                double lng = data.getJSONObject(i).getDouble("lng");
                int comments_num = data.getJSONObject(i).getInt("comments_num");

                int numberOfParticipants = 0;
                if (data.getJSONObject(i).has("numberOfParticipants")
                        && data.getJSONObject(i).get("numberOfParticipants") instanceof Integer)
                    numberOfParticipants = data.getJSONObject(i).getInt("numberOfParticipants");

                Position position = new Position(new LatLng(lat, lng), title, address);
                Event event = new Event(
                        event_id, user_public_key, name,
                        dateOfEvent, timeOfEvent, created_event_time,
                        num_interested_members, numberOfParticipants,
                        position, eventDetails, comments_num
                );

                container.add(event);

                System.out.println("event  " + i + " " + eventDetails);
            } catch (JSONException e) {
                System.out.println("An error was caught in message fetcher: " + e.getMessage());
            }
        }
*/
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
    //    holder.message.setText(container.get(position).getMsg());
     //   holder.locationName.setText(container.get(position).getPosition().getLocationName());
     //   holder.time.setText(container.get(position).getTimeOfEvent());
     //   holder.date.setText(container.get(position).getDateOfEvent());
   /*     holder.name.setText(container.get(position).getName());
        holder.people_going.setText(container.get(position).getNumberOfParticipants() + " people going");
        holder.interested.setOnClickListener(view -> markAsInterested(holder, position));
        holder.coming.setOnClickListener(view -> markAsGoing(holder, position));
        holder.who_is_coming.setOnClickListener(view -> showWhoIsGoing(holder, position));
        holder.who_is_interested.setOnClickListener(view -> showWhoIsInterested(holder, position));
        holder.commentButton.setOnClickListener(view -> eventsCommentsExtension.commentButton(holder, position));
        holder.mapButton.setOnClickListener(view -> openMap(container.get(position).getLatLng()));*/
        // holder.amountOfInterestedPeople.setText(container.get(position).getAmountOfInterestedPeople());
        holder.relativelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EventActivity.class);
                if (container.get(position).getGroupId() != null)
                    intent.putExtra("groupId", container.get(position).getGroupId());
                intent.putExtra("eventId", container.get(position).getEventId());
                getContext().startActivity(intent);
            }
        });
    }

    private void openMap(LatLng latLng) {
        Intent intent = new Intent(getContext(), Maps.class);
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("lng", latLng.longitude);
        activity.startActivity(intent);
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    protected void updateSearchText() {
        if (rangeText != null) {
            String str;
            if (range == 100){
                str = "Shows " + container.size() + " events in";
            } else
                str = "Shows " + container.size() + " events within " + range + " km of";
            rangeText.setText(str);
        }
    }


    private void markAsGoing(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.coming.setEnabled(false);

        System.out.println(container.get(position).getEventId());
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", container.get(position).getEventId());

        MainActivity.mFunctions
                .getHttpsCallable("going")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    holder.coming.setEnabled(true);
                    return "";
                });
    }

    private void markAsInterested(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.interested.setEnabled(false);

        System.out.println(container.get(position).getEventId());
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", container.get(position).getEventId());

        MainActivity.mFunctions
                .getHttpsCallable("interested")
                .call(data)
                .continueWith(task -> {

                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    holder.interested.setEnabled(true);
                    return "";
                });
    }

    private void showWhoIsGoing(RecyclerViewAdapter.ViewHolder holder, int position) {
        MembersList membersList = new MembersList(activity, container.get(position).getEventId(), "going");
        membersList.show();
    }

    private void showWhoIsInterested(RecyclerViewAdapter.ViewHolder holder, int position) {
        MembersList membersList = new MembersList(activity, container.get(position).getEventId(), "interested");
        membersList.show();
    }



    public void init(View root) {
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();
        progressBar = root.findViewById(R.id.progressBar);
        swipe_refresh = root.findViewById(R.id.swipe_refresh);
        progressBar.setVisibility(View.INVISIBLE);
    }
}
