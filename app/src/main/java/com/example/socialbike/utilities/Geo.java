package com.example.socialbike.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.maps.model.AddressComponentType;

import java.util.Arrays;
import java.util.List;

public class Geo {

    public static void startAutoComplete(Activity activity, Fragment fragment, TypeFilter filter) {
        Context context = activity != null ? activity : fragment.getContext();

        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(filter).build(context);

        if (activity != null)
            activity.startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
        else
            fragment.startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
    }

    public static Position getPosition(Intent data) {
        Place place = Autocomplete.getPlaceFromIntent(data);
        Position position = new Position();
        position.setAddress(place.getAddress());
        position.setLatLng(place.getLatLng());

        AddressComponents addressComponents = place.getAddressComponents();
        for (AddressComponent addressComponent : addressComponents.asList()){

             if (addressComponent.getTypes().contains("locality") || addressComponent.getTypes().contains("_3")){
                 position.setCity(addressComponent.getName());
             } else if (addressComponent.getTypes().contains("country")){
                 position.setCountry(addressComponent.getName());
             }

        }
        return position;

    }
}
