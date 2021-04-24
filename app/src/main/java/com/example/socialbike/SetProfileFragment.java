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
        root = inflater.inflate(R.layout.fragment_set_profile, container, false);
        country = root.findViewById(R.id.country);
        city = root.findViewById(R.id.city);
        gender = root.findViewById(R.id.gender);
        age = root.findViewById(R.id.age);
        doneButton = root.findViewById(R.id.done_button);
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
        data.put("country", country.getText().toString());
        data.put("city", city.getText().toString());
        data.put("gender", gender.getText().toString());
        data.put("age", age.getText().toString());

        MainActivity.mFunctions
                .getHttpsCallable("updateProfile")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) {
                        String answer = task.getResult().getData().toString();
                        System.out.println("Response from Server: " + answer);

                        if (answer.equals("OK"))
                                getActivity().finish();
                            else
                                MainActivity.toast(getContext(), "error", 1);
                        doneButton.setEnabled(true);
                        return "";
                    }
                });

    }

}