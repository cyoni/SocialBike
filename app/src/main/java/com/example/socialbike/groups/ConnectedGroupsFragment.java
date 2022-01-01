package com.example.socialbike.groups;

import static android.app.Activity.RESULT_OK;

import static com.example.socialbike.activities.MainActivity.groupManager;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.socialbike.utilities.Constants;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.PreferredLocation;
import com.example.socialbike.R;
import com.example.socialbike.recyclerview.RecyclerViewAdapter2;
import com.example.socialbike.utilities.Updater;
import com.example.socialbike.groups.group.GroupActivity;
import com.example.socialbike.utilities.Utils;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ConnectedGroupsFragment extends Fragment implements RecyclerViewAdapter2.ItemClickListener, Updater.IUpdate {

    private final GroupContainer groupContainer;


    private ProgressBar progressBar;
    private View root;
    private SwipeRefreshLayout swipeLayout;
    private final int DIVIDER_LAYOUT = 0;
    private final int EVENT_LAYOUT = 1;
    private PreferredLocation preferredLocation;
    private String location = "xxx";
    private Position position = new Position();
    private ArrayList<Group> container = new ArrayList<>();
    public RecyclerView recyclerView;
    public RecyclerViewAdapter2 recyclerViewAdapter;

    public ConnectedGroupsFragment(GroupContainer groupContainer) {
        this.groupContainer = groupContainer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preferredLocation = new PreferredLocation(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {

            root = inflater.inflate(R.layout.fragment_group, container, false);
            progressBar = root.findViewById(R.id.progressBar);
            swipeLayout = root.findViewById(R.id.swipe_refresh);
            progressBar.setVisibility(View.VISIBLE);
            preferredLocation.initPreferredLocation(position);

            setSwipeLayout();

            recyclerView = root.findViewById(R.id.recyclerview);

            recyclerViewAdapter = new RecyclerViewAdapter2(getContext(), R.layout.item_group, this.container);
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerViewAdapter.setClassReference(this);

            locallyGetGroups();

        }
        return root;
    }

    private void locallyGetGroups() {
            new CountDownTimer(3000, 500) {
                public void onTick(long millisUntilFinished) {
                    System.out.println("Waiting for main activity to finish downloading groups...");
                    if (!MainActivity.IsGettingMyConnectedGroups) {
                        refresh();
                        cancel();
                    }
                }

                public void onFinish() {
                    System.out.println("Ticker stopped.");
                }
            }.start();
    }

    private void refresh() {
        container.clear();
        System.out.println("size: " +  groupManager.MyConnectedGroups.size());
        for (Map.Entry<String, Group> group : groupManager.MyConnectedGroups.entrySet()) {
            container.add(group.getValue());
        }
        onFinishedUpdating();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!MainActivity.IsGettingMyConnectedGroups) {
            System.out.println("refresh list");
            refresh();
        }
    }

    private void getGroups() {
        groupManager.getMyConnectedGroups().continueWith(task -> {
            groupManager.parseGroups(String.valueOf(task.getResult().getData()));
            MainActivity.updateConnectedGroups(container);
            onFinishedUpdating();
            return null;
        });
    }

/*    public void getConnectedGroups(){
        container.clear();

        Map<String, Object> data = new HashMap<>();
        Utils.PostData(EMethods.GetMyGroups, data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    groupManager.parseGroups(response);
                    return null;
                });
    }*/



    private void sortContainer() {
        ArrayList<Group> tmpContainer = new ArrayList<>();
        for (int i = 0; i < container.size(); i++) {
            Group group = container.get(i);
            if (!group.getIsMember()) {
                tmpContainer.add(group);
                container.remove(group);
                i--;
            }
        }
        tmpContainer.addAll(container);

        container.clear();
        container.addAll(tmpContainer);
    }


    @Override
    public void onBinding(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecyclerViewAdapter2.ViewHolder _holder = (RecyclerViewAdapter2.ViewHolder) holder;
        Group current = container.get(position);
            _holder.joinButton.setVisibility(View.GONE);

        _holder.layout.setOnClickListener(view -> openGroupActivity(current.getGroupId(), current.getTitle()));
        _holder.title.setText(current.getTitle());
        _holder.description.setText(current.getDescription());
        _holder.memberCount.setText(current.getMemberCount() + " members");
        // enableItemMenu(_holder.menu_button, position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                String address = place.getAddress();
                String country, state = null;
                if (address != null && address.contains(",")) {
                    String[] countryAndState = address.split(",");
                    state = countryAndState[0].trim();
                    country = countryAndState[1].trim();
                } else if (address != null) {
                    country = address.trim();
                } else
                    country = "DEFAULT";

                //  position = new Position(place.getLatLng(), place.getName(), country);
                //  savePosition();

                // getEvents();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



/*
    private void openCitiesAutoComplete() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(getContext());
        startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
    }
*/

/*    private void enableItemMenu(View view, int position) {
        view.setOnClickListener(view1 -> {
            PopupMenu popup = new PopupMenu(getContext(), view1);
            popup.inflate(R.menu.group);

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.leave_group:
                            leaveNow(container.get(position).getGroupId());
                            container.remove(position);
                            recyclerViewAdapter.notifyItemRemoved(position);
                            recyclerViewAdapter.notifyItemRangeChanged(0, container.size());
                            // leaveGroup(view, position);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popup.show();
        });
    }*/

/*    private void joinOrLeaveGroup(RecyclerViewAdapter2.ViewHolder holder, int position) {
        if (holder.joinButton.getText().toString().toLowerCase().equals("join")) {
            //joinGroup(holder, position);
        } else
            //leaveGroup(holder, position);
    }*/

/*    private void leaveGroup(RecyclerViewAdapter2.ViewHolder holder, int position) {
        holder.joinButton.setText("Join");
        String groupId = container.get(position).getGroupId();
        int index = getIndex(groupContainer.groupsThatImInFragment.container, groupId);
        groupContainer.groupsThatImInFragment.container.remove(index);
        groupContainer.groupsThatImInFragment.recyclerViewAdapter.notifyItemRemoved(index);
        leaveNow(groupId);
    }*/



    private int getIndex(ArrayList<Group> container, String groupId) {
        for (int i = 0; i < container.size(); i++)
            if (container.get(i).getGroupId().equals(groupId))
                return i;
        return -1;
    }

    private void openGroupActivity(String groupId, String groupName) {
        Intent intent = new Intent(getContext(), GroupActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

/*    private void joinGroup(RecyclerViewAdapter2.ViewHolder holder, int position) {
        holder.joinButton.setText("Joining");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", container.get(position).getGroupId());

        MainActivity.mFunctions
                .getHttpsCallable("JoinGroup")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    groupContainer.groupsThatImInFragment.container.add(0, container.get(position));
                    groupContainer.groupsThatImInFragment.recyclerViewAdapter.notifyItemRangeChanged(0, groupContainer.groupsThatImInFragment.container.size());
                    holder.joinButton.setText("Joined");
                    return null;
                });
    }*/

    private void setSwipeLayout() {
        swipeLayout.setOnRefreshListener(this::getGroups);

        // Scheme colors for animation
        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }


    @Override
    public void onFinishedUpdating() {
        swipeLayout.setRefreshing(false);
        recyclerViewAdapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
    }


    @Override
    public void onItemClick(@NonNull View holder, int position) {
        System.out.println(position);
    }

    @Override
    public RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return recyclerViewAdapter.getLayoutView(view);
    }

    @Override
    public int getItemViewType(int position) {
        return EVENT_LAYOUT;
    }

}