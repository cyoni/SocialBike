package com.example.socialbike.chat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.socialbike.ConnectedUser;
import com.example.socialbike.DateUtils;
import com.example.socialbike.MainActivity;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ChatLobbyFragment extends Fragment
        implements RecyclerViewAdapter.ItemClickListener {

    static ChatLobbyFragment chatFragment = null;
    private RecyclerView recyclerView;
    protected final ArrayList<PreviewChatMessage> users = new ArrayList<>();
    private final ArrayList<PreviewChatMessage> reserve = new ArrayList<>();
    private final HashSet<String> prefixWithoutResults = new HashSet<>();
    public RecyclerViewAdapter recyclerViewAdapter;
    private Context context;
    private View root;
    private EditText searchUserTextbox;
    ProgressBar progressBar;


    public static ChatLobbyFragment getInstance() {
        if (chatFragment == null) {
            chatFragment = new ChatLobbyFragment();
        }
        return chatFragment;
    }

    public ChatLobbyFragment() {
        System.out.println("ChatLobbyFragment() call");
    }

    private void initAdapter() {
        recyclerViewAdapter = new
                RecyclerViewAdapter(getContext(), R.layout.item_chat_user, users);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.chatManager.chatLobbyFragment = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        if (root == null) {
            root = inflater.inflate(R.layout.fragment_chat_lobby, container, false);
            recyclerView = root.findViewById(R.id.recyclerview);
            searchUserTextbox = root.findViewById(R.id.search_box);
            progressBar = root.findViewById(R.id.progressBar);


            initSearchMembersTextBox();
            initAdapter();
            recyclerViewAdapter.setClassReference(this); // reference this class to the adaptor

            MainActivity.chatManager.currentConversationChat = null;
            // get user list
            reserve.addAll(users);

            if (ConnectedUser.getName() != null)
                loadUsersFromLocalDB();

        }
        return root;
    }

    private void initSearchMembersTextBox() {
        searchUserTextbox.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.toString().isEmpty())
                            return;


                        if (charSequence.toString().equals("reset")) {
                            users.clear();
                            List<PreviewChatMessage> chatMessages = MainActivity.chatManager.previewMessage.getAllMembers();
                            for (int _i=0; _i<chatMessages.size(); _i++)
                                MainActivity.chatManager.previewMessage.delete(chatMessages.get(_i));
                            recyclerViewAdapter.notifyDataSetChanged();
                            searchUserTextbox.setText("");
                            return;
                        }

                        if (charSequence.toString().equals("ref")) {
                            users.clear();
                            loadUsersFromLocalDB();
                            recyclerViewAdapter.notifyDataSetChanged();
                            searchUserTextbox.setText("");
                            return;
                        }

                        String name, message = "";
                        if (charSequence.toString().contains("*") && charSequence.toString().contains(":")) {
                            String text;
                            text = charSequence.toString().substring(0, charSequence.toString().length() - 1);
                            String[] array = text.split(":");
                            name = array[0];
                            message = array[1];

                            Map<String, Object> map = new HashMap<>();
                            map.put("senderPublicKey", name);
                            map.put("receiverPublicKey", "-MYVCkWexSO_jumnbr0l");
                            map.put("message", message);
                            map.put("sendersName", name);
                            map.put("timestamp", DateUtils.getTimeInMiliSecs());

                            MainActivity.mDatabase.child("private_msgs").
                                    child("-MYVCkWexSO_jumnbr0l").
                                    child("fakeMsgId_" + DateUtils.getTimeInMiliSecs()).
                                    child("testSenderKey").
                                    setValue(map);
                            System.out.println("SENT " + message + "; " + name);
                            searchUserTextbox.setText(name);
                        }

/*                        if (charSequence.toString().isEmpty()) {
                            users.clear();
                            users.addAll(reserve);
                            recyclerViewAdapter.notifyDataSetChanged();
                        } else if (charSequence.toString().length() > 0) {
                            findUsers(charSequence.toString());
                        }*/
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                }
        );
    }


    private void loadUsersFromLocalDB() { // TODO Work on another thread
        List<PreviewChatMessage> chatMessages = MainActivity.chatManager.previewMessage.getAllMembers();
        users.addAll(chatMessages);
        recyclerViewAdapter.notifyItemRangeChanged(0, chatMessages.size());
    }

/*    private void loadUsers() {
        userDao.getAllMembers().observe(getViewLifecycleOwner(), members -> {
            users.addAll(members);
            recyclerViewAdapter.notifyDataSetChanged();
        });
    }*/

    private void findUsers(String input) {
        users.clear();

        ArrayList<PreviewChatMessage> localUsers = getLocalUsers(input);
        users.addAll(localUsers);

        if (input.length() > 2 && isPrefixOk(input)) {
            getUsersFromServer(input).continueWith(task -> {
                String response = String.valueOf(task.getResult().getData());
                System.out.println("find user, response:" + response);
                parseUsers(input, response);
                return "";
            });
        }

        recyclerViewAdapter.notifyDataSetChanged();
    }

    private boolean isPrefixOk(String input) {
        for (String current : prefixWithoutResults) {
            if (prefixWithoutResults.contains(input)
                    || input.length() >= current.length()
                    && input.substring(0, current.length()).contains(current)) {
                System.out.println(input + " is in bad input");
                return false;
            }
        }
        return true;
    }

    private ArrayList<PreviewChatMessage> getLocalUsers(String user) {
        ArrayList<PreviewChatMessage> tmp = new ArrayList<>();
        for (int i = 0; i < reserve.size(); i++) {
            if (reserve.get(i).publicKey.toLowerCase().contains(user.toLowerCase()))
                tmp.add(reserve.get(i));
        }
        return tmp;
    }

    private Task<HttpsCallableResult> getUsersFromServer(String user) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> data = new HashMap<>();
        data.put("name", user);
        System.out.println("sending " + user);
        return MainActivity.mFunctions
                .getHttpsCallable("findUsers")
                .call(data);
    }

    private void parseUsers(String input, String response) {
        progressBar.setVisibility(View.GONE);

        if (response.isEmpty()) {
            // no results
            return;
        }

        JSONArray data = null;
        JSONObject obj;
        try {
            obj = new JSONObject(response);

            data = obj.getJSONArray("users");


        } catch (Exception e) {
            System.out.println("An error was caught in message fetcher: " + e.getMessage());
        }

        ArrayList<PreviewChatMessage> tmp = new ArrayList<>();


        if (data.length() == 0) {
            prefixWithoutResults.add(input);
            System.out.println(input + " was added to no result list");
        } else
            for (int i = 0; i < data.length(); i++) {
                try {
                    String userId = data.getJSONObject(i).getString("userId");
                    String name = data.getJSONObject(i).getString("name");
                    //String time = data.getJSONObject(i).getString("name");
                    boolean doesExist = false;

                    for (PreviewChatMessage currentUser : users) {
                        if (currentUser.name.equals(name)) {
                            doesExist = true;
                            break;
                        }
                    }

                    ///    if (!doesExist)
                    //        tmp.add(new PreviewChatMessage(userId, name, "", 0, 0, isIncomingMessage));

                } catch (JSONException e) {
                    System.out.println("An error was caught in message fetcher: " + e.getMessage());
                }
            }

        users.addAll(tmp);
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.layout.setOnClickListener(view -> openConversationActivity(position));
        PreviewChatMessage current = users.get(position);
        holder.name.setText(current.name);
        holder.message_preview.setText(current.previewMsg);
        holder.time.setText(DateUtils.convertMiliToTime(current.time));
        System.out.println(current.name + ", unread msgs: " + users.get(position).unreadMessages + ", time: " + current.time );
        if (current.unreadMessages == 0) {
            holder.red_dot.setVisibility(View.INVISIBLE);
        } else {
            holder.red_dot.setVisibility(View.VISIBLE);
            holder.red_dot.setText(String.valueOf(current.unreadMessages));
        }
    }

/*    @SuppressLint("UnsafeOptInUsageError")
    private void showBadge(TextView anchor) {
        Context context = getContext();
     //   if (context != null) {
            BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
            badgeDrawable.setVisible(true);
            badgeDrawable.setNumber(8);
            BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor);
     //   }
    }*/

    private void openConversationActivity(int position) {
        PreviewChatMessage chatMember = users.get(position);
        String userId = chatMember.publicKey;
        String name = chatMember.name;
        chatMember.unreadMessages = 0;
        MainActivity.chatManager.openConversationActivity(getContext(), userId, name);
        MainActivity.chatManager.previewMessage.resetUnreadMessages(userId);
        recyclerViewAdapter.notifyItemChanged(position);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {
        // nav.navigate(R.id.action_chatFragment_to_chatConversationFragment);
    }

    public void insert(PreviewChatMessage chatMember) {
        AsyncTask.execute(() -> {
            MainActivity.chatManager.previewMessage.insert(chatMember);
        });
    }


/*    public void updateLobbyList(ChatMessage chatMessage) {
        System.out.println("CHAT: a new message from " + chatMessage.getSendersName() + ": " + chatMessage.getMessage());
    }*/

    /*    public void addNewIncomeMessage(String senderPublicKey, ChatMember chatMember) {
     *//*   if (incomingMessages.get(senderPublicKey) == null) {
            incomingMessages.put(senderPublicKey, new ArrayList<>());
        }
        incomingMessages.get(senderPublicKey).add(chatMember);
        System.out.println("New message by " + senderPublicKey + " was recorded successfully");*//*
    }*/

}