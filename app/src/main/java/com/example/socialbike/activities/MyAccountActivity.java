package com.example.socialbike.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.socialbike.R;
import com.example.socialbike.utilities.MyPreferences;
import com.example.socialbike.utilities.Utils;

public class MyAccountActivity extends AppCompatActivity implements MenuAction {

    MenuManager menuManager = new MenuManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Account");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }*/

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuManager.setMenu(menu, R.layout.activity_my_account);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        action(item);

        return false;
    }

    private void logout() {
        // delete all database

        MainActivity.stopChat();
        Utils.removePreference(this, MyPreferences.USER_FOLDER, "nickname" );
        Utils.removePreference(this, MyPreferences.USER_FOLDER, "user_public_key" );

        MainActivity.mAuth.signOut();
        MainActivity.toast(getApplicationContext(), "You have been logged out successfully.", true);
        finish();
    }

    @Override
    public void action(MenuItem item) {
        if (item.getItemId() == menuManager.LOG_OUT){
            logout();
        }
    }
}
