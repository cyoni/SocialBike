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


public class EventsFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener{

    static EventsFragment eventsFragment = null;
    private RecyclerView recyclerView;
    private final ArrayList<Event> container;
    private RecyclerViewAdapter recyclerViewAdapter;
    private Updater updater;
    private boolean loadMore = true;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;


    public EventsFragment() {
        container = new ArrayList<>();
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_events, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static EventsFragment getInstance(){
        if (eventsFragment == null){
            eventsFragment = new EventsFragment();
        }
        return eventsFragment;
    }

    public void getPosts(){

        System.out.println("getting Events...");

        Map<String, Object> data = new HashMap<>();
        data.put("xxx", "message");

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

    public void parseMessages(String fresh_msgs){

        try {
            JSONObject obj = new JSONObject(fresh_msgs);
            JSONArray messages_array = obj.getJSONArray("events");

            for (int i = 0; i < messages_array.length(); i++) {

                String userPublicKey = messages_array.getJSONObject(i).getString("userPublicKey");
                String message = messages_array.getJSONObject(i).getString("eventContent");
                String name = messages_array.getJSONObject(i).getString("name");
                String dateOfEvent = messages_array.getJSONObject(i).getString("eventDate");
                String timeOfEvent = messages_array.getJSONObject(i).getString("eventTime");
                String createdEventTime = messages_array.getJSONObject(i).getString("createdEventTime");
                String eventId = messages_array.getJSONObject(i).getString("eventId");
                String amountOfInterestedPeople = messages_array.getJSONObject(i).getString("amountOfInterestedPeople");
                String city = messages_array.getJSONObject(i).getString("eventCity");
                String country = messages_array.getJSONObject(i).getString("eventCountry");

                Event event = new Event(eventId, userPublicKey, name,
                        dateOfEvent, timeOfEvent, createdEventTime,
                        amountOfInterestedPeople, city, country, message);
                updater.add(event);
                updater.update();

                System.out.println("post " + i + " " + message);
            }
        }
        catch(Exception e){
            System.out.println("An error was caught in message fetcher: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_events, container, false);
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();

        updater = new Updater(this.container, recyclerViewAdapter);
        if (loadMore) {
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

            if (sortButton.getText().equals("Most relevant")){
                sortButton.setText("New Activity");
            }
            else{
                sortButton.setText("Most relevant");
            }

        });

        return root;
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
       // holder.amountOfInterestedPeople.setText(container.get(position).getAmountOfInterestedPeople());
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }
}