package com.example.socialbike;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.socialbike.Event.EVENTS_CONTAINER_CODE;

public class EventsCommentsExtension {

    private final EventsManager eventsFragment;

    public EventsCommentsExtension(EventsManager eventsFragment) {
        this.eventsFragment = eventsFragment;
    }

    protected void commentButton(RecyclerViewAdapter.ViewHolder holder, int position) {

        if (holder.commentLayout.getVisibility() == View.GONE){
            if (eventsFragment.container.get(position).hasComments()){
                holder.progressBar.setVisibility(View.VISIBLE);
                removeOldComments(holder);
                getComments(holder, position);
            }
            holder.commentLayout.setVisibility(View.VISIBLE);
            holder.postCommentButton.setOnClickListener(view -> sendHeadComment(holder, position));
        } else{
            holder.progressBar.setVisibility(View.GONE);
            holder.commentLayout.setVisibility(View.GONE);
        }
    }

    private void removeOldComments(RecyclerViewAdapter.ViewHolder holder) {
        holder.commentLayout.removeAllViewsInLayout();
    }

    private void getComments(RecyclerViewAdapter.ViewHolder holder, int position) {
        eventsFragment.container.get(position).getComments()
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

                Comment comment = new Comment(EVENTS_CONTAINER_CODE, eventsFragment.container.get(position).getEventId(), commentId, publicKey, name, 8888, message);

                LinearLayout layout = addCommentToLayout(R.layout.item_comment, comment, holder, position);

                for (int j = 0; j < subCommentsArray.length(); j++) {
                    String subCommentMessage = subCommentsArray.getJSONObject(j).getString("comment");
                    String subCommentName = subCommentsArray.getJSONObject(j).getString("name");
                    String subCommentId = subCommentsArray.getJSONObject(j).getString("name");
                    String subCommentSenderPublicKey = subCommentsArray.getJSONObject(j).getString("senderPublicKey");
                    int subCommentTimestamp = subCommentsArray.getJSONObject(j).getInt("timestamp");

                    //System.out.println("Got subComment: " + subCommentsArray.getJSONObject(j).getString("commentId"));
                    System.out.println("got subcomment: " + subCommentMessage);
                    SubComment subComment = new SubComment(EVENTS_CONTAINER_CODE, commentId, subCommentId, "33", subCommentSenderPublicKey, subCommentName, subCommentTimestamp, subCommentMessage);
                    addSUBCommentToLayout(layout, subComment, holder, position);
                    //comment.addSubComment(subCommentMessage);
                }

                System.out.println("comment " + i + " " + message);


            }
        } catch (Exception e) {
            System.out.println("Error caught in message fetcher: " + e.getMessage());
        }

    }


    private void sendHeadComment(RecyclerViewAdapter.ViewHolder holder, int position) {

        holder.postCommentButton.setText("SENDING...");
        String comment = holder.comments.getText().toString();

        if (comment.isEmpty())
            return;

        sendComment(eventsFragment.container.get(position).getEventId(), "", comment).continueWith(task -> {

                    String commentIdFromServer = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + commentIdFromServer);

                    Comment newComment = new Comment(
                            EVENTS_CONTAINER_CODE,
                            eventsFragment.container.get(position).getEventId(),
                            commentIdFromServer,
                            ConnectedUser.getPublicKey(),
                            ConnectedUser.getName(),
                            121221,
                            comment);


                    eventsFragment.container.get(position).addComment(newComment);

                    addCommentToLayout(R.layout.item_comment, newComment, holder, position);

                    holder.comments.setText("");
                    holder.postCommentButton.setText("Send");
                    System.out.println("Done.");

                    return null;
                }
        );
    }


    private void sendSubComment(LinearLayout linearLayout, RecyclerViewAdapter.ViewHolder holder, int position, Comment comment) {
        EditText viewComment = linearLayout.findViewById(R.id.headCommentText);
        Button commentButton = linearLayout.findViewById(R.id.commentButton);
        commentButton.setText("Sending...");
        String commentStr = viewComment.getText().toString();

        comment.sendSubComment(commentStr).continueWith(task -> {
            String response = String.valueOf(task.getResult().getData());
            System.out.println("res: " + response);

            SubComment subComment = new SubComment(EVENTS_CONTAINER_CODE, comment.getPostId(), "ff", response, ConnectedUser.getPublicKey(), ConnectedUser.getName(), 12345, commentStr);
            addSUBCommentToLayout(linearLayout, subComment, holder, position);
            viewComment.setText("");
            commentButton.setText("Send");
            return null;
        });
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

    private void showOrHideNewCommentSection(LinearLayout linearLayout) {
        RelativeLayout relativeLayout = linearLayout.findViewById(R.id.newCommentSection);

        if (relativeLayout.getVisibility() == View.GONE)
            relativeLayout.setVisibility(View.VISIBLE);
        else
            relativeLayout.setVisibility(View.GONE);
    }

    private void addSUBCommentToLayout(LinearLayout headComment, Comment comment, RecyclerViewAdapter.ViewHolder holder, int position) {
        RelativeLayout layout = new RelativeLayout(eventsFragment.getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        LinearLayout linearLayout = (LinearLayout) View.inflate(eventsFragment.getContext(), R.layout.item_sub_comment, null);

        linearLayout.findViewById(R.id.commentButton).setOnClickListener(view -> quoteMember(holder, position, headComment));

        TextView commentText = linearLayout.findViewById(R.id.message);
        TextView commentName = linearLayout.findViewById(R.id.name);
        commentText.setText(comment.getMsg());
        commentName.setText(comment.getName());

        headComment.addView(linearLayout);
    }

    private LinearLayout addCommentToLayout(int commentType, Comment comment, RecyclerViewAdapter.ViewHolder holder, int position) {
        RelativeLayout layout = new RelativeLayout(eventsFragment.getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        LinearLayout commentLayout = holder.commentLayout;
        LinearLayout linearLayout = (LinearLayout) View.inflate(eventsFragment.getContext(), commentType, null);

        if (commentType == R.layout.item_comment) {
            linearLayout.findViewById(R.id.commentButton).setOnClickListener(view -> showOrHideNewCommentSection(linearLayout));
            linearLayout.findViewById(R.id.postCommentButton).setOnClickListener(view -> sendSubComment(linearLayout, holder, position, comment));
        }

        TextView commentText = linearLayout.findViewById(R.id.message);
        TextView commentName = linearLayout.findViewById(R.id.name);
        commentText.setText(comment.getMsg());
        commentName.setText(comment.getName());

        commentLayout.addView(linearLayout, 0);
        return linearLayout;
    }

    private void quoteMember(RecyclerViewAdapter.ViewHolder holder, int position, LinearLayout headComment) {
        RelativeLayout section = headComment.findViewById(R.id.newCommentSection);
        if (section.getVisibility() == View.GONE)
            section.setVisibility(View.VISIBLE);
        Post currentPost = eventsFragment.container.get(position);
        String str = "@" + currentPost.getName() + " ";
        EditText headCommentText = headComment.findViewById(R.id.headCommentText);
        headCommentText.setText(str);
    }


}



