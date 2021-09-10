package com.example.socialbike;

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
        implements SeekBar.OnSeekBarChangeListener, Updater.IUpdate {

    static EventsFragment eventsFragment = null;
    private SeekBar seekBar;
    private TextView cityText, no_events_text;
    private Position position;
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
            eventsManager = new EventsManager(getActivity(), getContext(), update);
            this.container = eventsManager.container;
            initiateScreen(root);
        }
        return root;
    }

    private void initiateScreen(View root) {
        eventsManager.init(root);
        eventsManager.rangeText = root.findViewById(R.id.rangeText);
        cityText = root.findViewById(R.id.city);
        no_events_text = root.findViewById(R.id.no_events_text);
        no_events_text.setVisibility(View.GONE);
        position = new Position(new LatLng(32.074022, 34.775507), "Tel Aviv", "Israel", "Tel Aviv District");

        setCityTextView("Tel Aviv");
        setSeekBar(root);
        setListeners(root);

        eventsManager.showProgressbar();
        getEvents();
    }

    private void getEvents() {
        eventsManager.showProgressbar();
        no_events_text.setVisibility(View.GONE);
        container.clear();

        Map<String, Object> data = new HashMap<>();
        data.put("range", eventsManager.range);
        data.put("country", position.getCountry());
        data.put("state", position.getState());
        data.put("lat", position.getLatLng().latitude);
        data.put("lng", position.getLatLng().longitude);
       // eventsManager.getEvents(data);
    }

    private void setListeners(View root) {

        Button addEvent = root.findViewById(R.id.add_new_event_button);

        addEvent.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AddNewEventActivity.class);
            startActivityForResult(intent, eventsManager.NEW_EVENT_CODE);
        });

        Button sortButton = root.findViewById(R.id.sort_button);
        sortButton.setOnClickListener(view -> {
            openLoginActivity();

          /*  if (sortButton.getText().equals("Treading")) {
                eventsManager.changeTypeOfSearch(eventsManager.MOST_RECENT_CODE);
                sortButton.setText("Recent Activity");
            } else {
                eventsManager.changeTypeOfSearch(eventsManager.TRADING_CODE);
                sortButton.setText("Treading");
            }*/
            getEvents();
        });

    }

    private void openLoginActivity() {
        Intent intent = new Intent(getContext(), LogInActivity.class);
        startActivity(intent);
    }


    private void setCityTextView(String city) {
        cityText.setOnClickListener(view -> openCitiesAutoComplete());
        cityText.setText(HtmlCompat.fromHtml
                ("<u><b>" + city + "</b></u>", HtmlCompat.FROM_HTML_MODE_LEGACY));
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int stepSize = 10;
        progress = (seekBar.getProgress() / stepSize) * stepSize;
        seekBar.setProgress(progress);
        System.out.println(progress);
        eventsManager.range = seekBar.getProgress();
        eventsManager.updateSearchText();
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        getEvents();
    }


    @Override
    public void onFinishedUpdating() {
        eventsManager.recyclerViewAdapter.notifyItemRangeInserted(0, container.size());
        eventsManager.updateSearchText();
        eventsManager.hideProgressbar();
    }
}