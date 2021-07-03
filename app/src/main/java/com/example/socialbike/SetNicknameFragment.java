package com.example.socialbike;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;


public class SetNicknameFragment extends Fragment {

    private View root;
    private Button continueButton;
    private NavController nav;
    EditText nickname_txt;

    public SetNicknameFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void startListening() {

        continueButton.setOnClickListener(view -> {

            if (ConnectedUser.getName() == null || !ConnectedUser.getName().toLowerCase().equals(nickname_txt.getText().toString().toLowerCase())){
                setNickname();
            }
            else
                proceedToTheNextFragment();


        });

    }

    private void proceedToTheNextFragment() {

          nav.navigate(R.id.action_se2tNicknameFragment_to_setProfileFragment);
    }

    private void setNickname() {
        continueButton.setEnabled(false);

        String nickname = nickname_txt.getText().toString().trim();

        Map<String, Object> data = new HashMap<>();
        data.put("nickname", nickname);

        MainActivity.mFunctions
                .getHttpsCallable("updateNickname")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) {
                        String answer = task.getResult().getData().toString();
                        System.out.println("Response from Server: " + answer);

                        switch (answer) {
                            case "[INVALID_NICKNAME]":
                                MainActivity.toast(getContext(), "This nickname is invalid", 1);
                                break;
                            case "[NICKNAME_TAKEN]":
                                MainActivity.toast(getContext(), "This nickname is already taken.", 1);
                                break;
                            case "[AUTH_FAILED]":
                                MainActivity.toast(getContext(), answer, 1);
                                break;
                            default:
                                ConnectedUser.setNickname(answer);
                                MyPreferences.setSharedPreference(getActivity(), MyPreferences.USER_FOLDER, "nickname", answer);
                                proceedToTheNextFragment();
                                return null;
                        }
                      //  continueButton.setEnabled(true);
                        return "";
                    }
                });

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_set_nickname, container, false);

        nav = Navigation.findNavController(container);
        nickname_txt = root.findViewById(R.id.nickname);

        continueButton = root.findViewById(R.id.continue_button);
        startListening();

        return root;
    }
}