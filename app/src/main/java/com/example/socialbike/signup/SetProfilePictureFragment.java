package com.example.socialbike.signup;

import static com.example.socialbike.utilities.ImageManager.SELECT_PICTURE_CODE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.socialbike.R;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.ImageManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;


public class SetProfilePictureFragment extends Fragment {

    View root;
    Button doneButton, skipButton, loadPictureButton;
    ImageManager imageManager;

    public SetProfilePictureFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment fragment = this;
        imageManager = new ImageManager(fragment);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_set_profile_picture, container, false);
            doneButton = root.findViewById(R.id.done_button);
            skipButton = root.findViewById(R.id.skip_button);
            loadPictureButton = root.findViewById(R.id.load_button);
            setButtonListeners();

        }

        return root;
    }

    private void setButtonListeners() {
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPictureValid()){
                    uploadPicture();
                }
            }
        });

        skipButton.setOnClickListener(view -> getActivity().finish());

        loadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPicture();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                Bitmap compressImage = imageManager.compressImage(bitmap);
                Bitmap copy = imageManager.compressImage(bitmap);
                Bitmap copy2 = imageManager.compressImage(bitmap);

                imageManager.setImage(compressImage, headerPicture);

                StorageReference ref = getPath().child("header");;

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.setMessage("Uploading image");
                progressDialog.show();

                imageManager.uploadImage(copy, ref).addOnSuccessListener(x -> {
                    progressDialog.dismiss();
                    imageManager.removePictureLocally(this, "event_picture_headers", event.getEventId());
                    imageManager.locallySavePicture(copy2, "event_picture_headers", event.getEventId());
                    MainActivity.toast(this, "Success!", true);
                } ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void pickPicture() {
        imageManager.loadPictureFromGallery();


    }

    private void uploadPicture() {

    }

    private boolean isPictureValid() {
        return false;
    }
}