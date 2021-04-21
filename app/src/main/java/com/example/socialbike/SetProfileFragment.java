package com.example.socialbike;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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


public class SetProfileFragment extends Fragment {

    private View root;
    private Button doneButton;
    private EditText country, city, gender, age;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_set_nickname, container, false);

     //   nav = Navigation.findNavController(container);
      //  nickname_txt = root.findViewById(R.id.nickname);
        country = root.findViewById(R.id.country);
        city = root.findViewById(R.id.city);
        gender = root.findViewById(R.id.gender);
        age = root.findViewById(R.id.age);

        
        doneButton = root.findViewById(R.id.continue_button);
        startListening();

        return root;
    }

    private void startListening() {
        doneButton.setOnClickListener(view -> {
            submitForm();
        });
    }

    private void submitForm() {

        doneButton.setEnabled(false);

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
                                User.setNickname(answer);
                                MyPreferences.setSharedPreference(getActivity(), MyPreferences.USER_FOLDER, "nickname", answer);

                                return null;
                        }
                        doneButton.setEnabled(true);
                        return "";
                    }
                });

    }

}