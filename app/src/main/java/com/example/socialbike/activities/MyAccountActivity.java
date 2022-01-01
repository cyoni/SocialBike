package com.example.socialbike.activities;

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
import com.example.socialbike.utilities.Utils;
import com.google.android.libraries.places.api.model.TypeFilter;

import java.util.HashMap;
import java.util.Map;

public class MyAccountActivity extends AppCompatActivity implements MenuAction {

    MenuManager menuManager = new MenuManager();
    ImageManager imageManager;
    ImageView profilePicture;
    Button saveButton;
    PreferredLocationService preferredLocationManager;
    EditText age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Account");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        imageManager = new ImageManager(this);
        preferredLocationManager = new PreferredLocationService(this);

        preferredLocationManager.initLocation();

        profilePicture = findViewById(R.id.profile_image);

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> save());

        TextView name = findViewById(R.id.name);
        name.setText(ConnectedUser.getName());
        initPicture();

        EditText locationText = findViewById(R.id.preferredLocation);
        locationText.setOnClickListener(view -> Geo.startAutoComplete(this, null, TypeFilter.CITIES));

    }

    private void save() {
        if (!saveButton.getText().toString().equals("Saving...")) {
            saveButton.setText("Saving...");
            submitForm();
        }
    }

    private void submitForm() {
        Map<String, Object> profileObject = buildRequestObject();
        preferredLocationManager.saveLocation();
        MainActivity.utils.PostData(EMethods.updateProfile, profileObject).continueWithTask(task -> {
            MainActivity.toast(this, "Saved", false);
            finish();
            return null;
        });

    }

    private Map<String, Object> buildRequestObject() {
        Map<String, Object> data = new HashMap<>();
        data.put("country", preferredLocationManager.position.getCountry());
        data.put("city", preferredLocationManager.position.getCity());
        data.put("lat", preferredLocationManager.position.getLatLng().latitude);
        data.put("lng", preferredLocationManager.position.getLatLng().longitude);
        return data;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        preferredLocationManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
