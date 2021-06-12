package com.example.socialbike;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class EventsFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    static EventsFragment eventsFragment = null;
    private RecyclerView recyclerView;
    private final ArrayList<Event> container = new ArrayList<>();
    private RecyclerViewAdapter recyclerViewAdapter;
    private Updater updater;
    private boolean loadMore = true;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private ProgressBar progressBar;
    private final String MOST_RECENT_CODE = "MOST_RECENT";
    private final String TRADING_CODE = "TRADING";
    private String dataType = MOST_RECENT_CODE;


    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_events, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static EventsFragment getInstance() {
        if (eventsFragment == null) {
            eventsFragment = new EventsFragment();
        }
        return eventsFragment;
    }

    public void getPosts() {

        System.out.println("getting Events...");

        Map<String, Object> data = new HashMap<>();
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

    public void parseMessages(String fresh_msgs) {

        try {
            JSONObject obj = new JSONObject(fresh_msgs);
            JSONArray data = obj.getJSONArray("events");

            for (int i = 0; i < data.length(); i++) {

                String userPublicKey = data.getJSONObject(i).getString("userPublicKey");
                String message = data.getJSONObject(i).getString("eventContent");
                String name = data.getJSONObject(i).getString("name");
                String dateOfEvent = data.getJSONObject(i).getString("eventDate");
                String timeOfEvent = data.getJSONObject(i).getString("eventTime");
                String createdEventTime = data.getJSONObject(i).getString("createdEventTime");
                String eventId = data.getJSONObject(i).getString("eventId");
                String amountOfInterestedPeople = data.getJSONObject(i).getString("amountOfInterestedPeople");
                String city = data.getJSONObject(i).getString("eventCity");
                String country = data.getJSONObject(i).getString("eventCountry");

                int numberOfParticipants = 0;
                if (data.getJSONObject(i).has("numberOfParticipants") && data.getJSONObject(i).get("numberOfParticipants") instanceof Integer)
                    numberOfParticipants  = data.getJSONObject(i).getInt("numberOfParticipants");

                Event event = new Event(
                        eventId, userPublicKey, name,
                        dateOfEvent, timeOfEvent, createdEventTime,
                        amountOfInterestedPeople, numberOfParticipants,
                        city, country, message);
                updater.add(event);

                System.out.println("post " + i + " " + message);
            }
            onFinishedTakingNewMessages();
        } catch (Exception e) {
            System.out.println("An error was caught in message fetcher: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_events, container, false);
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();
        progressBar = root.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);



        updater = new Updater(this, this.container, recyclerViewAdapter);
        if (loadMore) {
            showProgressbar();

            getPosts();
            loadMore = false;
        }

        Button addEvent = root.findViewById(R.id.add_new_event_button);
        Button changeRegion = root.findViewById(R.id.search_places);

        changeRegion.setOnClickListener(view -> {
            openSearchWindow();
        });

        addEvent.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AddNewEventActivity.class);
            startActivity(intent);
        });

        Button sortButton = root.findViewById(R.id.sort_button);
        sortButton.setOnClickListener(view -> {

            if (sortButton.getText().equals("Most relevant")) {
                getTradingData();
                sortButton.setText("New Activity");
            } else {
                getRecentData();
                sortButton.setText("Most relevant");
            }

        });

        return root;
    }

    private void showProgressbar() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressbar() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }


    private void getRecentData() {
        showProgressbar();
        dataType = MOST_RECENT_CODE;
    }

    private void getTradingData() {
        showProgressbar();
        dataType = TRADING_CODE;
    }

    private void openSearchWindow() {
        // Initialize the SDK
        Places.initialize(getActivity().getApplicationContext(), "AIzaSyB-ZfB7qwZpFVizXuvYwTSP3NGo0J0ZsDc");

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(getContext());

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(getContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                System.out.println("Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                System.out.println(status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.message.setText(container.get(position).getMsg());
        holder.country.setText(container.get(position).getCountry());
        holder.city.setText(container.get(position).getCity());
        holder.time.setText(container.get(position).getTimeOfEvent());
        holder.date.setText(container.get(position).getDateOfEvent());
        holder.name.setText(container.get(position).getName());
        holder.people_going.setText(container.get(position).getNumberOfParticipants() + " people going");
        holder.interested.setOnClickListener(view -> interested(holder, position));
        holder.coming.setOnClickListener(view -> markAsGoing(holder, position));
        holder.who_is_coming.setOnClickListener(view -> who_is_going(holder, position));
        holder.who_is_interested.setOnClickListener(view -> who_is_interested(holder, position));
        // holder.amountOfInterestedPeople.setText(container.get(position).getAmountOfInterestedPeople());
    }

    private void who_is_going(RecyclerViewAdapter.ViewHolder holder, int position) {
        MembersList membersList = new MembersList(getActivity(), container.get(position).getEventId(), "going");
        membersList.show();
    }

    private void who_is_interested(RecyclerViewAdapter.ViewHolder holder, int position) {
        MembersList membersList = new MembersList(getActivity(), container.get(position).getEventId(), "interested");
        membersList.show();
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

    private void interested(RecyclerViewAdapter.ViewHolder holder, int position) {
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

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    @Override
    public void onFinishedTakingNewMessages() {
        hideProgressbar();
    }
}