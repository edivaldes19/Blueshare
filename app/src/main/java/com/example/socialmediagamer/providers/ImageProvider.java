package com.example.socialmediagamer.providers;

import android.content.Context;

import com.example.socialmediagamer.utils.CompressorBitmapImage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;

public class ImageProvider {
    private StorageReference mStorage;
    private final FirebaseStorage mFirebaseStorage;

    public ImageProvider() {
        mStorage = FirebaseStorage.getInstance().getReference();
        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    public UploadTask save(Context context, File file) {
        byte[] imageByte = CompressorBitmapImage.getImage(context, file.getPath(), 500, 500);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(new Date() + ".jpg");
        mStorage = storageReference;
        return storageReference.putBytes(imageByte);
    }

    public void deleteFromPath(String path) {
        mStorage = mFirebaseStorage.getReferenceFromUrl(path);
        mStorage.delete();
    }

    public StorageReference getStorage() {
        return mStorage;
    }
}