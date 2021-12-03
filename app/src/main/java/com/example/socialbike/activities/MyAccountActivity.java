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

import com.example.socialbike.Enums.EnumGeneral;
import com.example.socialbike.PreferredLocationManager;
import com.example.socialbike.R;
import com.example.socialbike.utilities.ConnectedUser;
import com.example.socialbike.utilities.Consts;
import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.utilities.ImageManager;
import com.example.socialbike.utilities.MyPreferences;
import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.Utils;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class MyAccountActivity extends AppCompatActivity implements MenuAction {

    MenuManager menuManager = new MenuManager();
    ImageManager imageManager;
    ImageView profilePicture;
    Button saveButton;
    PreferredLocationManager preferredLocationManager;
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
        preferredLocationManager = new PreferredLocationManager(this);
        preferredLocationManager.init();
        preferredLocationManager.initLocation();

        profilePicture = findViewById(R.id.profile_image);

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> save());

        TextView name = findViewById(R.id.name);
        name.setText(ConnectedUser.getName());
        initPicture();

        age = findViewById(R.id.age);

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
        Utils.PostData(EMethods.updateProfile, profileObject).continueWithTask(task -> {
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
        data.put("gender", EnumGeneral.gender.Male);
        data.put("age", age.getText().toString());
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
        Utils.removePreference(this, MyPreferences.USER_FOLDER, "nickname" );
        Utils.removePreference(this, MyPreferences.USER_FOLDER, "user_public_key" );

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
