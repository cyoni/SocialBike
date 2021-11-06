package com.example.socialbike.activities;

import android.content.Intent;
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

import com.example.socialbike.post.Comment;
import com.example.socialbike.model.CommentDTO;
import com.example.socialbike.utilities.ConnectedUser;
import com.example.socialbike.utilities.DateUtils;
import com.example.socialbike.utilities.MessageGetter;
import com.example.socialbike.post.Post;
import com.example.socialbike.post.PostButtons;
import com.example.socialbike.R;
import com.example.socialbike.recyclerview.RecyclerViewAdapter;
import com.example.socialbike.post.SubComment;
import com.example.socialbike.utilities.Updater;
import com.example.socialbike.utilities.Utils;
import com.example.socialbike.room_database.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ProgressBar progressBar;
    private String groupId, eventId;

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
        progressBar = findViewById(R.id.progressBar);

        getPost();
        loadInit();

        new PostButtons(this, post, groupId, eventId);

        setToolBarTitle();
        printPost();

        sendComment.setOnClickListener(view -> submitComment());
    }

    private void loadInit() {
        if (post.getCommentsCount() > 0) {
            progressBar.setVisibility(View.VISIBLE);
            getComments();
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showRecyclerviewAndHideProgressBar() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void getComments() {
        System.out.println("Getting comments for post #" + post.getPostId());
        Map<String, Object> data = new HashMap<>();
        data.put("postId", post.getPostId());
        data.put("groupId", groupId);
        data.put("eventId", eventId);

        MainActivity.mFunctions
                .getHttpsCallable("getComments")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + response);
                    if (!response.isEmpty()) {
                        showRecyclerviewAndHideProgressBar();
                        parseMessages(response);
                    }
                    return "";
                });
    }

    private void parseMessages(String response) {
        System.out.println(response);

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                CommentDTO commentDTO = objectMapper.readValue(response, CommentDTO.class);
                setFieldsFromParentNode(commentDTO.getComments());
                commentsContainer.addAll(commentDTO.getComments());
                onFinishedUpdating();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void setFieldsFromParentNode(List<Comment> comments) {
        for (Comment comment : comments){
            for (SubComment subComment : comment.getSubComments()){
                subComment.setCommentKey(comment.commentKey);
                subComment.setPostId(comment.getPostId());
            }
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
        data.put("eventId", eventId);
        data.put("groupId", groupId);

        MainActivity.mFunctions
                .getHttpsCallable("sendComment")
                .call(data)
                .continueWith(task -> {
                    String postIdFromServer = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + postIdFromServer);
                    sendComment.setText("Send");
                    commentsContainer.add(0, new Comment(
                            post.getPostId(),
                            postIdFromServer,
                            ConnectedUser.getPublicKey(),
                            ConnectedUser.getName(),
                            DateUtils.getTimeInMiliSecs(),
                            comment));
                    recyclerViewAdapter.notifyItemInserted(0);
                    recyclerViewAdapter.notifyItemRangeChanged(0, commentsContainer.size() - 1);

                    newComment.setText("");
                    sendComment.setEnabled(true);
                    Utils.hideKeyboard(this);

                    return null;
                });
    }

    private void setToolBarTitle() {
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.title);
        collapsingToolbarLayout.setTitle(post.getName());
    }

    private void printPost() {
        postMsg.setText(post.getMsg());
    }

    private void getPost() {
        Intent intent = getIntent();
        post = (Post) intent.getSerializableExtra("post");
        groupId = intent.getStringExtra("groupId");
        eventId = intent.getStringExtra("eventId");
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Post comment = commentsContainer.get(position);
        holder.message.setText(comment.getMsg());
        Member.fetchAndSetName(holder, comment.getName(), comment.getPublicKey());
        holder.replyButton.setOnClickListener(view -> addNewComment(holder, position));
        holder.likeTextButton.setOnClickListener(view -> likeComment(holder.likeTextButton, comment));
        if (comment.getIsLiked())
            changeLikeButtonStyle(holder.likeTextButton, comment);
        holder.relativelayout.setVisibility(View.GONE);
        handleSubComments(holder, position);
    }


    private void likeComment(TextView reference, Post comment) {
        comment.setIsLiked(!comment.getIsLiked());
        changeLikeButtonStyle(reference, comment);
        Utils.registerLike(comment, groupId, eventId);
    }

    private void changeLikeButtonStyle(TextView reference, Post comment) {
        if (comment.getIsLiked())
            reference.setTextColor(getResources().getColor(R.color.black));
         else
            reference.setTextColor(getResources().getColor(R.color.default_black));

    }

    private void handleSubComments(RecyclerViewAdapter.ViewHolder holder, int position) {
        if (commentsContainer.get(position).getSubComments().size() > 0)
            readSubComments(holder, commentsContainer.get(position).getSubComments());
    }

    private void readSubComments(RecyclerViewAdapter.ViewHolder holder, ArrayList<SubComment> subComments) {
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
        if (holder.relativelayout.getVisibility() == View.VISIBLE) {
            Utils.hideKeyboard(this);
            holder.relativelayout.setVisibility(View.GONE);
        } else {
            holder.relativelayout.setVisibility(View.VISIBLE);
            holder.comments.requestFocus();
            Utils.showKeyboard(this);
        }
    }

    public Task<HttpsCallableResult> sendSubComment(Comment comment){
            System.out.println("sending comment: " + comment.getMessage());
            Map<String, Object> data = new HashMap<>();
            data.put("comment", comment.getMessage());
            data.put("postId", comment.getPostId());
            data.put("replyTo", comment.getCommentKey());
            data.put("groupId", groupId);
            data.put("eventId", eventId);

            return MainActivity.mFunctions
                    .getHttpsCallable("sendComment")
                    .call(data);
    }

    private void sendSubComment(RecyclerViewAdapter.ViewHolder holder, int position) {
        String comment = holder.comments.getText().toString();

        if (comment.isEmpty() || holder.postCommentButton.getText().equals("Sending"))
            return;
        Utils.hideKeyboard(this);
        holder.postCommentButton.setText("Sending");

        SubComment subComment = new SubComment(
                post.getPostId(),
                commentsContainer.get(position).getCommentKey(),
                null,
                ConnectedUser.getPublicKey(),
                ConnectedUser.getName(),
                DateUtils.getTimeInMiliSecs(),
                comment);

        sendSubComment(subComment).continueWith(task -> {

            String postIdFromServer = String.valueOf(task.getResult().getData());
            System.out.println("response: " + postIdFromServer);
            holder.postCommentButton.setText("Send");

            subComment.setSubCommentId(postIdFromServer);

            commentsContainer.get(position).addSubComment(subComment);
            addSubCommentToLayout(subComment, holder);

            holder.comments.setText("");

            showOrHideNewCommentSection(holder);
            System.out.println("Done.");

            return null;
        });
    }

    private void addSubCommentToLayout(SubComment subComment, RecyclerViewAdapter.ViewHolder holder) {
        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        LinearLayout commentLayout = holder.commentLayout;
        LinearLayout relativeLayout = (LinearLayout) View.inflate(this, R.layout.item_sub_comment, null);

        TextView holderName = relativeLayout.findViewById(R.id.name);
        String name = holderName.getText().toString();
        Member.fetchAndSetName(holderName, name, subComment.getPublicKey());
        relativeLayout.findViewById(R.id.replyButton).setOnClickListener(view -> quoteMember(holder, name));
        TextView likeTextButton = relativeLayout.findViewById(R.id.likeTextButton);

        if (subComment.getIsLiked())
            changeLikeButtonStyle(likeTextButton, subComment);
        likeTextButton.setOnClickListener(view -> likeComment((TextView) view, subComment));

        TextView commentText = relativeLayout.findViewById(R.id.description);
        commentText.setText(subComment.getMsg());

        commentLayout.addView(relativeLayout);
    }


    private void quoteMember(RecyclerViewAdapter.ViewHolder holder, String name) {
        showOrHideNewCommentSection(holder);
        String str = "@" + name + " ";
        holder.comments.setText(str);
        holder.comments.requestFocus();
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }


    @Override
    public void onFinishedUpdating() {
        recyclerViewAdapter.notifyDataSetChanged();
    }
}