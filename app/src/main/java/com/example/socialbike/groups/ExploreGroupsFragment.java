package com.example.socialbike.groups;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import static com.example.socialbike.activities.MainActivity.groupManager;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.socialbike.recyclerview.RecyclerViewAdapter;
import com.example.socialbike.utilities.Constants;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.Geo;
import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.PreferredLocation;
import com.example.socialbike.R;
import com.example.socialbike.utilities.Updater;
import com.example.socialbike.groups.group.GroupActivity;
import com.example.socialbike.groups.group.GroupDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ExploreGroupsFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    private final GroupContainer groupContainer;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<Group> container = new ArrayList<>();
    private ProgressBar progressBar;
    private View root;
    private SwipeRefreshLayout swipeLayout;
    protected Set<String> groupIds = new HashSet<>();
    private final int DIVIDER_LAYOUT = 0;
    private final int EVENT_LAYOUT = 1;
    private boolean skipRefresh = false;

    public ExploreGroupsFragment(GroupContainer groupContainer) {
        this.groupContainer = groupContainer;
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_group, R.layout.fragment_group_location, container);
        recyclerViewAdapter.setClassReference(this);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_group, container, false);

            recyclerView = root.findViewById(R.id.recyclerview);
            progressBar = root.findViewById(R.id.progressBar);
            swipeLayout = root.findViewById(R.id.swipe_refresh);
            progressBar.setVisibility(View.VISIBLE);

            setSwipeLayout();
            initAdapter();
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!skipRefresh) {
            if (container.isEmpty() || container.get(0) == null)
                getGroups();
            else {
                locallyRefresh();
            }
        } else{
            skipRefresh = false;
        }
    }

    private void locallyRefresh() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 1; i < container.size(); i++){
            Group group = container.get(i);
            if (group.getIsMember()){
                if (!groupManager.MyConnectedGroups.containsKey(group.getGroupId())){
                    group.setIsMember(false);
                    indexes.add(i);
                }
            } else{
                if (groupManager.MyConnectedGroups.containsKey(group.getGroupId())){
                    group.setIsMember(true);
                    indexes.add(i);
                }
            }
        }

        for (int index : indexes){
            recyclerViewAdapter.notifyItemChanged(index);
        }
    }

    private void getGroups() {
        groupManager.getAllGroups(MainActivity.preferredLocationService.getPreferredPosition()).continueWith(task -> {
            parseGroups(String.valueOf(task.getResult().getData()));
            return null;
        });

    }

    private void parseGroups(String response) {
        System.out.println("groups response: " + response);
        container.clear();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GroupDTO groupDTO = objectMapper.readValue(response, GroupDTO.class);
            container.addAll(groupDTO.getGroups());
            for (Group group : container) {
                if (group == null)
                    continue;
                groupIds.add(group.getGroupId());
            }
            sortContainer();
            container.add(0, null); // for divider
            onFinishedUpdating();
        } catch (IOException e) {
            System.out.println( e.getLocalizedMessage());
        }
    }

    private void sortContainer() {
        ArrayList<Group> tmpContainer = new ArrayList<>();
        for (int i = 0; i < container.size(); i++) {
            Group group = container.get(i);
            if (group != null) {
                if (!group.getIsMember()) {
                    tmpContainer.add(group);
                    container.remove(group);
                    i--;
                }
            }
        }

        tmpContainer.addAll(container);
        container.addAll(tmpContainer);
    }


    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Group current = container.get(position);

        if (current == null) {
            Position location = MainActivity.preferredLocationService.getPreferredPosition();
            holder.location.setText("Find groups near " + location.getCity());
            holder.changeLocationButton.setOnClickListener(view -> {
                skipRefresh = true;
                Geo.startAutoComplete(null, this, TypeFilter.CITIES);
            });
            return;
        }

        holder.joinButton.setVisibility(View.VISIBLE);
        holder.joinButton.setOnClickListener(view -> joinOrLeaveGroup(holder, position));

        if (current.getIsMember())
            holder.joinButton.setText("Joined");
        else
            holder.joinButton.setText("Join");

        holder.layout.setOnClickListener(view -> openGroupActivity(current.getGroupId(), current.getTitle(), current.ownerOfGroup));
        holder.title.setText(current.getTitle());
        holder.description.setText(current.getDescription());
        holder.memberCount.setText(current.getMemberCount() + " members");
       // enableItemMenu(_holder.menu_button, position);
    }

    private void joinOrLeaveGroup(RecyclerViewAdapter.ViewHolder holder, int position) {
        if (!(holder.joinButton.getText().equals("Leaving") || holder.joinButton.getText().equals("Joining"))){
            Group group = container.get(position);
            if (group.getIsMember()){ // leave
                holder.joinButton.setText("Leaving");
                groupManager.leaveGroup(group.getGroupId()).continueWith(task -> {
                    holder.joinButton.setText("Join");
                    groupManager.remove(group);
                    return null;
                });
            } else { // join
                holder.joinButton.setText("Joining");
                groupManager.joinGroup(group.getGroupId()).continueWith(task -> {
                    holder.joinButton.setText("Joined");
                    groupManager.add(group);
                    group.setIsMember(true);
                    return null;
                });
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Position position = Geo.getPosition(data);
                MainActivity.preferredLocationService.savePreferredLocation(position);
                showLoading(true);
                getGroups();
            }
           /* if (resultCode == RESULT_OK) {
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

                position.setLatLng(place.getLatLng());
                position.setCity(place.getName());
                position.setCountry(country);
                preferredLocation.savePosition(position);
                //
                // = new Position(place.getLatLng(), place.getName(), country);
                //
                //updateCityTextView(position.getCountry());
                //getEvents();
            }*/
            }
    }

    private void showLoading(boolean show) {
        if (show){
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else{
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void openCitiesAutoComplete() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(getContext());
        startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
    }

    /*private void enableItemMenu(View view, int position) {
        view.setOnClickListener(view1 -> {
            PopupMenu popup = new PopupMenu(getContext(), view1);
            popup.inflate(R.menu.group);

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.leave_group:
                            container.get(position).exitGroup(getActivity());
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
    }
*/



    private int getIndex(String groupId) {
        for (int i = 0; i < container.size(); i++)
            if (container.get(i).getGroupId().equals(groupId))
                return i;
        return -1;
    }

    private void openGroupActivity(String groupId, String groupName, String ownerOfEvent) {
        Intent intent = new Intent(getContext(), GroupActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("groupName", groupName);
        intent.putExtra("ownerOfEvent", ownerOfEvent);
        startActivity(intent);
    }




    @Override
    public void onItemClick(@NonNull View holder, int position) {
        System.out.println(position);
    }

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
        skipRefresh = false;
        showLoading(false);
        swipeLayout.setRefreshing(false);
        recyclerViewAdapter.notifyDataSetChanged();
        System.out.println("container size: " + container.size());
    }

    class EmptyView extends RecyclerView.ViewHolder {

        TextView location;

        public EmptyView(View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.location);
        }

    }
}