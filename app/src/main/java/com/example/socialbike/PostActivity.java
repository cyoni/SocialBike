package com.example.socialbike;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends Activity implements RecyclerViewAdapter.ItemClickListener {

    private ArrayList<Post> container = new ArrayList<>();
    ;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private Updater updater;
    private MessageGetter messageManager;

    private Post post;
    private TextView postMsg;
    private EditText newComment;
    private Button sendComment;
    private TextView commentsCounter;
    private ProgressBar progressBar;

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(this, R.layout.item_comment, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        postMsg = findViewById(R.id.post);
        sendComment = findViewById(R.id.sendComment);
        newComment = findViewById(R.id.newComment);
        recyclerView = findViewById(R.id.recyclerview);
        initAdapter();

        updater = new Updater(this.container, recyclerViewAdapter);
        messageManager = new MessageGetter(updater);
        commentsCounter = findViewById(R.id.commentsCounter);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);

        getPost();
        printPost();
        getComments();

        sendComment.setOnClickListener(view -> submitComment());

    }

    private void showRecyclerviewAndHideProgressBar() {
        recyclerView.setVisibility(View.VISIBLE);
       progressBar.setVisibility(View.INVISIBLE);
    }

    private void updateCommentsCounter() {
        String text;
        text = container.size() + " ";
        text += (container.size() == 1) ? "Comment" : "Comments";
        commentsCounter.setText(text);
    }

    private void getComments() {

        System.out.println("Getting comments... " + post.getPostId());

        Map<String, Object> data = new HashMap<>();
        data.put("postId", post.getPostId());

        MainActivity.mFunctions
                .getHttpsCallable("getComments")
                .call(data)
                .continueWith(task -> {

                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + response);

                    if (!response.isEmpty()) {
                        parseMessages(response);
                        updateCommentsCounter();
                        showRecyclerviewAndHideProgressBar();
                    }
                    return "";
                });
    }

    private void parseMessages(String rawComments) {
        System.out.println(rawComments);

        String tmp_category;
        try {
            JSONObject obj = new JSONObject(rawComments);
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
                String postId = messages_array.getJSONObject(i).getString("postId");

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

                Post post = new Post(postId, publicKey, name, 8888, message);
                updater.add(post);
                updater.update();

                System.out.println("msg " + i + " " + message);
            }
        } catch (Exception e) {
            System.out.println("Error caught in message fetcher: " + e.getMessage());
        }

    }


    private void submitComment() {
        sendComment.setEnabled(false);
        String comment = newComment.getText().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("comment", comment);
        data.put("postId", post.getPostId());
        data.put("replyTo", "");

        MainActivity.mFunctions
                .getHttpsCallable("sendComment")
                .call(data)
                .continueWith(task -> {

                    String postIdFromServer = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + postIdFromServer);

                    container.add(0, new Post("7658858", User.getPublicKey(), User.getName(), 121221, comment));
                    recyclerViewAdapter.notifyDataSetChanged();

                    newComment.setText("");
                    sendComment.setEnabled(true);

                    return null;
                });


    }

    private void printPost() {
        postMsg.setText(post.getMsg());
    }

    private void getPost() {
        Bundle data = getIntent().getExtras();
        post = (Post) data.getParcelable("post");
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Post post = container.get(position);
        holder.message.setText(post.getMsg());
        holder.name.setText(post.getName());
        holder.commentButton.setOnClickListener(view -> addNewComment(holder));

    }

    private void addNewComment(RecyclerViewAdapter.ViewHolder holder) {
        holder.commentText.setVisibility(View.VISIBLE);
        holder.postCommentButton.setVisibility(View.VISIBLE);
        holder.postCommentButton.setOnClickListener(view -> sendCommentNow(holder));
    }

    private void sendCommentNow(RecyclerViewAdapter.ViewHolder holder) {
        System.out.println("sending comment. " + holder.commentText.getText().toString());

        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        //   Button layoutinputButton = (Button) findViewById(R.id.layoutinputButton);
        RelativeLayout mainLinearLayout = findViewById(R.id.mainLinearLayout);
        RelativeLayout linearLayout =
                (RelativeLayout) View.inflate(this,
                R.layout.pos, null);

        params1.addRule(RelativeLayout.BELOW, 33);
        mainLinearLayout.addView(linearLayout);
/*
        LinearLayout options_layout =  findViewById(R.id.options_list);
        String[] options = getResources().getStringArray(R.array.options);
        for (int i = 0; i < options.length; i++) {
            View to_add = inflater.inflate(R.layout.pos, options_layout,false);

          //  TextView text = (TextView) to_add.findViewById(R.id.text);
          //  text.setText(options[i]);
         //   text.setTypeface(FontSelector.getBold(getActivity()));
            options_layout.addView(to_add);
        }*/
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }
}