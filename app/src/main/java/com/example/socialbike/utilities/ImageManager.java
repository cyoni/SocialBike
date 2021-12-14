package com.example.socialbike.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.socialbike.activities.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ImageManager {
    public static final int SELECT_PICTURE_CODE = 1;
    private Activity activity;
    private Fragment fragment;
    private Context context;

    public ImageManager(Activity activity){
        this.activity = activity;
        this.context = activity;
    }

    public ImageManager(Fragment fragment){
        this.fragment = fragment;
        this.context = fragment.getContext();
    }

    public byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bitmap.recycle();
        return stream.toByteArray();
    }

    public Bitmap compressImage(Bitmap image) {
        return Bitmap.createScaledBitmap(image, 1000, 1000, true);
    }

    public void loadPictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (fragment != null)
            fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_CODE);
        else if (activity != null)
            activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_CODE);

    }

    public UploadTask uploadImage(Bitmap bitmap, StorageReference ref) {

        byte[] byteArray = convertBitmapToByteArray(bitmap);
        System.out.println("Uploading picture...");
        return ref.putBytes(byteArray);
        };
       // setImageNoPicasso(Files.getPath(addNewEventActivity, "profile", ConnectedUser.getPublicKey()), addNewEventActivity.profile_image);
     //  setImage("..", addNewEventActivity.profile_image, out);


    public void setImage(Bitmap source, @NotNull final ImageView dest) {
        dest.setImageBitmap(source);
/*        File imgFile = new File(address);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            dest.setImageBitmap(myBitmap);
        }*/
    }

    public Task<byte[]> downloadPicture(StorageReference islandRef) {
        System.out.println("Downloading " + islandRef.getPath() + "...");
        FirebaseStorage storage = FirebaseStorage.getInstance();
       // StorageReference storageRef = storage.getReference();
        //StorageReference islandRef = storageRef.child(address);
        final long TWO_MEGABYTES = 2*1024 * 1024;
        return islandRef.getBytes(TWO_MEGABYTES);
    }

    public void locallySavePicture(Bitmap bitmapImage, String folder, String fileName) {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir(folder, Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, fileName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                System.out.println("wrote " + fileName + " successfully");
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    public void removePictureLocally(Context context, String folder, String filename){
        Files.removeFile(folder, filename, context);
    }

    public boolean doesPictureExistLocally(String folder, String fileName) {
        return Files.doesExist(context, folder, fileName);
    }

    public Bitmap loadPictureLocally(String folder, String filename) {
        File imgFile = new File(Files.getPath(context, folder, filename));
        Bitmap myBitmap = null;
        if (imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            //dest.setImageBitmap(myBitmap);
        }
        return myBitmap;
    }

    public Task<Void> removePictureRemotely(StorageReference islandRef, String path) {
        return islandRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                System.out.println("success");
                MainActivity.mDatabase.child(path).child("header_picture").removeValue();
                }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("failue");

            }
        });
    }
}
