package com.example.socialbike;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private PreferredLocation preferredLocation;
    Updater.IUpdate update = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preferredLocation = new PreferredLocation(getActivity(), position);
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

        preferredLocation.initPreferredLocation();

        if (root == null) {
            root = inflater.inflate(R.layout.fragment_events, container, false);
            eventsManager = new EventsManager(getActivity(), getContext(), update);
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

        cityText.setOnClickListener(view -> openCitiesAutoComplete());


        setListeners(root);

        eventsManager.showProgressbar();
        getEvents();
    }




    private void getEvents() {
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

    }

    private void openLoginActivity() {
        Intent intent = new Intent(getContext(), LogInActivity.class);
        startActivity(intent);
    }


    private void updateCityTextView() {
      //  String location = position.getCity();

     //   cityText.setText(HtmlCompat.fromHtml
       //         ("<u><b>" + location + "</b></u>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        preferredLocation.setLocationText(cityText);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == eventsManager.NEW_EVENT_CODE) {
            if (resultCode == RESULT_OK) {
                eventsManager.changeTypeOfSearch(eventsManager.MOST_RECENT_CODE);
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

                position.setLatLng(place.getLatLng());// = new Position(place.getLatLng(), place.getName(), country);
                position.setCity(place.getName());
                position.setCountry(country);
                preferredLocation.savePosition();
                updateCityTextView();
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