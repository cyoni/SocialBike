package com.example.socialbike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.socialbike.chat.ChatLobbyFragment;
import com.example.socialbike.chat.ChatManager;
import com.example.socialbike.chat.ContainerForChat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static FirebaseAuth mAuth;
    public static DatabaseReference mDatabase;
    public static FirebaseFunctions mFunctions;
    private BottomNavigationView bottomNavigationView;
    public static ChatManager chatManager;
    public static GeoApiContext geoApiContext;

    public static void toast(Context context, String msg, int isLong) {
        Toast.makeText(context, msg, isLong).show();
    }

    public MainActivity(){
        chatManager = new ChatManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setGeoContext();
        setContentView(R.layout.activity_main);
        setFirebase();
        loadUser();
        startListeningBottomMenu();
        changeFragment(HomeFragment.getInstance());
        startChat();
        initiatePlaces();

       // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void initiatePlaces() {
        Places.initialize(getApplicationContext(), "AIzaSyBNXcAnL0GPcywUubwmo_nDRzFeEyTAHMw");
    }

    private void setGeoContext() {
        geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyD86dWwLyv1w2TwmseD04jRDvo7L8rVAxo")
                .build();
    }

    private void startChat() {
        chatManager.listenForNewMessages();
    }

    private void loadUser() {
        if (checkIfUserConnected()){
            User.setPublicKey(MyPreferences.getUserPublicKey(this));
            User.setNickname(MyPreferences.getNicknameFromDevice(this));
        }
    }

    private void setFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFunctions = FirebaseFunctions.getInstance();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    private boolean checkIfUserConnected(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        return account != null;
    }


    private void startListeningBottomMenu() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackgroundColor(Color.parseColor("#ffffff"));
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.home:
                        changeFragment(HomeFragment.getInstance());
                        break;
                    case R.id.events:
                        changeFragment(EventsFragment.getInstance());
                        break;
                    case R.id.chat:
                        changeFragment(ContainerForChat.getInstance());
                        break;
                    case R.id.profile:
                        changeFragment(ProfileFragment.getInstance());
                        break;
                }

             /*   if (item.getItemId() == R.id.home){
                    changeFragment(HomeFragment.getInstance());
                }  else if (item.getItemId() == R.id.events)
                    changeFragment(EventsFragment.getInstance());
                else if (item.getItemId() == R.id.chat)
                    changeFragment(ContainerForChat.getInstance());
                else if (item.getItemId() == R.id.profile)
                    changeFragment(ProfileFragment.getInstance());*/

                return true;
            }
        });
    }

    private void changeFragment(Object fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.contentFragment, (Fragment) fragment);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.login:
                login();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void login() {
        startActivity(new Intent(this, LogIn.class));
    }
}