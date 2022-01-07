package com.example.socialbike.signup;

import static android.app.Activity.RESULT_OK;
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
import android.widget.ImageView;

import com.example.socialbike.R;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.ConnectedUser;
import com.example.socialbike.utilities.Consts;
import com.example.socialbike.utilities.ImageManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;


public class SetProfilePictureFragment extends Fragment {

    View root;
    Button doneButton, skipButton, loadPictureButton;
    ImageView profilePicture;
    ImageManager imageManager;
    private Bitmap bitmap;

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
            profilePicture = root.findViewById(R.id.profile_picture);
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

        skipButton.setOnClickListener(view -> {
            getActivity().onBackPressed();
        });

        loadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPicture();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                Bitmap compressImage = imageManager.compressImage(bitmap);


                imageManager.setImage(compressImage, profilePicture);



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
        Bitmap copy = imageManager.compressImage(bitmap);
        Bitmap copy2 = imageManager.compressImage(bitmap);

        StorageReference ref = MainActivity.storageRef.child("members").child(ConnectedUser.getPublicKey()).child("profile").child("main");;

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading...");
        progressDialog.setMessage("Uploading image");
        progressDialog.show();

        imageManager.uploadImage(copy, ref).addOnSuccessListener(x -> {
            progressDialog.dismiss();
            imageManager.removePictureLocally(getContext(), Consts.Profile_Picture, ConnectedUser.getPublicKey());
            imageManager.locallySavePicture(copy2, Consts.Profile_Picture, ConnectedUser.getPublicKey());
            MainActivity.toast(getContext(), "Success!", true);
            getActivity().onBackPressed();
        } ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });
    }

    private boolean isPictureValid() {
        return bitmap != null;
    }

}