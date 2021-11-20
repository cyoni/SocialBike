package com.example.socialbike.signup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.socialbike.R;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.ConnectedUser;
import com.example.socialbike.utilities.MyPreferences;
import com.example.socialbike.utilities.Utils;

import java.util.HashMap;
import java.util.Map;


public class SetNicknameFragment extends Fragment {

    private Button continueButton;
    private NavController nav;
    EditText nickname_txt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void startListening() {
        continueButton.setOnClickListener(view -> {
            String name = nickname_txt.getText().toString();
            if (MainActivity.isUserConnected &&
                    (ConnectedUser.getName() == null ||
                            ConnectedUser.getName().toLowerCase().equals(name.toLowerCase()))) {
                proceedToNextFragment();
            } else if (isNameValid(name)) {
                setNickname();
            } else
                MainActivity.toast(getContext(), "Name is not valid.", true);
        });
    }

    private boolean isNameValid(String name) {
        return !(ConnectedUser.getName() == null
                || name.trim().isEmpty());
    }

    private void proceedToNextFragment() {
        Utils.hideKeyboard(getActivity());
        nav.navigateUp();
    }

    private void setNickname() {
        if (continueButton.getText().toString().equals("Please wait"))
            return;
        continueButton.setText("Please wait");
        String nickname = nickname_txt.getText().toString().trim();
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", nickname);

        MainActivity.mFunctions
                .getHttpsCallable("updateNickname")
                .call(data)
                .continueWith(task -> {
                    String answer = task.getResult().getData().toString();
                    System.out.println("Response from Server: " + answer);
                    switch (answer) {
                        case "[INVALID_NICKNAME]":
                            MainActivity.toast(getContext(), "This nickname is invalid", true);
                            changeButtonText();
                            break;
                        case "[NICKNAME_TAKEN]":
                            MainActivity.toast(getContext(), "This nickname is already taken.", true);
                            changeButtonText();
                            break;
                        case "[AUTH_FAILED]":
                            MainActivity.toast(getContext(), answer, true);
                            changeButtonText();
                            break;
                        default:
                            MainActivity.isUserConnected = true;
                            ConnectedUser.setNickname(answer);
                            MyPreferences.setSharedPreference(getActivity(), MyPreferences.USER_FOLDER, "nickname", answer);
                            proceedToNextFragment();
                            return null;
                    }
                    return "";
                });

    }

    private void changeButtonText() {
        continueButton.setText("Continue");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_set_nickname, container, false);
        nav = Navigation.findNavController(container);
        nickname_txt = root.findViewById(R.id.nickname);
        continueButton = root.findViewById(R.id.continue_button);
        startListening();
        nickname_txt.requestFocus();
        Utils.showKeyboard(getActivity());
        return root;
    }
}