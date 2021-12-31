package com.example.socialbike.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.socialbike.groups.Group;
import com.example.socialbike.groups.GroupManager;
import com.example.socialbike.utilities.AppDatabase;
import com.example.socialbike.utilities.ConnectedUser;
import com.example.socialbike.fragment.EventsFragment;
import com.example.socialbike.utilities.MyPreferences;
import com.example.socialbike.fragment.ProfileFragment;
import com.example.socialbike.R;
import com.example.socialbike.chat.ChatLobbyFragment;
import com.example.socialbike.chat.ChatManager;
import com.example.socialbike.groups.GroupContainer;
import com.example.socialbike.room_database.Member;
import com.example.socialbike.room_database.MemberDao;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.GeoApiContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MenuAction{

    public static FirebaseAuth mAuth;
    public static DatabaseReference mDatabase;
    public static FirebaseFunctions mFunctions;
    public static ChatManager chatManager;
    public static GeoApiContext geoApiContext;
    public static BottomNavigationView bottomNavigationView;
    public static AppDatabase database;
    public static Map<String, String> membersMap = new HashMap<>();
    public static MemberDao memberDao;
    public static boolean isUserConnected;
    public static StorageReference storageRef;
    private MenuManager menuManager = new MenuManager();
    public static boolean IsGettingMyConnectedGroups;
    private int currentLayout;
    public static GroupManager groupManager = new GroupManager();

    public static void toast(Context context, String msg, boolean isLong) {
        int displayLongMessage = isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(context, msg, displayLongMessage).show();
    }

    public MainActivity() {
        chatManager = new ChatManager();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setGeoContext();
        setContentView(R.layout.activity_main);
        setFirebase();
        setIsUserConnected();

        loadUser();
        startListeningBottomMenu();

        currentLayout = R.layout.fragment_events;
        changeFragment(EventsFragment.getInstance());

        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "appDatabase").allowMainThreadQueries().build();

        if (isUserConnected){
            setupChatMembers();
            setupMyConnectedGroups();
            startChat();
        }

        initiatePlaces();

        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void updateConnectedGroups(List<Group> container) {
        groupManager.MyConnectedGroups.clear();
        for (Group group : container){
            groupManager.MyConnectedGroups.put(group.getGroupId(), group);
        }
    }

    private void setupMyConnectedGroups() {
        // connect to server and get groups that the user is in
        IsGettingMyConnectedGroups = true;
        ArrayList<Group> container = new ArrayList<>();

        groupManager.getMyConnectedGroups().continueWith(task -> {
            String response = String.valueOf(task.getResult().getData());
            System.out.println("response:" + response);
            List<Group> groups = groupManager.parseGroups(response);
            updateConnectedGroups(groups);
            IsGettingMyConnectedGroups = false;
            return null;
        });

    }

    private void setupChatMembers() {
        memberDao = MainActivity.database.memberDao();
        List<Member> members = memberDao.getAllMembers();
        for (Member member : members)
             //memberDao.delete(member);
            membersMap.put(member.publicKey, member.name);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initiatePlaces() {
        Places.initialize(getApplicationContext(), "AIzaSyBNXcAnL0GPcywUubwmo_nDRzFeEyTAHMw");
    }

    private void setGeoContext() {
        geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyD86dWwLyv1w2TwmseD04jRDvo7L8rVAxo")
                .build();
    }

    public static void startChat() {
        chatManager.listenForNewMessages();
    }

    public static void stopChat(){
        chatManager.endChat();
    }

    private void loadUser() {
        if (isUserConnected) {
            ConnectedUser.setPublicKey(MyPreferences.getUserPublicKey(this));
            ConnectedUser.setNickname(MyPreferences.getNicknameFromDevice(this));
        }
    }

    private void setFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mFunctions = FirebaseFunctions.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        //mFunctions.useEmulator("127.0.0.1", 5001);

    }

/*    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }*/

    public void setIsUserConnected() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        isUserConnected = account != null;
    }


    @SuppressLint("NonConstantResourceId")
    private void startListeningBottomMenu() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
       // bottomNavigationView.setBackgroundColor(Color.parseColor("#ffffff"));
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.events:

                    changeFragment(EventsFragment.getInstance());
                    currentLayout = R.layout.fragment_events;
                    BadgeDrawable xx = bottomNavigationView.getOrCreateBadge(2);
                    boolean a = xx.hasNumber();
                    xx.setVisible(true);
                    xx.setNumber(3);
                    break;
                case R.id.groups:
                    changeFragment(GroupContainer.getInstance());
                    currentLayout = R.layout.fragment_group;
                    break;
                case R.id.chat:
                    changeFragment(ChatLobbyFragment.getInstance());
                    currentLayout = R.layout.fragment_chat_lobby;
                    break;
                case R.id.profile:
                    changeFragment(ProfileFragment.getInstance());
                    currentLayout = R.layout.fragment_profile;
                    break;
            }
            return true;
        });



    }

    private void changeFragment(Object fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.contentFragment, (Fragment) fragment);
        transaction.commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuManager.setMenu(menu, currentLayout);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        action(item);
        return false;
    }

/*
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                login();
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

    public void login() {
        startActivity(new Intent(this, LogInActivity.class));
    }

    public void myAccount() {
        startActivity(new Intent(this, MyAccountActivity.class));
    }

    @Override
    public void action(MenuItem item) {
        switch (item.getItemId()){
            case MenuManager.LOGIN_SIGN_UP:
                login();
                break;
            case MenuManager.MY_ACCOUNT:
                myAccount();
                break;
                case MenuManager.RemoveChats:
                    chatManager.removeAllChats();
                    break;
        }
    }
}

