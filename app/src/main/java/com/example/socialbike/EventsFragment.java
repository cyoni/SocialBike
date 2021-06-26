package com.example.socialbike;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.socialbike.Event.EVENTS_CONTAINER_CODE;


public class EventsFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    static EventsFragment eventsFragment = null;
    private RecyclerView recyclerView;
    private final ArrayList<Event> container = new ArrayList<>();
    private RecyclerViewAdapter recyclerViewAdapter;
    private Updater updater;
    private boolean loadMore = true;
    private ProgressBar progressBar;
    private final String MOST_RECENT_CODE = "MOST_RECENT";
    private final String TRADING_CODE = "TRADING";
    private String dataType = MOST_RECENT_CODE;


    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_events, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static EventsFragment getInstance() {
        if (eventsFragment == null) {
            eventsFragment = new EventsFragment();
        }
        return eventsFragment;
    }

    public void getEvents() {

        System.out.println("getting Events...");

        container.clear();

        Map<String, Object> data = new HashMap<>();
        data.put("dataType", dataType);

        MainActivity.mFunctions
                .getHttpsCallable("getEvents")
                .call(data)
                .continueWith(task -> {

                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);

                    if (!response.isEmpty()) {
                        parseMessages(response);
                    }
                    return "";
                });

    }

    public void parseMessages(String fresh_msgs) {

        try {
            JSONObject obj = new JSONObject(fresh_msgs);
            JSONArray data = obj.getJSONArray("events");

            if (container.size() > 0 && data.length() > 0) {
                container.clear();
                recyclerViewAdapter.notifyDataSetChanged();
            }

            for (int i = 0; i < data.length(); i++) {

                String userPublicKey = data.getJSONObject(i).getString("userPublicKey");
                String message = data.getJSONObject(i).getString("eventContent");
                String name = data.getJSONObject(i).getString("name");
                String dateOfEvent = data.getJSONObject(i).getString("eventDate");
                String timeOfEvent = data.getJSONObject(i).getString("eventTime");
                String createdEventTime = data.getJSONObject(i).getString("createdEventTime");
                String eventId = data.getJSONObject(i).getString("eventId");
                String numOfInterestedMembers = data.getJSONObject(i).getString("numOfInterestedMembers");
                String city = data.getJSONObject(i).getString("eventCity");
                String country = data.getJSONObject(i).getString("eventCountry");
                int commentsNumber = data.getJSONObject(i).getInt("commentsNumber");

                int numberOfParticipants = 0;
                if (data.getJSONObject(i).has("numberOfParticipants") && data.getJSONObject(i).get("numberOfParticipants") instanceof Integer)
                    numberOfParticipants = data.getJSONObject(i).getInt("numberOfParticipants");

                Event event = new Event(
                        eventId, userPublicKey, name,
                        dateOfEvent, timeOfEvent, createdEventTime,
                        numOfInterestedMembers, numberOfParticipants,
                        city, country, message, commentsNumber);
                updater.add(event);

                System.out.println("post " + i + " " + message);
            }
            onFinishedTakingNewMessages();
        } catch (Exception e) {
            System.out.println("An error was caught in message fetcher: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_events, container, false);
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();
        progressBar = root.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);


        updater = new Updater(this, this.container, recyclerViewAdapter);
        if (loadMore) {
            showProgressbar();

            getEvents();
            loadMore = false;
        }

        Button addEvent = root.findViewById(R.id.add_new_event_button);
        Button changeRegion = root.findViewById(R.id.search_places);

        changeRegion.setOnClickListener(view -> {
            openSearchWindow();
        });

        addEvent.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AddNewEventActivity.class);
            startActivity(intent);
        });

        Button sortButton = root.findViewById(R.id.sort_button);
        sortButton.setOnClickListener(view -> {

            if (sortButton.getText().equals("Trending")) {
                getRecentData();
                sortButton.setText("Recent Activity");
            } else {
                getTradingData();
                sortButton.setText("Trending");
            }

        });

        return root;
    }

    private void showProgressbar() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressbar() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }


    private void getRecentData() {
        showProgressbar();
        dataType = MOST_RECENT_CODE;
        getEvents();
    }

    private void getTradingData() {
        showProgressbar();
        dataType = TRADING_CODE;
        getEvents();
    }

    private void openSearchWindow() {

    }



    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.message.setText(container.get(position).getMsg());
        holder.country.setText(container.get(position).getCountry());
        holder.city.setText(container.get(position).getCity());
        holder.time.setText(container.get(position).getTimeOfEvent());
        holder.date.setText(container.get(position).getDateOfEvent());
        holder.name.setText(container.get(position).getName());
        holder.people_going.setText(container.get(position).getNumberOfParticipants() + " people going");
        holder.interested.setOnClickListener(view -> interested(holder, position));
        holder.coming.setOnClickListener(view -> markAsGoing(holder, position));
        holder.who_is_coming.setOnClickListener(view -> who_is_going(holder, position));
        holder.who_is_interested.setOnClickListener(view -> who_is_interested(holder, position));
        holder.commentButton.setOnClickListener(view -> commentButton(holder, position));

        // holder.amountOfInterestedPeople.setText(container.get(position).getAmountOfInterestedPeople());
    }

    private void commentButton(RecyclerViewAdapter.ViewHolder holder, int position) {

        if (container.get(position).hasComments()){
            holder.progressBar.setVisibility(View.VISIBLE);
            getComments(holder, position);
        }
        else{
            holder.progressBar.setVisibility(View.GONE);
        }

        holder.commentLayout.setVisibility(View.VISIBLE);
        holder.postCommentButton.setOnClickListener(view -> sendMainComment(holder, position));
    }

    private void getComments(RecyclerViewAdapter.ViewHolder holder, int position) {
            container.get(position).getComments()
                    .continueWith(task -> {

                        String response = String.valueOf(task.getResult().getData());
                        System.out.println("response: " + response);

                        if (!response.isEmpty()) {
                            holder.progressBar.setVisibility(View.GONE);

                            processComments(response, holder, position);

                        }

                        return "";
                    });
    }

    private void processComments(String response, RecyclerViewAdapter.ViewHolder holder, int position) {

        String tmp_category;
        try {
            JSONObject obj = new JSONObject(response);
            JSONArray messages_array = obj.getJSONArray("posts");

/*            if (obj.has("upNext")) {
                upNext = obj.getString("upNext");
            }
            else
                upNext = DEFAULT_END_OF_LIST;*/

            for (int i = 0; i < messages_array.length(); i++) {
                int likes = 0, comments = 0;
                boolean doILike = false, isAuthor = false, has_profile_img = false;

                String publicKey = messages_array.getJSONObject(i).getString("publicKey");
                String message = messages_array.getJSONObject(i).getString("message");
                String name = messages_array.getJSONObject(i).getString("name");
                String time = messages_array.getJSONObject(i).getString("timestamp");
                String commentId = messages_array.getJSONObject(i).getString("postId");

                JSONArray subCommentsArray = messages_array.getJSONObject(i).getJSONArray("subComments");

                if (messages_array.getJSONObject(i).has("likes_count")) {
                    likes = Integer.parseInt(messages_array.getJSONObject(i).getString("likes_count"));
                }
                if (messages_array.getJSONObject(i).has("comments_count")) {
                    comments = Integer.parseInt(messages_array.getJSONObject(i).getString("comments_count"));
                }
                if (messages_array.getJSONObject(i).has("doILike")) {
                    doILike = messages_array.getJSONObject(i).getBoolean("doILike");
                }
                if (messages_array.getJSONObject(i).has("isauthor")) {
                    isAuthor = messages_array.getJSONObject(i).getBoolean("isauthor");
                }
                if (messages_array.getJSONObject(i).has("has_p_img")) {
                    has_profile_img = messages_array.getJSONObject(i).getBoolean("has_p_img");
                }

                Comment comment = new Comment(EVENTS_CONTAINER_CODE, container.get(position).getEventId(), commentId, publicKey, name, 8888, message);

                LinearLayout layout = addCommentToLayout(R.layout.item_comment, comment, holder, position);

                for (int j = 0; j < subCommentsArray.length(); j++) {
                    String subCommentMessage = subCommentsArray.getJSONObject(j).getString("comment");
                    String subCommentName = subCommentsArray.getJSONObject(j).getString("name");
                    String subCommentId = subCommentsArray.getJSONObject(j).getString("name");
                    String subCommentSenderPublicKey = subCommentsArray.getJSONObject(j).getString("senderPublicKey");
                    int subCommentTimestamp = subCommentsArray.getJSONObject(j).getInt("timestamp");

                    //System.out.println("Got subComment: " + subCommentsArray.getJSONObject(j).getString("commentId"));
                    System.out.println("got subcomment: " + subCommentMessage);
                    SubComment subComment = new SubComment(EVENTS_CONTAINER_CODE, commentId, subCommentId, subCommentSenderPublicKey, subCommentName, subCommentTimestamp, subCommentMessage);
                    addSUBCommentToLayout(layout, subComment, holder, position);
                    //comment.addSubComment(subCommentMessage);
                }

                System.out.println("comment " + i + " " + message);


            }
        } catch (Exception e) {
            System.out.println("Error caught in message fetcher: " + e.getMessage());
        }

    }

    private void sendMainComment(RecyclerViewAdapter.ViewHolder holder, int position) {

        String comment = holder.commentText.getText().toString();

        if (comment.isEmpty())
            return;

        sendComment(container.get(position).getEventId(), "", comment).continueWith(task -> {

                    String commentIdFromServer = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + commentIdFromServer);

                    Comment newComment = new Comment(
                            EVENTS_CONTAINER_CODE,
                            container.get(position).getEventId(),
                            commentIdFromServer,
                            User.getPublicKey(),
                            User.getName(),
                            121221,
                            comment);

                    container.get(position).addComment(newComment);

                    addCommentToLayout(R.layout.item_comment, newComment, holder, position);

                    holder.commentText.setText("");
                    System.out.println("Done.");

                    return null;
                }
        );


    }

    private void addSUBCommentToLayout(LinearLayout headComment, Comment comment, RecyclerViewAdapter.ViewHolder holder, int position) {
        RelativeLayout layout = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        LinearLayout linearLayout = (LinearLayout) View.inflate(getContext(), R.layout.item_sub_comment, null);

        linearLayout.findViewById(R.id.commentButton).setOnClickListener(view -> quoteMember(holder, position, headComment));


        TextView commentText = linearLayout.findViewById(R.id.message);
        TextView commentName = linearLayout.findViewById(R.id.name);
        commentText.setText(comment.getMsg());
        commentName.setText(comment.getName());

        headComment.addView(linearLayout);
    }

    private LinearLayout addCommentToLayout(int commentType, Comment comment, RecyclerViewAdapter.ViewHolder holder, int position) {
        RelativeLayout layout = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        LinearLayout commentLayout = holder.commentLayout;
        LinearLayout linearLayout = (LinearLayout) View.inflate(getContext(), commentType, null);

      //  if (commentType == R.layout.item_sub_comment)
      //     linearLayout.findViewById(R.id.commentButton).setOnClickListener(view -> quoteMember(holder, position, linearLayout));

         if (commentType == R.layout.item_comment) {
            linearLayout.findViewById(R.id.commentButton).setOnClickListener(view -> showOrHideNewCommentSection(linearLayout));
            linearLayout.findViewById(R.id.postCommentButton).setOnClickListener(view -> sendSubComment(linearLayout, holder, position, comment));
         }

        TextView commentText = linearLayout.findViewById(R.id.message);
        TextView commentName = linearLayout.findViewById(R.id.name);
        commentText.setText(comment.getMsg());
        commentName.setText(comment.getName());

        commentLayout.addView(linearLayout);
        return linearLayout;
    }

    private void sendSubComment(LinearLayout linearLayout, RecyclerViewAdapter.ViewHolder holder, int position, Comment comment) {
        EditText viewComment = linearLayout.findViewById(R.id.headCommentText);
        String commentStr = viewComment.getText().toString();

        //sendComment(container.get(position).getEventId(), comment.getPostId(), commentStr).continueWith(response -> {
          comment.sendSubComment(commentStr).continueWith(task -> {
              String response = String.valueOf(task.getResult().getData());
              System.out.println("res: " + response);

              SubComment subComment = new SubComment(EVENTS_CONTAINER_CODE, comment.getPostId(), response, User.getPublicKey(), User.getName(), 12345, commentStr);
              addSUBCommentToLayout(linearLayout, subComment, holder, position);
              viewComment.setText("");
              return null;
          });


    }

    private void showOrHideNewCommentSection(LinearLayout linearLayout) {
        RelativeLayout relativeLayout = linearLayout.findViewById(R.id.newCommentSection);

        if (relativeLayout.getVisibility() == View.GONE)
            relativeLayout.setVisibility(View.VISIBLE);
        else
            relativeLayout.setVisibility(View.GONE);
    }


    private Task<HttpsCallableResult> sendComment(String eventId, String replyTo, String comment) {

        Map<String, Object> data = new HashMap<>();
        data.put("postId", eventId);
        data.put("replyTo", replyTo);
        data.put("comment", comment);
        data.put("container", "events");

        System.out.println(comment + "," + eventId + ", " + replyTo);

        return MainActivity.mFunctions
                .getHttpsCallable("sendComment")
                .call(data);
    }

    private void addSubCommentToLayout(String message, RecyclerViewAdapter.ViewHolder holder, int position) {
        RelativeLayout layout = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        LinearLayout commentLayout = holder.commentLayout;
        RelativeLayout relativeLayout = (RelativeLayout) View.inflate(getContext(), R.layout.item_sub_comment, null);

        relativeLayout.findViewById(R.id.commentButton).setOnClickListener(view -> quoteMember(holder, position, null));
        TextView commentText = relativeLayout.findViewById(R.id.message);
        commentText.setText(message);

        commentLayout.addView(relativeLayout);
    }

    private void quoteMember(RecyclerViewAdapter.ViewHolder holder, int position, LinearLayout headComment) {
        RelativeLayout section = headComment.findViewById(R.id.newCommentSection);
        if (section.getVisibility() == View.GONE)
            section.setVisibility(View.VISIBLE);
        Post currentPost = container.get(position);
        String str = "@" + currentPost.getName() + " ";
        EditText headCommentText = headComment.findViewById(R.id.headCommentText);
        headCommentText.setText(str);
    }

    private void who_is_going(RecyclerViewAdapter.ViewHolder holder, int position) {
        MembersList membersList = new MembersList(getActivity(), container.get(position).getEventId(), "going");
        membersList.show();
    }

    private void who_is_interested(RecyclerViewAdapter.ViewHolder holder, int position) {
        MembersList membersList = new MembersList(getActivity(), container.get(position).getEventId(), "interested");
        membersList.show();
    }

    private void markAsGoing(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.coming.setEnabled(false);

        System.out.println(container.get(position).getEventId());
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", container.get(position).getEventId());

        MainActivity.mFunctions
                .getHttpsCallable("going")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    holder.coming.setEnabled(true);
                    return "";
                });
    }

    private void interested(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.interested.setEnabled(false);

        System.out.println(container.get(position).getEventId());
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", container.get(position).getEventId());

        MainActivity.mFunctions
                .getHttpsCallable("interested")
                .call(data)
                .continueWith(task -> {

                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    holder.interested.setEnabled(true);
                    return "";
                });
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    @Override
    public void onFinishedTakingNewMessages() {
        hideProgressbar();
    }
}