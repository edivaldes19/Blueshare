package com.manuel.blueshare.providers;

import com.manuel.blueshare.models.Like;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class LikesProvider {
    private final CollectionReference mCollection;

    public LikesProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Likes");
    }

    public Task<Void> create(Like like) {
        DocumentReference document = mCollection.document();
        String id = document.getId();
        like.setId(id);
        return document.set(like);
    }

    public Query getLikesByPost(String idPost) {
        return mCollection.whereEqualTo("idPost", idPost);
    }

    public Query getLikeByPostAndUser(String idPost, String idUser) {
        return mCollection.whereEqualTo("idPost", idPost).whereEqualTo("idUser", idUser);
    }

    public void delete(String id) {
        mCollection.document(id).delete();
    }
}