package com.example.socialbike;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MembersList extends Dialog implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    private final String eventId;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private final ArrayList<String> container = new ArrayList<>();
    private ProgressBar progressBar;
    private final Updater updater;
    private final String keyName;

    public MembersList(@NonNull Activity activity, String eventId, String functionName) {
        super(activity);

        this.keyName = functionName;
        this.eventId = eventId;
        updater = new Updater(this, container, recyclerViewAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_member_list);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        initAdapter();
        getMembers();
    }

    private void getMembers() {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("keyName", keyName);

        MainActivity.mFunctions
                .getHttpsCallable("getMemberList")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    parseStr(response);
                    updater.referenceClass.onFinishedTakingNewMessages();
                    return "";
                }).addOnFailureListener(e -> {
            updater.referenceClass.onFinishedTakingNewMessages();
            System.out.println("ERROR");
        });
    }

    private void parseStr(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            JSONArray jsonArray = obj.getJSONArray("members");

            for (int i = 0; i < jsonArray.length(); i++) {
                container.add((String) jsonArray.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_member, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.name.setText(container.get(position));
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    @Override
    public void onFinishedTakingNewMessages() {
        progressBar.setVisibility(View.GONE);
    }
}
