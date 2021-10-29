package com.example.socialbike.groups;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.socialbike.Constants;
import com.example.socialbike.MainActivity;
import com.example.socialbike.Methods;
import com.example.socialbike.Position;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.RecyclerViewAdapter2;
import com.example.socialbike.Updater;
import com.example.socialbike.groups.group.GroupActivity;
import com.example.socialbike.groups.group.GroupDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class GroupFragment extends Fragment implements RecyclerViewAdapter2.ItemClickListener, Updater.IUpdate {

    private final boolean isExplore;
    private final GroupContainer groupContainer;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter2 recyclerViewAdapter;
    private ArrayList<Group> container = new ArrayList<>();
    private ProgressBar progressBar;
    private View root;
    private SwipeRefreshLayout swipeLayout;
    protected Set<String> groupIds = new HashSet<>();
    private final int DIVIDER_LAYOUT = 0;
    private final int EVENT_LAYOUT = 1;
    private String location = "xxx";

    public GroupFragment(GroupContainer groupContainer, boolean isExplore) {
        this.isExplore = isExplore;
        this.groupContainer = groupContainer;
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter2(getContext(), R.layout.item_group, container);
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
        if (container.isEmpty())
            getGroups();
    }

    private void getGroups() {
        container.clear();

        Map<String, Object> data = new HashMap<>();

        MainActivity.mFunctions
                .getHttpsCallable(isExplore ? "GetAllGroups" : "GetMyGroups")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);

                    parseGroups(response);

                    return null;
                });
    }

    private void parseGroups(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GroupDTO groupDTO = objectMapper.readValue(response, GroupDTO.class);
            container.addAll(groupDTO.getGroups());
            for (Group group : container)
                groupIds.add(group.getGroupId());
            if (isExplore) {
                sortContainer();
            }

            container.add(0, null);

            onFinishedUpdating();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        if (holder.getItemViewType() == DIVIDER_LAYOUT) {
            EmptyView __holder = (EmptyView) holder;
            __holder.location.setOnClickListener(view -> openCitiesAutoComplete());
            __holder.location.setText(HtmlCompat.fromHtml
                    ("<u><b>"+ location +"</b></u>", HtmlCompat.FROM_HTML_MODE_LEGACY));

            return;
        }

        RecyclerViewAdapter2.ViewHolder _holder = (RecyclerViewAdapter2.ViewHolder) holder;

        Group current = container.get(position);
        if (isExplore) {
            _holder.joinButton.setVisibility(View.VISIBLE);
            _holder.joinButton.setOnClickListener(view -> joinOrLeaveGroup(_holder, position));
        } else
            _holder.joinButton.setVisibility(View.GONE);

        if (isExplore && current.getIsMember())
            _holder.joinButton.setText("Joined");
        else
            _holder.joinButton.setText("Join");
        _holder.layout.setOnClickListener(view -> openGroupActivity(current.getGroupId(), current.getTitle()));
        _holder.title.setText(current.getTitle());
        _holder.description.setText(current.getDescription());
        _holder.memberCount.setText(current.getMemberCount() + " members");
        enableItemMenu(_holder.menu_button, position);
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
                updateCityTextView(address.split(",")[0]);
               // getEvents();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateCityTextView(String country) {
        location = country;
        recyclerViewAdapter.notifyItemChanged(0);
    }

    private void openCitiesAutoComplete() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(getContext());
        startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
    }

    private void enableItemMenu(View view, int position) {
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
    }

    private void joinOrLeaveGroup(RecyclerViewAdapter2.ViewHolder holder, int position) {
        if (holder.joinButton.getText().toString().toLowerCase().equals("join")) {
            joinGroup(holder, position);
        } else
            leaveGroup(holder, position);
    }

    private void leaveGroup(RecyclerViewAdapter2.ViewHolder holder, int position) {
        holder.joinButton.setText("Join");
        String groupId = container.get(position).getGroupId();
        int index = getIndex(groupContainer.groupsThatImInFragment.container, groupId);
        groupContainer.groupsThatImInFragment.container.remove(index);
        groupContainer.groupsThatImInFragment.recyclerViewAdapter.notifyItemRemoved(index);
        leaveNow(groupId);
    }

    private void leaveNow(String groupId) {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        MainActivity.mFunctions
                .getHttpsCallable(Methods.LeaveGroup)
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    return null;
                });
    }

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

    private void joinGroup(RecyclerViewAdapter2.ViewHolder holder, int position) {
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
    }



    @Override
    public void onItemClick(@NonNull View holder, int position) {
        System.out.println(position);
    }

    @Override
    public RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == DIVIDER_LAYOUT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.title_with_location_layout, parent, false);
            return new EmptyView(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
            return recyclerViewAdapter.getLayoutView(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return DIVIDER_LAYOUT;
        else
            return EVENT_LAYOUT;
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
        swipeLayout.setRefreshing(false);
        recyclerViewAdapter.notifyItemRangeChanged(0, container.size());
        progressBar.setVisibility(View.GONE);
    }

    class EmptyView extends RecyclerView.ViewHolder {

        TextView location;

        public EmptyView(View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.location);
        }

    }
}