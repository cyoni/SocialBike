package com.example.socialbike.fragment;

import
        android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.socialbike.activities.FavoriteEventsActivity;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.events.Event;
import com.example.socialbike.events.EventsManager;
import com.example.socialbike.utilities.Geo;
import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.PreferredLocation;
import com.example.socialbike.R;
import com.example.socialbike.utilities.Updater;
import com.example.socialbike.activities.AddNewEventActivity;
import com.example.socialbike.activities.LogInActivity;
import com.example.socialbike.utilities.Constants;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class EventsFragment extends Fragment
        implements Updater.IUpdate {

    static EventsFragment eventsFragment = null;
    private SeekBar seekBar;
    private TextView cityText, no_events_text;
    private Position position = new Position();
    private View root;
    private EventsManager eventsManager;
    protected ArrayList<Event> container;
    Updater.IUpdate update = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public EventsFragment() {

    }

    public static EventsFragment getInstance() {
        if (eventsFragment == null) {
            eventsFragment = new EventsFragment();
        }
        return eventsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (root == null) {
            root = inflater.inflate(R.layout.fragment_events, container, false);
            eventsManager = new EventsManager(getActivity(), getContext(), update, true);
            this.container = eventsManager.container;
            initiateScreen(root);
            eventsManager.showProgressbar();
            setSwipeLayout();
        }

        updateCityTextView();

        return root;
    }

    private void setSwipeLayout() {
        eventsManager.swipe_refresh.setOnRefreshListener(this::getEvents);

        eventsManager.swipe_refresh.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }

    private void initiateScreen(View root) {
        eventsManager.init(root);
        eventsManager.rangeText = root.findViewById(R.id.rangeText);
        cityText = root.findViewById(R.id.city);
        no_events_text = root.findViewById(R.id.no_events_text);
        no_events_text.setVisibility(View.GONE);
        position = MainActivity.preferredLocationService.getPreferredPosition();
        cityText.setOnClickListener(view -> Geo.startAutoComplete(null, this, TypeFilter.CITIES));

        setListeners(root);

        eventsManager.showProgressbar();
        getEvents();
    }

    private void getEvents() {
        if (position.getLatLng() != null){
            no_events_text.setVisibility(View.GONE);
            container.clear();

            Map<String, Object> data = new HashMap<>();
            data.put("country", position.getCountry());
            data.put("city", position.getCity());
            data.put("lat", position.getLatLng().latitude);
            data.put("lng", position.getLatLng().longitude);
            System.out.println( position.getLatLng().latitude + "," +  position.getLatLng().longitude);
            eventsManager.getEvents(data);
        }
    }

    private void setListeners(View root) {

        Button addEvent = root.findViewById(R.id.add_new_event_button);

        addEvent.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AddNewEventActivity.class);
            startActivityForResult(intent, eventsManager.NEW_EVENT_CODE);
        });

        Button sortButton = root.findViewById(R.id.sort_button);
        sortButton.setOnClickListener(view -> {
            //openLoginActivity();
            if (sortButton.getText().equals("Trending")) {
                eventsManager.changeTypeOfSearch(eventsManager.MOST_RECENT_CODE);
                sortButton.setText("Recent");
            } else {
                eventsManager.changeTypeOfSearch(eventsManager.TRADING_CODE);
                sortButton.setText("Trending");
            }
            eventsManager.showProgressbar();
            getEvents();
        });

        ImageButton favoriteEventsButton = root.findViewById(R.id.favoriteEventsButton);
        favoriteEventsButton.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), FavoriteEventsActivity.class);
            startActivity(intent);
        });
    }

    public void refresh(){
        eventsManager.showProgressbar();
        updateCityTextView();
        getEvents();
    }

    private void updateCityTextView() {
        cityText.setText(HtmlCompat.fromHtml
                ("<u><b>"+ position.getCity() +"</b></u>", HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == eventsManager.NEW_EVENT_CODE) {
            if (resultCode == RESULT_OK) {
                eventsManager.changeTypeOfSearch(eventsManager.MOST_RECENT_CODE);
            }
        } else if (requestCode == Constants.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                position = Geo.getPosition(data);
                MainActivity.preferredLocationService.savePreferredLocation(position);

                updateCityTextView();
                getEvents();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onFinishedUpdating() {
        eventsManager.recyclerViewAdapter.notifyDataSetChanged();
        cityText.setVisibility(View.VISIBLE);
        eventsManager.updateSearchText();
        eventsManager.hideProgressbar();
        if (eventsManager.container.isEmpty())
            no_events_text.setVisibility(View.VISIBLE);
    }
}