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
    protected final ArrayList<ChatMember> users = new ArrayList<>();
    private final ArrayList<ChatMember> reserve = new ArrayList<>();
    private final HashSet<String> prefixWithoutResults = new HashSet<>();
    public RecyclerViewAdapter recyclerViewAdapter;
    private Context context;
    private View root;
    private EditText searchUserTextbox;
    ProgressBar progressBar;
    private ChatMemberDao userDao;


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
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        if (root == null) {
            root = inflater.inflate(R.layout.fragment_chat, container, false);
            recyclerView = root.findViewById(R.id.recyclerview);
            searchUserTextbox = root.findViewById(R.id.search_box);
            progressBar = root.findViewById(R.id.progressBar);
            userDao = MainActivity.database.chatMemberDao();

            loadUsers();

            searchUserTextbox.addTextChangedListener(
                    new TextWatcher() {

                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (charSequence.toString().isEmpty()) {
                                users.clear();
                                users.addAll(reserve);
                                recyclerViewAdapter.notifyDataSetChanged();
                            } else if (charSequence.toString().length() > 0) {
                                findUsers(charSequence.toString());
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    }
            );

            initAdapter();
            //  context = getActivity();

            recyclerViewAdapter.setClassReference(this); // reference this class to the adaptor


            MainActivity.chatManager.currentConversationChat = null;

            // get user list
            reserve.addAll(users);

        }
        return root;
    }

    private void loadUsers() {

        ChatMember person1 = new ChatMember("12345", "yoni");


        AsyncTask.execute(() -> {
            // Insert Data
        //    userDao.insert(person1);

            List<ChatMember> chatMembers = userDao.getAllMembers();

            for (ChatMember member : chatMembers){
                users.add(new ChatMember("2234663", member.publicKey, member.name, member.previewMsg));
                System.out.println("got: " + member.name);
            }

        });

/*         userDao.getAllMembers().observe(getViewLifecycleOwner(), chatMembers -> {
             for (ChatMember member : chatMembers) {

             }
         });*/
    }

    private void findUsers(String input) {
        users.clear();

        ArrayList<ChatMember> localUsers = getLocalUsers(input);
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

    private ArrayList<ChatMember> getLocalUsers(String user) {
        ArrayList<ChatMember> tmp = new ArrayList<>();
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

        ArrayList<ChatMember> tmp = new ArrayList<>();


        if (data.length() == 0) {
            prefixWithoutResults.add(input);
            System.out.println(input + " was added to no result list");
        } else
            for (int i = 0; i < data.length(); i++) {
                try {
                    String userId = data.getJSONObject(i).getString("userId");
                    String name = data.getJSONObject(i).getString("name");
                    boolean doesExist = false;

                    for (ChatMember currentUser : users) {
                        if (currentUser.name.equals(name)) {
                            doesExist = true;
                            break;
                        }
                    }

                    if (!doesExist)
                        tmp.add(new ChatMember("", userId, name, ""));

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
        holder.name.setText(users.get(position).name);
        holder.message_preview.setText(users.get(position).previewMsg);
        if (users.get(position).isRead) {
            holder.red_dot.setVisibility(View.INVISIBLE);
        } else
            holder.red_dot.setVisibility(View.VISIBLE);
    }

    private void openConversationActivity(int position) {
        String userId = users.get(position).publicKey;
        String name = users.get(position).name;
        MainActivity.chatManager.openConversationActivity(getContext(), userId, name);
        users.get(position).isRead = true;
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

    public void updateLobbyList(ChatMessage chatMessage) {
        System.out.println("CHAT: a new message from " + chatMessage.getSendersName() + ": " + chatMessage.getMessage());
    }

    public void addNewIncomeMessage(String senderPublicKey, ChatMember chatMember) {
        if (incomingMessages.get(senderPublicKey) == null) {
            incomingMessages.put(senderPublicKey, new ArrayList<>());
        }
        incomingMessages.get(senderPublicKey).add(chatMember);
        System.out.println("New message by " + senderPublicKey + " was recorded successfully");
    }

}