package com.example.socialbike;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

public class ImageManager {


    public byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bitmap.recycle();
        return stream.toByteArray();
    }

    public Bitmap compressImage(Bitmap image) {
        return Bitmap.createScaledBitmap(image, 1000, 1000, true);
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
}
