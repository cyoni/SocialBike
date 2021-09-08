package com.example.socialbike;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;


public class SetProfileFragment extends Fragment {

    private Button doneButton;
    private ImageButton skipButton;

    private EditText country, city, gender, age;
    private NavController nav;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_set_profile, container, false);
        country = root.findViewById(R.id.country);
        city = root.findViewById(R.id.city);
        gender = root.findViewById(R.id.gender);
        age = root.findViewById(R.id.age);
        doneButton = root.findViewById(R.id.done_button);
        skipButton = root.findViewById(R.id.skip_button);
        nav = Navigation.findNavController(container);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);        //  toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationOnClickListener(v -> nav.navigateUp());

        startListening();
        return root;
    }

    private void startListening() {
        doneButton.setOnClickListener(view -> submitForm());
        skipButton.setOnClickListener(view -> getActivity().finish());
    }

    private void submitForm() {
        Map<String, Object> data = new HashMap<>();
        data.put("country", country.getText().toString());
        data.put("city", city.getText().toString());
        data.put("gender", gender.getText().toString());
        data.put("age", age.getText().toString());
        getActivity().finish();

        MainActivity.mFunctions
                .getHttpsCallable("updateProfile")
                .call(data)
                .continueWith(task -> {
                    String answer = task.getResult().getData().toString();
                    System.out.println("Response from Server: " + answer);
                    MainActivity.startChat();
                    return "";
                });
    }

}