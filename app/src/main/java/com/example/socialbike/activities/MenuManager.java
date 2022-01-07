package com.example.socialbike.activities;

import android.annotation.SuppressLint;
import android.view.Menu;
import android.view.MenuItem;

import com.example.socialbike.R;
import com.example.socialbike.utilities.ConnectedUser;

public class MenuManager {

    public static final int LOGIN_SIGN_UP = 1;
    public static final int LOG_OUT = 3;
    public static final int Profile = 10;
    public static final int ChatSettings = 11;
    public static final int MY_ACCOUNT = 12;
    public static final int RemoveChats = 13;



    private Menu menu;
    private int currentLayout;

    @SuppressLint("NonConstantResourceId")
    public void setMenu(Menu menu, int layout){
        this.menu = menu;
        menu.clear();
        currentLayout = layout;

        switch (layout){
            case R.layout.fragment_events: createEventsMenu(); break;
            case R.layout.fragment_group: createGroupMenu(); break;
            case R.layout.fragment_chat_lobby: createChatMenu(); break;
            case R.layout.fragment_profile: createProfileMenu(); break;
            case R.layout.activity_my_account: createAccountMenu(); break;
        }

        if (ConnectedUser.getPublicKey() != null && layout != R.layout.activity_my_account && !ConnectedUser.getPublicKey().equals("-")){

        }



    }

    private void hideMenu(int menuCode) {
        menu.removeItem(menuCode);

    }

    private void createAccountMenu() {
        createButton(LOG_OUT, "Log Out");
    }

    private void createMyAccountButton() {
        createButton(MY_ACCOUNT, "My Account");
    }

    private void actionEvents(MenuItem item) {
        switch (item.getItemId()) {


        }
    }

    private void createProfileMenu() {
        createButton(Profile, "Report");

    }

    private void createGroupMenu() {

    }

    private void createChatMenu() {
        createButton(RemoveChats, "Remove all chats");
        createButton(ChatSettings, "Settings");
    }

    private void createButton(int buttonCode, String button) {
        menu.add(0, buttonCode, Menu.NONE, button);
    }

    private void createLogInButton() {
        createButton(LOGIN_SIGN_UP, "Log in / Sign up");
    }

    private void createEventsMenu() {
        if (ConnectedUser.getPublicKey() == null || ConnectedUser.getPublicKey().equals("-")) {
            createLogInButton();
        } else
            createMyAccountButton();
    }


}
