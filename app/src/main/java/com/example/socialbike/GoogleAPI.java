package com.example.socialbike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

public class GoogleAPI {

    public void Places(Activity activity, Context context, AutocompleteActivityMode autocompleteActivityMode) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(autocompleteActivityMode, fields).setTypeFilter(TypeFilter.CITIES)
                .build(context);
        activity.startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
    }
}
