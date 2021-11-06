package com.example.socialbike.activities;

import static com.example.socialbike.utilities.ImageManager.SELECT_PICTURE_CODE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.socialbike.utilities.ConnectedUser;
import com.example.socialbike.utilities.Consts;
import com.example.socialbike.utilities.DateUtils;
import com.example.socialbike.events.Event;
import com.example.socialbike.utilities.ImageManager;
import com.example.socialbike.utilities.Maps;
import com.example.socialbike.post.MembersList;
import com.example.socialbike.R;
import com.example.socialbike.utilities.Utils;
import com.example.socialbike.events.EventDetails;
import com.example.socialbike.groups.IPageAdapter;
import com.example.socialbike.groups.SectionsPagerAdapter;
import com.example.socialbike.groups.TabManager;
import com.example.socialbike.groups.group.GroupPostsFragment;
import com.example.socialbike.utilities.pictureSheetDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class EventActivity extends AppCompatActivity implements IPageAdapter, pictureSheetDialog.BottomSheetListener {

    String[] tabTitles = {"Details", "Discussion"};
    public TabLayout tabLayout;
    EventDetails eventDetails;
    GroupPostsFragment privateGroupFragment;
    private Event event;
    Button save, interested, going;
    LinearLayout interestedLayOut, goingLayout;
    TextView interested_count, going_count, duration;
    private ImageManager imageManager;
    ImageView headerPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ViewPager viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);
        duration = findViewById(R.id.duration);

        imageManager = new ImageManager(this);

        headerPicture = findViewById(R.id.header_picture);

        Intent intent = getIntent();
        event = (Event) intent.getSerializableExtra("event");

        if (event.getPublicKey().equals(ConnectedUser.getPublicKey()))
            headerPicture.setOnClickListener(view -> {
                BitmapDrawable drawable = (BitmapDrawable) headerPicture.getDrawable();
                if (drawable == null){
                    imageManager.loadPictureFromGallery(this);
                }
                else
                    openSheet();
            });

        TabManager tabManager = new TabManager(viewPager, tabLayout, tabTitles);
        tabManager.init();
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(sectionsPagerAdapter);

        privateGroupFragment = new GroupPostsFragment(event.getGroupId(), event.getEventId());
        eventDetails = new EventDetails(event.getDetails());

        setAllFields();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SELECT_PICTURE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Bitmap compressImage = imageManager.compressImage(bitmap);
                Bitmap copy = imageManager.compressImage(bitmap);
                Bitmap copy2 = imageManager.compressImage(bitmap);

                imageManager.setImage(compressImage, headerPicture);

                StorageReference ref = getPath().child("header");;

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.setMessage("Uploading image");
                progressDialog.show();

                imageManager.uploadImage(copy, ref).addOnSuccessListener(x -> {
                    progressDialog.dismiss();
                    imageManager.removePictureLocally(this, "event_picture_headers", event.getEventId());
                    imageManager.locallySavePicture(copy2, "event_picture_headers", event.getEventId());
                    MainActivity.toast(this, "Success!", true);
                } ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private StorageReference getPath() {
        StorageReference ref;
        if (event.getGroupId() == null) {
            ref = MainActivity.storageRef.
                    child("events").
                    child(event.getEventId());
        } else
            ref = MainActivity.storageRef.child("groups").
                    child(event.getGroupId()).child("events").
                    child(event.getEventId());
        return ref;
    }

    private void openSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.activity_profile_bottom_sheet);

        Button button2 = bottomSheetDialog.findViewById(R.id.picture_locally);
        Button button3 = bottomSheetDialog.findViewById(R.id.button_remove_picture);

        ImageManager imageManager = new ImageManager(this);
        button2.setOnClickListener(v -> {
            imageManager.loadPictureFromGallery(this);
            bottomSheetDialog.dismiss();
        });
        button3.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            imageManager.removePictureLocally(this, "event_picture_headers", event.getEventId());
            imageManager.removePictureRemotely(getPath().child("header"), getPath().getPath());
            resetHeaderPicture();
        });


        bottomSheetDialog.show();
    }

    private void resetHeaderPicture() {
        headerPicture.setImageBitmap(null);
    }

    private void setAllFields() {
        TextView date_and_time = findViewById(R.id.date_and_time);
        String start = DateUtils.convertMiliToDateTime(event.getStart(), Consts.FULL_DATE_TIME);
        date_and_time.setText(start);

        long diff = event.getEnd() - event.getStart();
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours >= 1 && hours < 24)
            duration.setText("Duration: " + hours + " hrs");
        else if (hours < 0.5){
            duration.setVisibility(View.GONE);
        } else if (hours >= 24 && hours <= 24*7){
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            duration.setText("Duration: " + days + " days");
        }
        else if (hours >= 0.5){
            long mins = TimeUnit.MILLISECONDS.toMinutes(diff);
            duration.setText("Duration: " + mins + " mins");
        }

        TextView location = findViewById(R.id.location);
        location.setText(event.getAddress());
        location.setOnClickListener(view -> openMap());
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.title);
        collapsingToolbarLayout.setTitle(event.getTitle());

        save = findViewById(R.id.save_button);
        interested = findViewById(R.id.interested_button);
        going = findViewById(R.id.going_button);

        interested_count = findViewById(R.id.interested_count);
        going_count = findViewById(R.id.going_count);

        goingLayout = findViewById(R.id.going_layout);
        interestedLayOut = findViewById(R.id.interested_layout);

        goingLayout.setOnClickListener(view -> showWhoIsGoing());
        interestedLayOut.setOnClickListener(view -> showWhoIsInterested());

        save.setOnClickListener(view -> saveEvent());
        interested.setOnClickListener(view -> interested());
        going.setOnClickListener(view -> go());

        setPressed(going, event.getIsGoing());
        setPressed(interested, event.getIsInterested());
        setPressed(save, getIsEventSavedInLocal());

        going_count.setText(String.valueOf(Math.max(0, event.getNumParticipants())));
        interested_count.setText(String.valueOf(Math.max(0, event.getNumInterestedMembers())));

        setHeaderPictureIfExists();

    }

    private void setHeaderPictureIfExists() {
        if (event.getHasHeaderPicture() && imageManager.doesPictureExistLocally("event_picture_headers", event.getEventId())){
            imageManager.setImage(imageManager.loadPictureLocally("event_picture_headers", event.getEventId()), headerPicture);
        }
    }

    private boolean getIsEventSavedInLocal() {
        Map<String, ?> map = Utils.getAllPreferences(this, "saved_events");
        Set<String> keys = map.keySet();
        return (keys.contains(event.getEventId()));
    }

    private void openMap() {
        Maps.openMap(this, event.getPosition(), true);
    }

    private void showWhoIsGoing() {
        if (!going_count.getText().toString().equals("0")) {
            MembersList membersList = new MembersList(this, event.getGroupId(), event.getEventId(), "going");
            membersList.show();
        }
    }

    private void showWhoIsInterested() {
        if (!interested_count.getText().toString().equals("0")) {
            MembersList membersList = new MembersList(this, event.getGroupId(), event.getEventId(), "interested");
            membersList.show();
        }
    }

    private void interested() {
        int inc = !event.getIsInterested() ? 1 : -1;
        event.setNumInterestedMembers(event.getNumInterestedMembers() + inc);
        event.setIsInterested(!event.getIsInterested());

        interested_count.setText(String.valueOf(event.getNumInterestedMembers()));
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        data.put("action", event.getIsInterested());

        setPressed(interested, event.getIsInterested());

        MainActivity.mFunctions
                .getHttpsCallable("interested")
                .call(data)
                .continueWith(task -> {

                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    return "";
                });
    }

    private void setPressed(Button button, boolean state) {
        if (state)
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.button_pressed));
        else
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
    }

    private void go() {
        int inc = !event.getIsGoing() ? 1 : -1;
        event.setNumParticipants(event.getNumParticipants() + inc);
        event.setIsGoing(!event.getIsGoing());
        setPressed(going, event.getIsGoing());
        going_count.setText(String.valueOf(event.getNumParticipants()));

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        MainActivity.mFunctions
                .getHttpsCallable("going")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    return "";
                });
    }

    private void saveEvent() {
        boolean isSaved = getIsEventSavedInLocal();
        if (isSaved)
            Utils.removePreference(this, "saved_events", event.getEventId());
        else
             Utils.savePreference(this,"saved_events", event.getEventId(), event.getGroupId() + ",");
        setPressed(save, !isSaved);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return eventDetails;
            case 1:
                return privateGroupFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public void onButtonClicked(String text) {
        System.out.println(text);
    }
}