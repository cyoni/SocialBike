package com.example.socialbike;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.socialbike.Post.POSTS_CONTAINER_CODE;

public class PostActivity extends AppCompatActivity
        implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    private ArrayList<Comment> commentsContainer = new ArrayList<>();
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
        recyclerViewAdapter = new RecyclerViewAdapter(this, R.layout.item_comment, commentsContainer);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        postMsg = findViewById(R.id.post);
        sendComment = findViewById(R.id.sendComment);
        newComment = findViewById(R.id.newComment);
        recyclerView = findViewById(R.id.recyclerview);
        initAdapter();

        updater = new Updater(this, this.commentsContainer, recyclerViewAdapter);
        messageManager = new MessageGetter(updater);
        commentsCounter = findViewById(R.id.commentsCounter);
        progressBar = findViewById(R.id.progressBar);

        getPost();
        setToolBarTitle();
        printPost();

        if (post.getCommentsCount() > 0) {
            progressBar.setVisibility(View.VISIBLE);
            getComments();
        }

        sendComment.setOnClickListener(view -> submitComment());

    }

    private void showRecyclerviewAndHideProgressBar() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void updateCommentsCounter() {
        String text;
        text = commentsContainer.size() + " ";
        text += (commentsContainer.size() == 1) ? "Comment" : "Comments";
        commentsCounter.setText(text);
    }

    private void getComments() {
        post.getComments()
                .continueWith(task -> {

                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + response);

                    if (!response.isEmpty()) {
                        showRecyclerviewAndHideProgressBar();
                        parseMessages(response);
                        updateCommentsCounter();
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

                Comment comment = new Comment(POSTS_CONTAINER_CODE, post.getPostId(), commentId, publicKey, name, 8888, message);

                for (int j = 0; j < subCommentsArray.length(); j++) {
                    String subCommentMessage = subCommentsArray.getJSONObject(j).getString("comment");
                    String subCommentName = subCommentsArray.getJSONObject(j).getString("name");
                    String subCommentId = subCommentsArray.getJSONObject(j).getString("name");
                    String subCommentSenderPublicKey = subCommentsArray.getJSONObject(j).getString("senderPublicKey");
                    int subCommentTimestamp = subCommentsArray.getJSONObject(j).getInt("timestamp");

                    System.out.println("Got subComment: " + subCommentsArray.getJSONObject(j).getString("commentId"));
                  //  SubComment subComment = new SubComment(subCommentId, subCommentSenderPublicKey, subCommentName, subCommentTimestamp, subCommentMessage);

                    comment.addSubComment(subCommentMessage);
                }

                updater.add(comment);

                System.out.println("msg " + i + " " + message);
            }
        } catch (Exception e) {
            System.out.println("Error caught in message fetcher: " + e.getMessage());
        }
    }


    private void submitComment() {

        String comment = newComment.getText().toString();

        if (comment.isEmpty() || sendComment.getText().equals("Sending"))
            return;

        sendComment.setText("Sending");
        Utils.hideKeyboard(this);
        Map<String, Object> data = new HashMap<>();
        data.put("comment", comment);
        data.put("postId", post.getPostId());
        data.put("replyTo", "");
        data.put("container", "global_posts");

        MainActivity.mFunctions
                .getHttpsCallable("sendComment")
                .call(data)
                .continueWith(task -> {
                    String postIdFromServer = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + postIdFromServer);
                    sendComment.setText("Send");
                    commentsContainer.add(0, new Comment(
                            POSTS_CONTAINER_CODE,
                            post.getPostId(),
                            postIdFromServer,
                            ConnectedUser.getPublicKey(),
                            ConnectedUser.getName(),
                            121221,
                            comment));
                    recyclerViewAdapter.notifyItemInserted(0);
                    newComment.setText("");
                    sendComment.setEnabled(true);
                    updateCommentsCounter();
                    Utils.hideKeyboard(this);

                    return null;
                });
    }


    private void setToolBarTitle() {
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.title);
        collapsingToolbarLayout.setTitle(post.getName() + " says");
  /*      if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(post.getName() + " says");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }*/
    }

    private void printPost() {
        postMsg.setText(post.getMsg());
    }

    private void getPost() {
        post = (Post) getIntent().getSerializableExtra("post");
        post.DatabaseContainer = POSTS_CONTAINER_CODE;
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Post post = commentsContainer.get(position);
        holder.message.setText(post.getMsg());
        holder.name.setText(post.getName());
        holder.replyButton.setOnClickListener(view -> addNewComment(holder, position));
        holder.newCommentSection.setVisibility(View.GONE);
        handleSubComments(holder, position);
    }

    private void handleSubComments(RecyclerViewAdapter.ViewHolder holder, int position) {
        if (commentsContainer.get(position).getSubComments().size() > 0)
            readSubComments(holder, commentsContainer.get(position).getSubComments());
    }

    private void readSubComments(RecyclerViewAdapter.ViewHolder holder, ArrayList<String> subComments) {
        for (int i = 0; i < subComments.size(); i++) {
            System.out.println("sub comment: " + subComments.get(i));
            addSubCommentToLayout(subComments.get(i), holder);
        }
    }

    private void addNewComment(RecyclerViewAdapter.ViewHolder holder, int position) {
        showOrHideNewCommentSection(holder);
        holder.postCommentButton.setOnClickListener(view -> sendSubComment(holder, position));
    }

    private void showOrHideNewCommentSection(RecyclerViewAdapter.ViewHolder holder) {
        if (holder.newCommentSection.getVisibility() == View.VISIBLE) {
            Utils.hideKeyboard(this);
            holder.newCommentSection.setVisibility(View.GONE);
        } else {
            holder.newCommentSection.setVisibility(View.VISIBLE);
            holder.commentText.requestFocus();
            Utils.showKeyboard(this);
        }
    }

    private void sendSubComment(RecyclerViewAdapter.ViewHolder holder, int position) {
        String comment = holder.commentText.getText().toString();

        if (comment.isEmpty() || holder.postCommentButton.getText().equals("Sending"))
            return;
        Utils.hideKeyboard(this);
        holder.postCommentButton.setText("Sending");
        commentsContainer.get(position).sendSubComment(comment).continueWith(task -> {

            String postIdFromServer = String.valueOf(task.getResult().getData());
            System.out.println("response: " + postIdFromServer);
            holder.postCommentButton.setText("Send");

            commentsContainer.get(position).addSubComment(comment);
            addSubCommentToLayout(holder.commentText.getText().toString(), holder);

            holder.commentText.setText("");

            showOrHideNewCommentSection(holder);
            System.out.println("Done.");

            return null;
        });
    }

    private void addSubCommentToLayout(String message, RecyclerViewAdapter.ViewHolder holder) {
        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        LinearLayout commentLayout = holder.commentLayout;
        LinearLayout relativeLayout = (LinearLayout) View.inflate(this, R.layout.item_sub_comment, null);

        relativeLayout.findViewById(R.id.replyButton).setOnClickListener(view -> quoteMember(holder));
        TextView commentText = relativeLayout.findViewById(R.id.message);
        commentText.setText(message);

        commentLayout.addView(relativeLayout);
    }

    private void quoteMember(RecyclerViewAdapter.ViewHolder holder) {
        showOrHideNewCommentSection(holder);
        String str = "@Yoni ";
        holder.commentText.setText(str);
        holder.commentText.requestFocus();
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }


    @Override
    public void onFinishedTakingNewMessages() {
        System.out.println("Finished!!!");
    }
}