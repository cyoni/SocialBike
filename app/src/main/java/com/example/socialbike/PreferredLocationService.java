package com.example.socialbike;

import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.Position;
import com.google.android.gms.maps.model.LatLng;

interface IPreferredLocation{

    void init();
    void savePreferredLocation(Position position);
    void savePrivateLocation(Position position);
    Position getPreferredPosition();
    Position getPrivatePosition();
}

public class PreferredLocationService implements IPreferredLocation{


    // responsible for private location and preferred location in search and groups

    public Position preferredLocation = new Position();
    public Position privateLocation = new Position();
    private String PublicFolder = "public_data";
    private String PrivateFolder = "private_data";

    private void saveLocation(String folder, Position position) {
        MainActivity.utils.savePreference(folder,"lat", String.valueOf(position.getLatLng().latitude));
        MainActivity.utils.savePreference(folder,"city", position.getCity());
        MainActivity.utils.savePreference(folder,"lng", String.valueOf(position.getLatLng().longitude));
        MainActivity.utils.savePreference(folder,"country", position.getCountry());
        MainActivity.utils.savePreference(folder,"address", position.getAddress());
    }


    public void init() {
        preferredLocation = getPosition(PublicFolder);
        privateLocation = getPosition(PrivateFolder);

        if (preferredLocation == null){
            String userCountry = MainActivity.utils.getUserCountry();
            if (userCountry != null) {
                String preferredCity = userCountry.toUpperCase();
                this.preferredLocation = MainActivity.utils.getLatLngOfString(preferredCity + " country");
                saveLocation(PublicFolder, preferredLocation);
            }
        }
    }

    private Position getPosition(String folder) {
        String lat = MainActivity.utils.getPreference(folder, "lat");
        String lng = MainActivity.utils.getPreference(folder, "lng");
        String city = MainActivity.utils.getPreference(folder, "city");
        String country = MainActivity.utils.getPreference(folder, "country");
        String address = MainActivity.utils.getPreference(folder, "address");

        if (lat == null || lng == null){
            return null;
        }

        return new Position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)), country, city, address);
    }

    @Override
    public void savePreferredLocation(Position position) {
        preferredLocation = position;
        saveLocation(PublicFolder, preferredLocation);
    }

    @Override
    public void savePrivateLocation(Position position) {
        privateLocation = position;
        saveLocation(PrivateFolder, privateLocation);
    }

    @Override
    public Position getPreferredPosition() {
        return preferredLocation;
    }

    @Override
    public Position getPrivatePosition() {
        return privateLocation;
    }
}
