package com.example.socialbike;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class EventsFragment extends Fragment
        implements RecyclerViewAdapter.ItemClickListener,
        Updater.IUpdate,
        SeekBar.OnSeekBarChangeListener {

    private final int NEW_EVENT_CODE = 100;
    static EventsFragment eventsFragment = null;
    private RecyclerView recyclerView;
    protected final ArrayList<Event> container = new ArrayList<>();
    private RecyclerViewAdapter recyclerViewAdapter;
    private Updater updater;
    private ProgressBar progressBar;
    private final String MOST_RECENT_CODE = "MOST_RECENT";
    private final String TRADING_CODE = "TRADING";
    private String dataType = MOST_RECENT_CODE;
    private SeekBar seekBar;
    private TextView rangeText, cityText, no_events_text;
    private final EventsCommentsExtension eventsCommentsExtension;
    private int range = 10;
    private Position position;
    private View root;

    public EventsFragment() {
        eventsCommentsExtension = new EventsCommentsExtension(this);
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

    public static EventsFragment getInstance() {
        if (eventsFragment == null) {
            eventsFragment = new EventsFragment();
        }
        return eventsFragment;
    }

    public void getEvents() {

        showProgressbar();
        System.out.println("getting Events...");
        no_events_text.setVisibility(View.GONE);
        container.clear();

        Map<String, Object> data = new HashMap<>();
        data.put("dataType", dataType);
        data.put("range", range);
        data.put("country", position.getCountry());
        data.put("state", position.getState());
        data.put("lat", position.getLatLng().latitude);
        data.put("lng", position.getLatLng().longitude);

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
            no_events_text.setVisibility(View.VISIBLE);
            onFinishedTakingNewMessages();
            return;
        }

        if (container.size() > 0 && data.length() > 0) {
            container.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }

        for (int i = 0; i < data.length(); i++) {

            String userPublicKey = null;
            try {
                userPublicKey = data.getJSONObject(i).getString("userPublicKey");
                String eventDetails = data.getJSONObject(i).getString("eventDetails");
                String name = data.getJSONObject(i).getString("name");
                String dateOfEvent = data.getJSONObject(i).getString("eventDate");
                String timeOfEvent = data.getJSONObject(i).getString("eventTime");
                String createdEventTime = data.getJSONObject(i).getString("createdEventTime");
                String eventId = data.getJSONObject(i).getString("eventId");
                String numOfInterestedMembers = data.getJSONObject(i).getString("numOfInterestedMembers");
                String locationName = data.getJSONObject(i).getString("locationName");
                String locationAddress = data.getJSONObject(i).getString("locationAddress");
                double lat = data.getJSONObject(i).getDouble("lat");
                double lng = data.getJSONObject(i).getDouble("lng");
                int commentsNumber = data.getJSONObject(i).getInt("commentsNumber");

                int numberOfParticipants = 0;
                if (data.getJSONObject(i).has("numberOfParticipants") && data.getJSONObject(i).get("numberOfParticipants") instanceof Integer)
                    numberOfParticipants = data.getJSONObject(i).getInt("numberOfParticipants");

                Position position = new Position(new LatLng(lat, lng), locationName, locationAddress);
                Event event = new Event(
                        eventId, userPublicKey, name,
                        dateOfEvent, timeOfEvent, createdEventTime,
                        numOfInterestedMembers, numberOfParticipants,
                        position, eventDetails, commentsNumber
                );

                updater.add(event);

                System.out.println("event  " + i + " " + eventDetails);
            } catch (JSONException e) {
                System.out.println("An error was caught in message fetcher: " + e.getMessage());
            }
        }
        onFinishedTakingNewMessages();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_events, container, false);
            initiateScreen(root);
        }
        return root;
    }

    private void initiateScreen(View root) {
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();
        progressBar = root.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        rangeText = root.findViewById(R.id.rangeText);
        cityText = root.findViewById(R.id.city);
        no_events_text = root.findViewById(R.id.no_events_text);
        no_events_text.setVisibility(View.GONE);
        position = new Position(new LatLng(32.074022, 34.775507), "Tel Aviv", "Israel", "Tel Aviv District");

        setCityTextView("Tel Aviv");
        setSeekBar(root);

        updater = new Updater(this, this.container, recyclerViewAdapter);
        setListeners(root);

        showProgressbar();
        getEvents();
    }

    private void setListeners(View root) {

        Button addEvent = root.findViewById(R.id.add_new_event_button);


        addEvent.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AddNewEventActivity.class);
            startActivityForResult(intent, NEW_EVENT_CODE);
        });

        Button sortButton = root.findViewById(R.id.sort_button);
        sortButton.setOnClickListener(view -> {

            if (sortButton.getText().equals("Treading")) {
                changeTypeOfSearch(MOST_RECENT_CODE);
                sortButton.setText("Recent Activity");
            } else {
                changeTypeOfSearch(TRADING_CODE);
                sortButton.setText("Treading");
            }

        });

    }

    private void changeTypeOfSearch(String type) {
        dataType = type;
        getEvents();
    }


    private void setCityTextView(String city) {
        cityText.setOnClickListener(view -> openCitiesAutoComplete());
        cityText.setText(HtmlCompat.fromHtml
                ("<u><b>" + city + "</b></u>", HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == NEW_EVENT_CODE) {
            if (resultCode == RESULT_OK) {
                changeTypeOfSearch(MOST_RECENT_CODE);
            }
        } else if (requestCode == Constants.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                String address = place.getAddress();
                String country, state = null;
                if (address != null && address.contains(",")) {
                    String[] countryAndState = address.split(",");
                    state = countryAndState[0].trim();
                    country = countryAndState[1].trim();
                } else if (address != null) {
                    country = address.trim();
                } else
                    country = "DEFAULT";

                position = new Position(place.getLatLng(), place.getName(), country, state);
                System.out.println(position.toString());
                setCityTextView(position.getLocationName());
                getEvents();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openCitiesAutoComplete() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(getContext());
        startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
    }

    private void setSeekBar(View root) {
        seekBar = root.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(10);
        }
    }


    private void showProgressbar() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressbar() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int stepSize = 10;
        progress = (seekBar.getProgress() / stepSize) * stepSize;
        seekBar.setProgress(progress);
        System.out.println(progress);
        updateSearchText();
    }

    private void updateSearchText() {
        range = seekBar.getProgress();
        String str = "Finds " + container.size() + " events within " + range + " km of";
        rangeText.setText(str);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        getEvents();
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.message.setText(container.get(position).getMsg());
        holder.locationName.setText(container.get(position).getPosition().getLocationName());
        holder.time.setText(container.get(position).getTimeOfEvent());
        holder.date.setText(container.get(position).getDateOfEvent());
        holder.name.setText(container.get(position).getName());
        holder.people_going.setText(container.get(position).getNumberOfParticipants() + " people going");
        holder.interested.setOnClickListener(view -> markAsInterested(holder, position));
        holder.coming.setOnClickListener(view -> markAsGoing(holder, position));
        holder.who_is_coming.setOnClickListener(view -> showWhoIsGoing(holder, position));
        holder.who_is_interested.setOnClickListener(view -> showWhoIsInterested(holder, position));
        holder.commentButton.setOnClickListener(view -> eventsCommentsExtension.commentButton(holder, position));
        holder.mapButton.setOnClickListener(view -> openMap(container.get(position).getLatLng()));

        // holder.amountOfInterestedPeople.setText(container.get(position).getAmountOfInterestedPeople());
    }

    private void openMap(LatLng latLng) {
        Intent intent = new Intent(getContext(), Maps.class);
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("lng", latLng.longitude);
        startActivity(intent);
    }

    private void showWhoIsGoing(RecyclerViewAdapter.ViewHolder holder, int position) {
        MembersList membersList = new MembersList(getActivity(), container.get(position).getEventId(), "going");
        membersList.show();
    }

    private void showWhoIsInterested(RecyclerViewAdapter.ViewHolder holder, int position) {
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

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    @Override
    public void onFinishedTakingNewMessages() {
        updateSearchText();
        hideProgressbar();
    }

}