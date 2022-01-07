package com.example.socialbike.activities;

import static com.example.socialbike.utilities.Constants.ADDRESS_FROM_MAPS_CODE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.socialbike.PreferredLocationService;
import com.example.socialbike.R;
import com.example.socialbike.utilities.ConnectedUser;
import com.example.socialbike.utilities.Consts;
import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.utilities.Geo;
import com.example.socialbike.utilities.ImageManager;
import com.example.socialbike.utilities.MyPreferences;
import com.example.socialbike.utilities.Position;
import com.google.android.libraries.places.api.model.TypeFilter;

import java.util.HashMap;
import java.util.Map;

public class MyAccountActivity extends AppCompatActivity implements MenuAction {

    MenuManager menuManager = new MenuManager();
    ImageManager imageManager;
    ImageView profilePicture;
    Button saveButton;
    private Position position;
    EditText locationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Account");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        imageManager = new ImageManager(this);

        profilePicture = findViewById(R.id.profile_image);

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> save());

        TextView name = findViewById(R.id.name);
        name.setText(ConnectedUser.getName());
        initPicture();

        locationText = findViewById(R.id.preferredLocation);
        locationText.setOnClickListener(view -> Geo.startAutoComplete(this, null, TypeFilter.CITIES));

        position = MainActivity.preferredLocationService.getPrivatePosition();
        if (position != null){
            locationText.setText(position.getAddress());
        }
    }

    private void save() {
        if (!saveButton.getText().toString().equals("Saving")) {
            saveButton.setText("Saving");
            submitForm();
        }
    }

    private void submitForm() {
        Map<String, Object> profileObject = buildRequestObject();
        MainActivity.utils.PostData(EMethods.updateProfile, profileObject).continueWithTask(task -> {
            MainActivity.preferredLocationService.savePrivateLocation(position);
            MainActivity.preferredLocationService.savePreferredLocation(position);
            MainActivity.toast(this, "Saved", false);
            finish();
            return null;
        });

    }

    private Map<String, Object> buildRequestObject() {
        Map<String, Object> data = new HashMap<>();
        data.put("country", position.getCountry());
        data.put("city", position.getCity());
        data.put("lat", position.getLatLng().latitude);
        data.put("lng", position.getLatLng().longitude);
        return data;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADDRESS_FROM_MAPS_CODE) {
            if (resultCode == RESULT_OK) {
                position = Geo.getPosition(data);
                locationText.setText(position.getAddress());
            }
        }

    }

    private void initPicture() {
        if (imageManager.doesPictureExistLocally(Consts.Profile_Picture, ConnectedUser.getPublicKey())){
            Bitmap bitmap = imageManager.loadPictureLocally(Consts.Profile_Picture, ConnectedUser.getPublicKey());
            imageManager.setImage(bitmap, profilePicture);
        } else{
            // does user have picture online?
        }
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }*/

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuManager.setMenu(menu, R.layout.activity_my_account);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        action(item);

        return false;
    }

    private void logout() {
        // delete all local database

        MainActivity.stopChat();
        MainActivity.utils.removePreference(MyPreferences.USER_FOLDER, "nickname" );
        MainActivity.utils.removePreference(MyPreferences.USER_FOLDER, "user_public_key" );

        MainActivity.mAuth.signOut();
        ConnectedUser.setPublicKey("-");

        MainActivity.toast(getApplicationContext(), "You have been logged out successfully.", true);
        finish();
    }

    @Override
    public void action(MenuItem item) {
        if (item.getItemId() == menuManager.LOG_OUT){
            logout();
        }
    }
}
